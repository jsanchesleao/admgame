(ns admgame.model.game
  (:require [schema.core :as s]
            [admgame.database :as db]))

(def Team {:key s/Str
           :name s/Str
           :password s/Str
           :stock {:product s/Num
                   :wrapper s/Num}
           :cash s/Num})

(def Game {:key s/Str
           :title s/Str
           :start-date java.util.Date
           :close-date (s/maybe java.util.Date)
           :tutor s/Str
           :teams [Team]})

(def game-collection "game")

(s/defn find-all-by-tutor :- [Game] [tutor :- s/Str]
  (db/find-all game-collection {:tutor tutor}))