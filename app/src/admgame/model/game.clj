(ns admgame.model.game
  (:require [schema.core :as s]
            [clojure.string :as string]
            [admgame.cryptography :as crypto]
            [monger.operators :refer [$push]]
            [admgame.database :as db])
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

(def game-collection "game")

(defn uuid [] (str (java.util.UUID/randomUUID)))

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
  (map remove-sensitive-data
    (db/find-all game-collection {:tutor tutor} ["tutor" "title" "key" "teams"])))

(s/defn find-by-tutor-and-key-complete :- (s/maybe Game) [tutor :- s/Str, key :- s/Str]
  (db/find-one game-collection {:tutor tutor :key key} ["tutor" "title" "key" "teams"]))

(s/defn find-by-tutor-and-key :- (s/maybe Game) [tutor :- s/Str, key :- s/Str]
  (remove-sensitive-data
      (find-by-tutor-and-key-complete tutor key)))

(s/defn create-game :- CreateResult [data :- {:tutor s/Str
                                              :title s/Str}]
  (let [{:keys [tutor title]} data
        key (make-key title)]
    (when-not (nil? (find-by-tutor-and-key tutor key))
      (throw+ {:type :game-with-existing-key :message "a game with the same tutor and key already exists"}))
    (db/insert game-collection {:tutor tutor :title title :key key :teams []})
    {:id key}))

(s/defn find-team-by-key :- (s/maybe Team) [game-data :- Game
                                            team-key :- s/Str]
  (->> game-data
       :teams
       (filter #(= team-key (:key %)))
       first))

(s/defn create-team :- CreateResult [data :- {:tutor     s/Str
                                              :game-key  s/Str
                                              :team-name s/Str
                                              :password  s/Str}]

  (let [target-game (find-by-tutor-and-key (:tutor data) (:game-key data))
        {:keys [team-name game-key tutor password]} data
        team-key    (make-key team-name)]
    (when (nil? target-game)
      (throw+ {:type :game-not-found :message "cannot find a game with given tutor and key"}))
    (when-not (nil? (find-team-by-key target-game team-key))
      (throw+ {:type :team-with-existing-key :message "a team with the same key already exists"}))
    
    (db/update-document game-collection {:tutor tutor
                                         :key game-key}
                                        {$push {:teams {:key team-key
                                                        :name team-name
                                                        :password (crypto/encrypt! password)
                                                        :stock {:product 0
                                                                :wrapper 0}
                                                        :cash 0}}})
    {:id team-key}))