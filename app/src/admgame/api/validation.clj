(ns admgame.api.validation
  (:require [bouncer.core :as b])
  (:use [slingshot.slingshot :only [throw+]]))

(defn validate [check-fn]
  (fn [data]
    (let [[error _] (check-fn data)]
      (if (nil? error)
        data
        (throw+ {:type :validation-error :error error})))))

(defmacro defvalidator [name & body]
  `(def ~name (validate (fn [data#] 
                          (b/validate data# ~@body)))))