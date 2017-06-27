(ns admgame.model.game-es
  (:require [schema.core :as s]
            [admgame.cryptography :as crypto]
            [admgame.database :as db]
            [admgame.model.game :refer [Game 
                                        Team
                                        CreateResult
                                        remove-sensitive-data
                                        make-key]])
  (:use [slingshot.slingshot :only [throw+]]))

; Reducers

(def game-initial-state nil)
(def game-tutor-initial-state [])

(s/defn game-reducer [state event]
  (case (:type event)
    "create-game" (let [{:keys [tutor title key]} event]
                    {:key key
                     :title title
                     :tutor tutor
                     :teams []})

    "create-team" (let [{:keys [key name password]} event]
                    (update state :teams conj {:key key
                                               :name name
                                               :password password
                                               :stock {:product 0 
                                                       :wrapper 0}
                                               :cash 0}))
    state))

(s/defn game-tutor-reducer [state event]
  (case (:type event)
    "tutor-create-game" (let [{:keys [title key]} event]
                          (conj state {:title title :key key}))
     state))

; Aggregate ID functions

(s/defn game-aggregate-id :- s/Str [tutor :- s/Str, game-key :- s/Str]
  (str "/tutor/" tutor "/game/" game-key))

(s/defn tutor-game-aggregate-id :- s/Str [tutor    :- s/Str]
  (str "/tutor/" tutor "/game"))

; Reading functions

(def tutor-game-reader (db/make-reader game-tutor-reducer game-tutor-initial-state))
(def game-reader       (db/make-reader game-reducer game-initial-state))

(s/defn find-all-by-tutor :- [Game] [tutor :- s/Str]
  (tutor-game-reader (tutor-game-aggregate-id tutor)))

(s/defn find-by-tutor-and-key-complete :- (s/maybe Game) [tutor :- s/Str, key :- s/Str]
  (game-reader (game-aggregate-id tutor key)))

(s/defn find-by-tutor-and-key :- (s/maybe Game) [tutor :- s/Str, key :- s/Str]
  (remove-sensitive-data
      (find-by-tutor-and-key-complete tutor key)))

(s/defn find-team-by-key :- (s/maybe Team) [game-data :- Game
                                            team-key :- s/Str]
  (->> game-data
       :teams
       (filter #(= team-key (:key %)))
       first))

;Event emitting functions

(s/defn create-game-event :- s/Any
        [tutor :- s/Str title :- s/Str]
  (let [game-key       (make-key title)
        event-id  (str "create-game:" game-key)
        aggregate-id   (game-aggregate-id tutor game-key)
        payload        {:type "create-game" :tutor tutor :title title :key game-key}]
    (db/save-event! event-id aggregate-id payload)))

(s/defn tutor-create-game-event :- s/Any 
        [tutor :- s/Str title :- s/Str]
  (let [game-key       (make-key title)
        event-id  (str "tutor-create-game:" game-key)
        aggregate-id   (tutor-game-aggregate-id tutor)
        payload        {:type "tutor-create-game" :tutor tutor :title title :key game-key}]
    (db/save-event! event-id aggregate-id payload)))

(s/defn create-team-event :- s/Any
        [tutor :- s/Str, game-key :- s/Str, team-name :- s/Str, password :- s/Str]
  (let [team-key     (make-key team-name)
        event-id     (str "create-game-team:" team-key)
        aggregate-id (game-aggregate-id tutor game-key)
        payload      {:type "create-team" 
                      :key team-key 
                      :name team-name 
                      :password (crypto/encrypt! password)}]
    (db/save-event! event-id aggregate-id payload)))

; API functions

(s/defn create-game :- CreateResult [data :- {:tutor :- s/Str
                                              :title :- s/Str}]
  (let [{:keys [tutor title]} data
        game-key (make-key title)
        game (find-by-tutor-and-key-complete tutor game-key)]
    (when-not (nil? game)
      (throw+ {:type :game-with-existing-key :message "a game with the same tutor and key already exists"}))
    (create-game-event tutor title)
    (tutor-create-game-event tutor title)
    {:id game-key}))

(s/defn create-team :- CreateResult [data :- {:tutor     s/Str
                                              :game-key  s/Str
                                              :team-name s/Str
                                              :password  s/Str}]
  (let [target-game (find-by-tutor-and-key-complete (:tutor data) (:game-key data))
        {:keys [team-name game-key tutor password]} data
        team-key    (make-key team-name)]
    (when (nil? target-game)
      (throw+ {:type :game-not-found :message "cannot find a game with given tutor and key"}))
    (when-not (nil? (find-team-by-key target-game team-key))
      (throw+ {:type :team-with-existing-key :message "a team with the same key already exists"}))
    (create-team-event tutor game-key team-name password)
    {:id team-key}))