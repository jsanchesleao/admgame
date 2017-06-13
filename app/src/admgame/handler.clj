(ns admgame.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :as resp]))

(defn default-handler [request]
  {:status 500
   :body (str "Not implemented: " (:uri request))})

(defroutes app
  ;DATA ACCESS ENDPOINTS
  (GET "/tutor/:id" req (default-handler req))
  (PUT "/tutor/:id" req (default-handler req))

  (GET "/tutor/:tutorid/game" req (default-handler req))
  (GET "/tutor/:tutorid/game-dashboard" req (default-handler req))
  (GET "/tutor/:tutorid/game/:gameid" req (default-handler req))
  (PUT "/tutor/:tutorid/game/:gameid" req (default-handler req))

  (GET "/tutor/:tutorid/game/:gameid/team" req (default-handler req))
  (GET "/tutor/:tutorid/game/:gameid/team/:teamid" req (default-handler req))
  (PUT "/tutor/:tutorid/game/:gameid/team/:teamid" req (default-handler req))


  ;ACTION ENDPOINTS
  (POST "/action/login-tutor" req (default-handler req))
  (POST "/action/login-team" req (default-handler req))
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