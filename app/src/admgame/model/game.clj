(ns admgame.model.game
  (:require [schema.core :as s]
            [clojure.string :as string]
            [admgame.model.game-events :as e]
            [admgame.model.game-reader :as r])
  (:use [slingshot.slingshot :only [throw+]]))

(def Team {:key s/Str
           :name s/Str
           :password (s/maybe s/Str)
           :stock {:product s/Num
                   :wrapper s/Num}
           :cash s/Num})

(def Game {:key s/Str
           :title s/Str
           :start-date java.util.Date
           :close-date (s/maybe java.util.Date)
           :tutor s/Str
           :teams [Team]})

(def CreateResult {:id s/Str})

(s/defn make-key :- s/Str [title :- s/Str]
  (-> title
      (string/trim)
      (string/replace #"\s+" "-" )
      (string/lower-case)))

(defn remove-sensitive-data [game]
  (-> game
      (dissoc :_id)
      (update-in [:teams] (fn [teams] (map #(dissoc % :password) teams)))))

(s/defn find-all-by-tutor :- [Game] [tutor :- s/Str]
  (r/find-all-by-tutor tutor))

(s/defn find-by-tutor-and-key-complete :- (s/maybe Game) [tutor :- s/Str, key :- s/Str]
  (r/find-by-tutor-and-key tutor key))

(s/defn find-by-tutor-and-key :- (s/maybe Game) [tutor :- s/Str, key :- s/Str]
  (remove-sensitive-data
      (find-by-tutor-and-key-complete tutor key)))

(s/defn find-team-by-key :- (s/maybe Team) [game-data :- Game
                                            team-key :- s/Str]
  (->> game-data
       :teams
       (filter #(= team-key (:key %)))
       first))

(s/defn create-game :- CreateResult [data :- {:tutor :- s/Str
                                              :title :- s/Str}]
  (let [{:keys [tutor title]} data
        game-key (make-key title)
        game (r/find-by-tutor-and-key tutor game-key)
        event-data {:tutor tutor, :title title, :game-key game-key}]
    (when-not (nil? game)
      (throw+ {:type :game-with-existing-key :message "a game with the same tutor and key already exists"}))
    (e/create-game-event event-data)
    (e/tutor-create-game-event event-data)
    {:id game-key}))

(s/defn create-team :- CreateResult [data :- {:tutor     s/Str
                                              :game-key  s/Str
                                              :team-name s/Str
                                              :password  s/Str}]
  (let [target-game (r/find-by-tutor-and-key (:tutor data) (:game-key data))
        {:keys [team-name game-key tutor password]} data
        team-key    (make-key team-name)]
    (when (nil? target-game)
      (throw+ {:type :game-not-found :message "cannot find a game with given tutor and key"}))
    (when-not (nil? (find-team-by-key target-game team-key))
      (throw+ {:type :team-with-existing-key :message "a team with the same key already exists"}))
    (e/create-team-event {:tutor tutor
                          :game-key game-key
                          :team-name team-name,
                          :team-key team-key
                          :password password})
    {:id team-key}))