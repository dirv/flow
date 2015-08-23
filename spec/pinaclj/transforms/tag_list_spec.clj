(ns pinaclj.transforms.tag-list-spec
  (:require [speclj.core :refer :all]
            [pinaclj.group :as group]
            [pinaclj.transforms.tag-list :refer :all]))

(def page
  {:tags '(:tagA :tagB)})

(def tag-page-A (group/tag-url :tagA))

(describe "get-tags"
  (it "returns right number of tag pages"
    (should= 2 (count (:pages (get-tags page {})))))
  (it "returns correct page"
    (should= tag-page-A
      (first (:pages (get-tags page {}))))))

