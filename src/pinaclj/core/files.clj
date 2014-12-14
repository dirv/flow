(ns pinaclj.core.files
  (:require [pinaclj.core.nio :as nio]))

(defn- as-bytes [st]
  (bytes (byte-array (map byte st))))

(def fs-root (atom ""))

(defn init [filesystem root]
  (reset! fs-root (nio/get-path filesystem root)))

(defn read-lines [path]
  (nio/read-all-lines @fs-root path))

(defn content [path]
  (clojure.string/join "\n" (read-lines path)))

(defn relativize [path]
  (nio/relativize @fs-root path))

(defn create [path content]
  (nio/create-file @fs-root path (as-bytes content)))

(defn all []
  (nio/get-all-files @fs-root))
