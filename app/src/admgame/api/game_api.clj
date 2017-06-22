(ns admgame.api.game-api
  (:require [admgame.model.token :refer [validate-tutor-token]]
            [admgame.middleware.authorization-handler :refer [check-tutor-auth!]]))

(defn list-all-by-tutor [req]
  (check-tutor-auth! (-> req :params :tutorid) req)
  {:status 200 :body {:status "ok"}})

(defn find-by-tutor-and-id [req]
  (check-tutor-auth! (-> req :params :tutorid) req)
  {:status 200 :body {:status "ok"}})

(defn save-or-update-game [req]
  (check-tutor-auth! (-> req :params :tutorid) req)
  {:status 200 :body {:status "ok"}})