(ns admgame.database
  (:require [monger.core :as mg]
            [monger.query :as q]
            [monger.operators :refer [$gt $lte]]
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

(defn insert [collection document]
  (mc/insert (:db @conn) collection document))
  
(defn find-one 
  ([collection query]
    (mc/find-one-as-map (:db @conn) collection query))
  ([collection query fields]
    (mc/find-one-as-map (:db @conn) collection query fields)))

(defn find-all 
  ([collection query]
    (mc/find-maps (:db @conn) collection query))
  ([collection query fields]
    (mc/find-maps (:db @conn) collection query fields)))

(defn update-document [collection query document]
  (mc/update (:db @conn) collection query document {:multi false}))


;Event Sourcing

(def event-collection "events")
(def cache-collection "statecache")

(defn save-event! [id aggregate-id payload]
  (insert event-collection {:_id id
                            :aggregate-id aggregate-id
                            :time (java.util.Date.)
                            :payload payload}))

(defn find-events 
  ([aggregate-id]
    (find-events aggregate-id (java.util.Date.)))
  ([aggregate-id end-time]
    (find-events aggregate-id end-time (java.util.Date. 0)))
  ([aggregate-id end-time begin-time]
    (q/with-collection (:db @conn) event-collection 
      (q/find {:aggregate-id aggregate-id
               :time {$gt begin-time $lte end-time}})
      (q/fields [:aggregate-id :time :payload])
      (q/sort (array-map :time 1)))))

(defn read-data 
  ([aggregate-id reducer initial-state]
    (read-data aggregate-id reducer initial-state (java.util.Date.)))
  ([aggregate-id reducer initial-state end-time]
    (read-data aggregate-id reducer initial-state end-time (java.util.Date. 0)))
  ([aggregate-id reducer initial-state end-time begin-time]
    (let [events (find-events aggregate-id end-time begin-time)]
      (reduce (fn [state event]
                (reducer state (-> event :payload (assoc :time (:time event))))) 
              initial-state events))))

(defn find-cache [aggregate-id time]
  (first
    (q/with-collection (:db @conn) cache-collection
      (q/find {:aggregate-id aggregate-id
              :time {$lte time}})
      (q/fields [:time :state])
      (q/sort (array-map :time 1))
      (q/limit 1))))

(defn save-cache [aggregate-id reducer time initial-state]
  (let [previous     (find-cache aggregate-id time)
        events-begin (if (nil? previous) (java.util.Date. 0) (:time previous))
        events       (find-events aggregate-id time events-begin)
        state        (if (nil? previous) initial-state (:state previous))
        cache-state  (reduce reducer events state)
        cache-time   (-> events last :time)]
    (insert cache-collection {:state state :time cache-time})))

(defn read-aggregate 
  ([aggregate-id reducer initial-state]
    (read-aggregate aggregate-id reducer initial-state (java.util.Date.)))
  ([aggregate-id reducer initial-state time]
    (let [cache (find-cache aggregate-id time)]
      (if (nil? cache)
        (read-data aggregate-id reducer initial-state time)
        (read-data aggregate-id reducer (:state cache) time (:time cache))))))

(defn make-reader [reducer initial-state]
  (fn ([aggregate-id]      (read-aggregate aggregate-id reducer initial-state))
      ([aggregate-id time] (read-aggregate aggregate-id reducer initial-state time))))