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

(defn insert-document [collection document]
  (mc/insert-and-return (:db @conn) collection document))
  
(defn find-one [collection query]
  (mc/find-one-as-map (:db @conn) collection query))

(defn find-all [collection query]
  (mc/find-maps (:db @conn) collection query))

(defn update-document [collection query document]
  (mc/update (:db @conn) collection query document {:multi false}))