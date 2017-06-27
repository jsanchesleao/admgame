(ns admgame.api.login-api
  (:require [admgame.api.validation :refer [defvalidator]]
            [admgame.model.tutor :as tutor-model]
            [admgame.model.game :as game-model]
            [admgame.model.token :as token-model]
            [admgame.cryptography :as crypto]
            [bouncer.validators :as v]))

(defvalidator validate-tutor-login
  :username [[v/required :message "username is required"]
             [v/string :message "username must be a string"]]
  :password [[v/required :message "password is required"]
             [v/string :message "password must be a string"]])

(defvalidator validate-team-login
  :tutor    [[v/required :message "tutor is required"]
             [v/string :message "tutor must be a string"]]
  :game     [[v/required :message "game is required"]
             [v/string :message "game must be a string"]]
  :team     [[v/required :message "team is required"]
             [v/string :message "team must be a string"]]
  :password [[v/required :message "password is required"]
             [v/string :message "password must be a string"]])

(def authentication-error
  {:status 401 :body {:authenticated false :message "invalid login credentials"}})

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

(defn create-and-send-team-token [game team]
  (let [token-string (token-model/emit-team-token (:tutor game) (:key game) (:key team))]
    {:status 200
     :body {:authenticated true
            :token token-string}}))

(defn perform-team-login [game team-key password]
  (let [team (game-model/find-team-by-key game team-key)]
    (cond
      (nil? team) {:status 404 :message "team not found"}
      (crypto/check password (:password team)) (create-and-send-team-token game team)
      :else authentication-error)))

(defn do-team-login [req]
  (let [body (-> req :body validate-team-login)
        {:keys [tutor team game password]} body
        game-data (game-model/find-by-tutor-and-key-complete tutor game)]
    (if (nil? game-data)
      {:status 404 :body {:message "game not found"}}
      (perform-team-login game-data team password))))

(defn do-logout [req]
  (let [token (-> req :auth :data)]
    (cond
      (nil? (:string token)) {:status 200 :body {:success false}}
      (not (:valid? token))  {:status 200 :body {:success false}}
      :else                 (do
                              (token-model/invalidate-token! token)
                              {:status 200 :body {:success true}}))))