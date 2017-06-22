(ns admgame.model.tutor
  (:require [schema.core :as s]
            [admgame.database :as db]
            [admgame.cryptography :as crypto]))

;Schema Definitions
(def Tutor {:username s/Str
            :fullname s/Str
            :password s/Str})

(def Response {:status (s/enum :success :failure)
               :message (s/maybe s/Str)})


(def tutor-collection "tutor")

(defn encrypt [password]
  (crypto/encrypt! password))

(s/defn find-tutor-by-id :- (s/maybe Tutor) 
        [id :- s/Str]
  (let [tutor (db/find-one tutor-collection {:username id})]
    (if (nil? tutor)
      tutor
      (dissoc tutor :_id))))

(s/defn save-tutor :- Response 
        [tutor :- Tutor]
  (if (nil? (find-tutor-by-id (:username tutor)))
    (do
      (db/insert-document tutor-collection {:username (:username tutor)
                                            :password (encrypt (:password tutor))
                                            :fullname (:fullname tutor)})
      {:status :success})
    {:status :failure
     :message "username taken"}))