(ns pinaclj.children
  (:require [pinaclj.page :as page]
            [pinaclj.transforms.category :as category]))

(defn- except-page [page all-pages]
  (dissoc all-pages (page/retrieve-value page :destination {})))

(defn- filter-to-category [all-pages {category :category}]
  (if category
    (filter #(= (keyword category) (page/retrieve-value (val %) :category {})) all-pages)
    all-pages))

(defn- sort-pages [all-pages order-by reverse?]
  (let [sorted (sort-by #(get (val %) order-by) all-pages)]
    (if reverse? (reverse sorted) sorted)))

(defn- matches? [k v page]
  (= v (page/retrieve-value page k {})))

(defn- filter-to-parent [pages parent]
 (let [category (keyword (page/retrieve-value parent :category {}))
       title (page/retrieve-value parent :title {})]
    (if (= category/default-category category)
      pages
      (filter #(matches? category title (val %)) pages))))

(defn- order-key [{order-key :order-by}]
  (if order-key (keyword order-key) :published-at))

(defn- reverse? [{order-key :order-by reverse? :reverse}]
  (or (nil? order-key) reverse?))

(defn- remove-generated [pages]
  (remove #(:generated (val %)) pages))

(defn- to-urls [pages]
  (map key pages))

(defn children [page list-node-attrs all-pages]
  (-> page
      (except-page all-pages)
      (remove-generated)
      (filter-to-category list-node-attrs)
      (filter-to-parent page)
      (sort-pages (order-key list-node-attrs) (reverse? list-node-attrs))
      (to-urls)))