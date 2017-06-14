(ns admgame.api.tutor-api
  (:require [admgame.model.tutor :as model]
            [cheshire.core :as json]
            [bouncer.core :as b]
            [bouncer.validators :as v])
  (:use [slingshot.slingshot :only [throw+]]))

(defn validate-tutor [data]
  (b/validate data
    :username [[v/required :message "username is required"] 
               [v/string :message "username must be a string"]]
    :password [[v/required :message "password is required"]
               [v/string :message "password must be a string"]]
    :fullname [[v/required :message "fullname is required"]
               [v/string :message "fullname must be a string"]]))

(defn validate [data]
  (let [[error _] (validate-tutor data)]
    (if (nil? error)
      data
      (throw+ {:type :validation-error :error error}))))

(defn save [req]
  (let [raw-body (-> req :body slurp)
        parsed-body (validate (json/parse-string raw-body true))
        save-result (model/save-tutor parsed-body)]
    (case (:status save-result)
      :success {:status 200 :body {:success true}}
      :failure {:status 422 :body {:success false :message (:message save-result)}}
      {:status 500 :body {:error "internal server error"}})))

(defn get-by-id [req]
  (let [id (-> req :params :id)
        tutor (model/find-tutor-by-id id)]
    (if (nil? tutor)
      {:status 404 :body {:error "not found"}}
      {:status 200 :body (dissoc tutor :password)})))