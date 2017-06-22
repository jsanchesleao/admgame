(ns admgame.middleware.authorization-handler
  (:require [schema.core :as s]
            [clojure.string :refer [split join]]
            [admgame.model.token :refer [find-token valid-time?]])
  (:use [slingshot.slingshot :only [try+ throw+]]))

(defn get-bearer-token [token-string]
  (if-let [token (find-token token-string)]
    (do
      (-> token
          (dissoc :_id)
          (assoc :valid? (-> token :expiry valid-time?))
          ))
    nil))

(defn parse-authorization-header [string]
  (let [[type & parts] (split string #" ")
        data (join " " parts)]
    (case type
      "Bearer" (get-bearer-token data)
      nil)))

(defn enhance-request [request auth-header]
  (try+
    (assoc request :auth {:raw auth-header 
                          :data (parse-authorization-header auth-header)})
    (catch Exception e
      (assoc request :auth {:error e 
                            :raw auth-header}))
    (catch Object _
      (assoc request :auth {:raw auth-header}))))

(defn wrap-authorization-handling [handler]
  (fn [request]
    (let [auth-header (-> request :headers (get "authorization"))]
      (if (nil? auth-header)
        (handler (assoc request :auth {}))
        (handler (enhance-request request auth-header))))))


;utility functions
(s/defn check-tutor-auth! :- s/Any
        [tutor :- s/Str, request :- s/Any]
  (let [token (-> request :auth :data)
        type  (-> token :type)
        owner (-> token :owner)]
    (cond
      (nil? token)                (throw+ {:type :authorization-error :message "Authorization header required"})
      (not= "tutor" type)         (throw+ {:type :authorization-error :message "Token type not authorized"})
      (not= tutor (:tutor owner)) (throw+ {:type :authorization-error :message "Token owner not authorized"})
      (not (:valid? token))       (throw+ {:type :authorization-error :message "Token expired"})
      :else                       nil)))