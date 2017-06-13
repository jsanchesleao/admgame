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
