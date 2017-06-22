(ns admgame.cryptography
  (:require [crypto.password.bcrypt :as crypto]))

(defn encrypt! [string]
  (crypto/encrypt string))

(defn check [raw encrypted]
  (crypto/check raw encrypted))
