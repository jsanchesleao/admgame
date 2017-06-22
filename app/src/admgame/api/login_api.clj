(ns admgame.api.login-api
  (:require [admgame.api.validation :refer [defvalidator]]
            [admgame.model.tutor :as tutor-model]
            [admgame.model.token :as token-model]
            [admgame.cryptography :as crypto]
            [bouncer.validators :as v]))

(defvalidator validate-tutor-login
  :username [[v/required :message "username is required"]
             [v/string :message "username must be a string"]]
  :password [[v/required :message "password is required"]
             [v/string :message "password must be a string"]])

(def authentication-error
  {:status 401 :body {:authenticated false :message "username and password don't match"}})

(defn create-and-send-tutor-token [tutor]
  (let [token-string (token-model/emit-tutor-token (:username tutor))]
    {:status 200
     :body {:authenticated true
            :token token-string}}))

(defn perform-tutor-login! [tutor password]
  (if (crypto/check password (:password tutor))
    (create-and-send-tutor-token tutor)
    authentication-error))

(defn do-tutor-login [req]
  (let [body (-> req :body validate-tutor-login)
        tutor (-> body :username tutor-model/find-tutor-by-id)]
    (if (nil? tutor)
      {:status 404 :body {:message "username not found"}}
      (perform-tutor-login! tutor (:password body)))))