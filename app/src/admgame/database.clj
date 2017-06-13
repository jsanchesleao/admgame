(ns admgame.database
  (:require [monger.core :as mg]
            [monger.collection :as mc]))

(def conn (atom nil))

(defn connect! [uri]
  (reset! conn (mg/connect-via-uri uri)))

(defn disconnect! []
  (when (not (nil? @conn))
    (mg/disconnect (:conn @conn))
    (reset! conn nil)))

(defn with-db [func]
  (func (:db @conn)))

(defn insert-document []
  (mc/insert-and-return (:db @conn) "documents" {:foo "bar"}))
  
