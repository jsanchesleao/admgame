(ns admgame.model.token
  (:require [schema.core :as s]
            [crypto.random :as random]
            [admgame.database :as db]))

;Schema Definitions
(def Token {:string s/Str
            :type (s/enum "tutor" "team")
            :expiry s/Num
            :owner {
              :tutor s/Str
              :game (s/maybe s/Str)
              :team (s/maybe s/Str)}})

(def token-collection "token")

(s/defn generate-token-string :- s/Str []
  (random/base64 64))

(s/defn current-time :- s/Num []
  (quot (System/currentTimeMillis) 1000))

(s/defn expiry-time :- s/Num []
  (+ (* 60 30)
     (current-time)))

(s/defn valid-time? :- s/Bool [expiry]
  (> expiry (current-time)))

(s/defn create-tutor-token :- Token 
        [username :- s/Str]
  {:string (generate-token-string)
   :type "tutor"
   :expiry (expiry-time)
   :owner {:tutor username}})

(s/defn emit-tutor-token :- s/Str
        [username :- s/Str]
  (let [token (create-tutor-token username)]
    (db/insert-document token-collection token)
    (:string token)))

(s/defn find-token :- (s/maybe Token)
        [token-string :- s/Str]
  (db/find-one token-collection {:string token-string}))

(s/defn refresh-token! :- s/Any
        [token :- Token]
  (db/update-document token-collection 
    {:string (:string token)}
    (assoc Token :expiry (expiry-time))))

(s/defn validate-tutor-token :- s/Bool
        [token-string :- s/Str
         owner :- s/Str]
  (let [token (find-token token-string)]
    (cond
      (nil? token) false
      (not= (-> token :owner :tutor) owner) false
      (valid-time? (:expiry token)) (do (refresh-token! token) true)
      :else false)))