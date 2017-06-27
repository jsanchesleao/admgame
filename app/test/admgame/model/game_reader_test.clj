(ns admgame.model.game-reader-test
  (:require [clojure.test :refer :all]
            [admgame.model.game-reader :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

(def non-empty-string-gen
  (gen/such-that
    #(> (count %) 0)
    gen/string-alphanumeric))

(defn tutor-create-game-event-gen [] 
  (gen/hash-map
    :type  (gen/return "tutor-create-game")
    :title non-empty-string-gen
    :key   non-empty-string-gen))

(defn game-tutor-reducer-state-gen []
  (gen/vector
    (gen/hash-map
      :title non-empty-string-gen
      :key   non-empty-string-gen)))

(defspec create-game-will-always-add-to-the-resulting-vector
  100
  (prop/for-all [state (game-tutor-reducer-state-gen)
                 event (tutor-create-game-event-gen)]
    (let [next-state (game-tutor-reducer state event)]
      (= (count next-state)
         (inc (count state))))))