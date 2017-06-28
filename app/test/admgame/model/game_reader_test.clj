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
    :tutor non-empty-string-gen
    :title non-empty-string-gen
    :key   non-empty-string-gen))

(defn game-tutor-reducer-state-gen []
  (gen/vector
    (gen/hash-map
      :title non-empty-string-gen
      :key   non-empty-string-gen)))

(defn create-game-event-gen [] 
  (gen/hash-map
    :type  (gen/return "create-game")
    :tutor non-empty-string-gen
    :title non-empty-string-gen
    :key   non-empty-string-gen))

(defn create-team-event-gen []
  (gen/hash-map
    :type (gen/return "create-team")
    :key  non-empty-string-gen
    :name non-empty-string-gen
    :password non-empty-string-gen))

(def nil-gen (gen/return nil))

(defn key-does-not-repeat [items]
  (let [keys (->> items (map :key) (into #{}))]
    (= (count items) (count keys))))

(defn game-team-state-gen []
  (gen/hash-map
    :key non-empty-string-gen
    :name non-empty-string-gen
    :password non-empty-string-gen
    :stock (gen/hash-map
              :product gen/nat
              :wrapper gen/nat)
    :cash gen/nat))

(defn game-reducer-state-gen []
  (gen/hash-map
    :key non-empty-string-gen
    :title non-empty-string-gen
    :tutor non-empty-string-gen
    :teams (gen/such-that
             key-does-not-repeat
             (gen/vector (game-team-state-gen)))))

(defspec tutor-create-game-will-always-add-to-the-resulting-vector
  100
  (prop/for-all [state (game-tutor-reducer-state-gen)
                 event (tutor-create-game-event-gen)]
    (let [next-state (game-tutor-reducer state event)]
      (= (count next-state)
         (inc (count state))))))

(defspec create-game-initializes-the-state-with-correct-data 
  100
  (prop/for-all [state (gen/return nil)
                 event (create-game-event-gen)]
    (let [next-state (game-reducer state event)]
      (and
        (not (nil? next-state))
        (= (:title event) (:title next-state))
        (= (:key event) (:key event))
        (= 0 (count (:teams event)))))))

(defspec create-game-does-nothing-if-game-is-already-created 
  10
  (prop/for-all [state (game-reducer-state-gen)
                 event (create-game-event-gen)]
    (let [next-state (game-reducer state event)]
      (= state next-state))))

(defspec create-team-adds-the-newly-created-team-to-the-state
  100
  (prop/for-all [state (game-reducer-state-gen)
                 event (create-team-event-gen)]
    (as-> (game-reducer state event) x
         (:teams x)
         (map :key x)
         (into #{} x)
         (apply x [(:key event)])
         (boolean x))))