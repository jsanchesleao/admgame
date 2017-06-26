(ns admgame.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [admgame.api.tutor-api :as tutor]
            [admgame.api.game-api :as game]
            [admgame.api.login-api :as login]
            [admgame.middleware.exception-handler :refer [wrap-exception-handling]]
            [admgame.middleware.authorization-handler :refer [wrap-authorization-handling]]
            [ring.middleware.json :refer [wrap-json-response
                                          wrap-json-body]]
            ))

(defn default-handler [request]
  {:status 500
   :body (str "Not implemented: " (:uri request))})

(defroutes app-routes
  ;DATA ACCESS ENDPOINTS
  (GET "/tutor/:id" req (tutor/get-by-id req))
  (PUT "/tutor/:id" req (tutor/save req))

  (GET "/tutor/:tutorid/game" req (game/list-all-by-tutor req))
  (GET "/tutor/:tutorid/game-dashboard" req (default-handler req))
  (GET "/tutor/:tutorid/game/:gameid" req (game/find-by-tutor-and-id req))
  (POST "/tutor/:tutorid/game" req (game/create-game req))

  (GET "/tutor/:tutorid/game/:gameid/team" req (game/find-teams req))
  (GET "/tutor/:tutorid/game/:gameid/team/:teamid" req (game/find-team-by-id req))
  (POST "/tutor/:tutorid/game/:gameid/team" req (game/create-team req))


  ;ACTION ENDPOINTS
  (POST "/action/login-tutor" req (login/do-tutor-login req))
  (POST "/action/login-team" req (login/do-team-login req))
  (POST "/action/logout" req (default-handler req))
  (POST "/action/buy-product" req (default-handler req))
  (POST "/action/buy-wrapper" req (default-handler req))
  (POST "/action/sell-product" req (default-handler req))
  (POST "/action/open-game" req (default-handler req))
  (POST "/action/run-cycle" req (default-handler req))
  (POST "/action/close-game" req (default-handler req))
  

  ;RESOURCE ENDPOINTS
  (GET "/" [] (resp/resource-response "index.html" {:root "public"}))
  (route/resources "/")

  (route/not-found (assoc (resp/resource-response "404.html" {:root "public"})
                          :status 404)))

(def app (-> app-routes
             (wrap-json-body {:keywords? true :bigdecimals? true})
             wrap-json-response
             wrap-authorization-handling
             wrap-exception-handling))