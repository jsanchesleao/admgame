(ns admgame.model.game-events
  (:require [schema.core :as s]
            [admgame.database :as db]
            [admgame.cryptography :as crypto]))

(s/defn game-aggregate-id :- s/Str [tutor :- s/Str, game-key :- s/Str]
  (str "/tutor/" tutor "/game/" game-key))

(s/defn game-tutor-aggregate-id :- s/Str [tutor :- s/Str]
  (str "/tutor/" tutor "/game"))

(defn tutor-create-game-event [{:keys [tutor title game-key]}]
  (let [event-id  (str "tutor-create-game:" game-key)
        aggregate-id   (game-tutor-aggregate-id tutor)
        payload        {:type "tutor-create-game" :tutor tutor :title title :key game-key}]
    (db/save-event! event-id aggregate-id payload)))

(defn create-game-event [{:keys [tutor title game-key]}]
  (let [event-id  (str "create-game:" game-key)
        aggregate-id   (game-aggregate-id tutor game-key)
        payload        {:type "create-game" :tutor tutor :title title :key game-key}]
    (db/save-event! event-id aggregate-id payload)))

(defn create-team-event [{:keys [tutor game-key team-key team-name password]}]
  (let [event-id     (str "create-game-team:" team-key)
        aggregate-id (game-aggregate-id tutor game-key)
        payload      {:type "create-team" 
                      :key team-key 
                      :name team-name 
                      :password (crypto/encrypt! password)}]
    (db/save-event! event-id aggregate-id payload)))