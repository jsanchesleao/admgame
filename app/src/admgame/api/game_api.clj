(ns admgame.api.game-api
  (:require [admgame.model.token :refer [validate-tutor-token]]
            [admgame.api.validation :refer [defvalidator]]
            [bouncer.validators :as v]
            [admgame.middleware.authorization-handler :refer [check-tutor-auth! check-tutor-or-team-auth!]]
            [admgame.model.game-es :as game]))

(defvalidator validate-game-creation-request
  :title  [[v/required :message "title is required"]
           [v/string :message "title must be a string"]])

(defvalidator validate-team-creation-request
  :name     [[v/required :message "title is required"]
             [v/string :message "title must be a string"]]
  :password [[v/required :message "password is required"]
             [v/string :message "password must be a string"]])

(defn list-all-by-tutor [req]
  (let [tutor (-> req :params :tutorid)]
    (check-tutor-auth! tutor req)
    {:status 200 
     :body {:games (game/find-all-by-tutor tutor)}}))

(defn find-by-tutor-and-id [req]
  (let [tutor (-> req :params :tutorid)
        key   (-> req :params :gameid)]
    (check-tutor-auth! tutor req)
    (let [game-data (game/find-by-tutor-and-key tutor key)]
      (if (nil? game-data)
        {:status 404 :body {:message "game not found"}}
        {:status 200 :body game-data}))))

(defn create-game [req]
  (check-tutor-auth! (-> req :params :tutorid) req)
  (let [body (-> req :body validate-game-creation-request)
        tutor (-> req :params :tutorid)
        create-request (assoc body :tutor tutor)
        result (game/create-game create-request)]
    {:status 201 :body result}))

(defn find-teams [req]
  (let [tutor (-> req :params :tutorid)
        key   (-> req :params :gameid)]
    (check-tutor-auth! tutor req)
    (let [game-data (game/find-by-tutor-and-key tutor key)]
      (if (nil? game-data)
        {:status 404 :body {:message "game not found"}}
        {:status 200 :body {:teams (:teams game-data)}}))))

(defn- get-team-from-game [game-data team]
  (let [teams  (:teams game-data)
        result (->> teams
                (filter #(= team (:key %)))
                first)]
    (if (nil? result)
      {:status 404 :body {:message "team not found"}}
      {:status 200 :body result})))

(defn find-team-by-id [req]
  (let [tutor (-> req :params :tutorid)
        game-key   (-> req :params :gameid)
        team  (-> req :params :teamid)]
    (check-tutor-or-team-auth! tutor game-key team req)
    (let [game-data (game/find-by-tutor-and-key tutor game-key)]
      (if (nil? game-data)
        {:status 404 :body {:message "game not found"}}
        (get-team-from-game game-data team)))))

(defn create-team [req]
  (check-tutor-auth! (-> req :params :tutorid) req)
  (let [tutor (-> req :params :tutorid)
        key   (-> req :params :gameid)
        body  (-> req :body validate-team-creation-request)
        create-request {:tutor tutor
                        :game-key key
                        :team-name (:name body)
                        :password  (:password body)}
        result (game/create-team create-request)]
    {:status 201 :body result}))