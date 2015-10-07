(ns pinaclj.transforms.total-child-pages-spec
  (:require [speclj.core :refer :all]
            [pinaclj.transforms.transforms :as transforms]
            [pinaclj.transforms.total-child-pages :refer :all]))


(def no-child-pages {})

(def two-children
  (transforms/apply-all {:pages ["a" "b"]}))

(def author-pages
  [{:destination "a" :author "wayne"}
   {:destination "b" :author "garth"}
   {:destination "c" :author "wayne"}])

(def author-parent-page
  (transforms/apply-all {:title "wayne" :destination "wayne" :category "author"}))

(def author-page-map
  (apply merge (map #(hash-map (:destination %) %) author-pages)))

(describe "calculate-total-child-pages"
  (it "returns 0 if :pages is not set"
    (should= 0 (calculate-total-child-pages no-child-pages {})))
  (it "returns count if :pages is set"
    (should= 2 (calculate-total-child-pages two-children {:max-pages "5"})))
  (it "uses :page-list calculation to compute children"
    (should= 2 (calculate-total-child-pages author-parent-page {:all-pages author-page-map}))))

