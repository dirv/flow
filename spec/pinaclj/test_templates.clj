(ns pinaclj.test-templates
  (:require [pinaclj.test-fs :as test-fs]
            [pinaclj.templates :as templates]))

(defn- page-stream []
  (test-fs/resource-stream "example_theme/post.html"))

(defn- page-list-stream []
  (test-fs/resource-stream "example_theme/index.html"))

(defn- feed-stream []
  (test-fs/resource-stream "example_theme/feed.xml"))

(defn- func-params-stream []
  (test-fs/resource-stream "example_theme/func_params.html"))

(defn- split-list-stream []
  (test-fs/resource-stream "example_theme/split_list.html"))

(defn write-to-fs [fs]
  (doall (map #(test-fs/write-stream-file fs %) [["/theme/post.html" (page-stream)]
                                          ["/theme/index.html" (page-list-stream)]
                                           ["/theme/feed.xml" (feed-stream)]])))

(defn write-split-to-fs [fs]
  (doall (map #(test-fs/write-stream-file fs %) [["/theme/post.html" (page-stream)]
                                          ["/theme/index.html" (split-list-stream)]
                                           ["/theme/feed.xml" (feed-stream)]])))

(def split-list
  (templates/build-template (split-list-stream)))

(def page-list
  (templates/build-template (page-list-stream)))

(def feed-list
  (templates/build-template (feed-stream)))

(def page
  (templates/build-template (page-stream)))

(def func-params
  (templates/build-template (func-params-stream)))

