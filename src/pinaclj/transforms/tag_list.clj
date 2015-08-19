(ns pinaclj.transforms.tag-list
  (:require [pinaclj.group :as group]
            [pinaclj.page :as page]
            [pinaclj.transforms.transforms :as transforms]))

(defn- to-link-page [tag]
  (transforms/apply-all {:title (name tag)
                         :destination (group/tag-url tag)}))

(defn get-tags [page opts]
  {:pages (map to-link-page (page/retrieve-value page :tags {}))})

(def transform [:tag-list get-tags])
