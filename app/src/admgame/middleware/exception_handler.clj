(ns admgame.middleware.exception-handler
  (:require [cheshire.core :as json])
  (:use [slingshot.slingshot :only [try+]]))

(def headers
  {"Content-Type" "application/json"})

(defn wrap-exception-handling [handler]
  (fn [request]
    (try+
      (handler request)
      (catch [:type :validation-error] {:keys [error]}
        {:status 400 :headers headers :body (json/generate-string {:validation-errors error})})
      
      (catch [:type :authorization-error] {:keys [message]}
        {:status 401 :headers headers :body (json/generate-string {:message message})})
      
      (catch [:type :game-with-existing-key] {:keys [type message]}
        {:status 422 :headers headers :body (json/generate-string {:type type :message message})})

      (catch [:type :game-not-found] {:keys [type message]}
        {:status 404 :headers headers :body (json/generate-string {:type type :message message})})
      
      (catch [:type :team-with-existing-key] {:keys [type message]}
        {:status 422 :headers headers :body (json/generate-string {:type type :message message})})
      
      (catch Exception e
        (.printStackTrace e)
        {:status 500 :headers headers :body (json/generate-string {:message (.getMessage e)})})
      (catch Object e
        (println e)
        {:status 500 :headers headers :body (json/generate-string {:message "unknown unhandled error"})}))))