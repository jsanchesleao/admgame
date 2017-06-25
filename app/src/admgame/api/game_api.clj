(ns admgame.api.game-api
  (:require [admgame.model.token :refer [validate-tutor-token]]
            [admgame.middleware.authorization-handler :refer [check-tutor-auth!]]
            [admgame.model.game :as game]))

(defn list-all-by-tutor [req]
  (let [tutor (-> req :params :tutorid)]
    (check-tutor-auth! tutor req)
    {:status 200 
     :body {:games (game/find-all-by-tutor tutor)}}))

(defn find-by-tutor-and-id [req]
  (check-tutor-auth! (-> req :params :tutorid) req)
  {:status 200 :body {:status "ok"}})

(defn save-or-update-game [req]
  (check-tutor-auth! (-> req :params :tutorid) req)
  {:status 200 :body {:status "ok"}})