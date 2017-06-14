(ns admgame.exception-handler
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
      (catch Exception e
        {:status 500 :headers headers :body (json/generate-string {:message (.getMessage e)})})
      (catch Object _
        {:status 500 :headers headers :body (json/generate-string {:message "unknown unhandled error"})}))))