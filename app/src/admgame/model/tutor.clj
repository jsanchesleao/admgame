(ns admgame.model.tutor
  (:require [schema.core :as s]
            [admgame.database :as db]
            [admgame.cryptography :as crypto]))

(def Tutor {:username s/Str
            :fullname s/Str
            :email s/Str})

(def tutor-collection "tutor")

(defn encrypt [password]
  (crypto/encrypt! password))

(defn find-tutor-by-id [id]
  (let [tutor (db/find-one tutor-collection {:username id})]
    (if (nil? tutor)
      tutor
      (dissoc tutor :_id))))

(defn save-tutor [tutor]
  (if (nil? (find-tutor-by-id (:username tutor)))
    (do
      (db/insert-document tutor-collection {:username (:username tutor)
                                            :password (encrypt (:password tutor))
                                            :fullname (:fullname tutor)})
      {:status :success})
    {:status :failure
     :message "username taken"}))