(ns pinaclj.core.pages.write
  (:require [pinaclj.core.nio :as nio]
            [pinaclj.core.templates :as templates])
  (:import (java.time Instant Month ZoneId ZonedDateTime)
           (java.time.format DateTimeFormatter)))

(defn- format-published-at [published-at]
  (.format published-at DateTimeFormatter/ISO_INSTANT))

(defmulti serialize-header-pair (fn [pair] (first pair)))

(defmethod serialize-header-pair :published-at [pair]
  (str "published-at: " (format-published-at (second pair)) "\n"))

(defmethod serialize-header-pair :default [pair]
  (str (name (first pair)) ": " (second pair) "\n"))

(defn- serialize-headers [headers]
  (apply str (map serialize-header-pair (vec headers))))

(defn- serialize [{:keys [headers content]}]
  (str (serialize-headers headers)
       "\n"
       content))

(defn write-page [fs-root path page]
  (nio/create-file fs-root path (serialize page)))
