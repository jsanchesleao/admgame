(ns admgame.cryptography
  (:require [crypto.password.bcrypt :as crypto]
            [crypto.random :as random]))

(def salt-size 10)

(defn gen-salt! []
  (random/base64 salt-size))

(defn encrypt! [string]
  (let [salt (gen-salt!)
        hash (crypto/encrypt string)]
    (str salt hash)))

(defn check [raw encrypted]
  (let [salt (subs encrypted 0 salt-size)
        hash (subs encrypted salt-size)]
    (crypto/check (str salt raw) hash)))
