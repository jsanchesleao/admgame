(ns admgame.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [admgame.handler :as handler]
            [admgame.database :as db]
            [environ.core :refer [env]])
  (:gen-class))

(defn -main
  [& args]
  (db/connect! (:mongo-uri env))
  (run-jetty handler/app {:port 3000}))

(comment 

  ;using mini event source of db

  (defn r [state event]
    (case (:type event)
      "inc"  {:value (+ (:value state) (:value event))}
      "dec"  {:value (- (:value state) (:value event))}
      state))

  (def read (db/make-reader r {:value 0})))