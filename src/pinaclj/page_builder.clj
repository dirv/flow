(ns pinaclj.page-builder
  (:require [pinaclj.page :as page]
            [pinaclj.date-time :as dt]
            [pinaclj.transforms.transforms :as transforms]
            [pinaclj.tag-page :as tp]))

(defn create-page [src path]
  (transforms/apply-all {:path path :src-root src}))

(defn- create-list-page [pages url]
  {:pages pages
   :raw-content ""
   :url url
   :modified (System/currentTimeMillis)
   :published-at (dt/now) })

(defn build-list-page [pages url]
  (transforms/apply-all (create-list-page pages url)))

(defn- build-tag-page [[tag pages]]
  {tag (assoc (build-list-page pages (str "tags/" (name tag) "/"))
         :title (name tag))})

(defn build-tag-pages [pages]
  (apply merge (map build-tag-page (tp/pages-by-tag pages))))

(defn- page-tags [page]
  (map keyword (page/retrieve-value page :tags {})))

(defn attach-tag-pages [pages tag-pages]
  (map #(assoc % :tag-pages
               (select-keys tag-pages (page-tags %))) pages))

(defn- split-page-url [page]
  (.split (page/retrieve-value page :destination {}) "\\."))

(defn- create-url [page num]
  (cond
    (= num 0) (:url page)
    (and (> num 0) (< num (dec (count (:pages page)))))
    (let [[start ext] (split-page-url page)]
      (str start "-" (inc num) "." ext))))

(defn- duplicate-page [page start num-pages]
  (let [page-num (/ start num-pages)]
    (assoc page
         :start start
         :raw-content ""
         :url (create-url page page-num)
         :pages (take num-pages (drop start (:pages page)))
         :previous (create-url page (dec page-num))
         :next (create-url page (inc page-num)))))

(defn divide [page {max-pages :max-pages}]
  (if (nil? max-pages)
    [page]
    (let [starts (range 0 (count (:pages page)) max-pages)]
      (map #(duplicate-page page % max-pages) starts))))
