(ns pinaclj.transforms.templated-content-spec
  (require [speclj.core :refer :all]
           [net.cgrand.enlive-html :as html]
           [pinaclj.transforms.transforms :as transforms]
           [pinaclj.transforms.templated-content :refer :all]))

(def quote-page
  {:content "'"})

(def nested-page
  (transforms/apply-all {:url "nested/test.html"
                         :content (html/html-snippet "<link src=styles.css />")}))

(def template
  {:template-fn (fn [pages] (fn [x] (:content x)))})

(def opts
  {:template template :all-pages {}})

(describe "do-template"
  (it "transforms quotes"
    (should= "‘" (do-template quote-page opts)))

  (it "transforms relative urls"
    (should-contain "../styles.css" (do-template nested-page opts))))
