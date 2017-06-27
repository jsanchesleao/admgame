(ns admgame.model.game-reader
  (:require [admgame.model.game-events :refer [game-aggregate-id game-tutor-aggregate-id]]
            [admgame.database :as db]))

(def game-initial-state nil)
(def game-tutor-initial-state [])

(defn game-reducer [state event]
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

(defn game-tutor-reducer [state event]
  (case (:type event)
    "tutor-create-game" (let [{:keys [title key]} event]
                          (conj state {:title title :key key}))
     state))


(def tutor-game-reader (db/make-reader game-tutor-reducer game-tutor-initial-state))
(def game-reader       (db/make-reader game-reducer game-initial-state))

(defn find-all-by-tutor [tutor]
  (tutor-game-reader (game-tutor-aggregate-id tutor)))

(defn find-by-tutor-and-key [tutor key]
  (game-reader (game-aggregate-id tutor key)))