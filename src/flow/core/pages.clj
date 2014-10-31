(ns flow.core.pages
  (:require [flow.core.nio :as nio]
            [flow.core.templates :as templates]))

(defn- extract-title [line]
  (nth (clojure.string/split line #": ") 1))

(defn to-page [path fs-root]
  (let [content-lines  (nio/read-all-lines path)]
  { :path (nio/get-path-string fs-root path)
   :content (nth content-lines 2)
   :title (extract-title ( nth content-lines 0))
   :published-at (nio/get-last-modified-time path)}))

(defn- get-all-pages [fs-root]
  (with-open [children (nio/get-all-files fs-root)]
    (vec (map #(to-page % fs-root) children))))

(defn- sort-by-descending-date [pages]
  (reverse (sort-by :published-at pages)))

(defn build-page-list [fs-root]
  (->> fs-root
       get-all-pages
       sort-by-descending-date
       templates/page-list
       (apply str)))
