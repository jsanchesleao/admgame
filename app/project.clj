(defproject admgame "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring "1.6.1"]
                 [compojure "1.6.0"]
                 [com.novemberain/monger "3.1.0"]
                 [prismatic/schema "1.1.6"]
                 [crypto-password "0.2.0"]
                 [crypto-random "1.2.0"]
                 [bouncer "1.0.1"]
                 [slingshot "0.12.2"]
                 [ring/ring-json "0.4.0"]
                 [cheshire "5.7.1"]
                 [org.clojure/test.check "0.9.0"]
                 [environ "1.1.0"]]
  :plugins [[lein-environ "1.1.0"]]
  :main ^:skip-aot admgame.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
