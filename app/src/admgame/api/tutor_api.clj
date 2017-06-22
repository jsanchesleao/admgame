(ns admgame.api.tutor-api
  (:require [admgame.model.tutor :as model]
            [cheshire.core :as json]
            [admgame.api.validation :refer [defvalidator]]
            [bouncer.validators :as v]))

(defvalidator validate-tutor
  :username [[v/required :message "username is required"] 
             [v/string :message "username must be a string"]]
  :password [[v/required :message "password is required"]
             [v/string :message "password must be a string"]]
  :fullname [[v/required :message "fullname is required"]
             [v/string :message "fullname must be a string"]])

(defn save [req]
  (let [body (-> req :body validate-tutor)
        save-result (model/save-tutor body)]
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