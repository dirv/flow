(defproject pinaclj "0.1.0-SNAPSHOT"
  :description "Simple web content system"
  :url "https://github.com/dirv/pinaclj"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [com.google.jimfs/jimfs "1.0"]
                 [endophile "0.1.2"]
                 [enlive "1.1.5"]]
  :plugins [[speclj "3.1.0"]]
  :test-paths ["spec"]
  :main pinaclj.cli
  :profiles {:dev {:dependencies [[speclj "3.1.0"]]}})
