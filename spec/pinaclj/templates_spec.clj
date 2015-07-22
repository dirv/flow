(ns pinaclj.templates-spec
  (:require [speclj.core :refer :all]
            [clojure.pprint]
            [pinaclj.transforms.transforms :as transforms]
            [pinaclj.test-templates :as test-templates]
            [pinaclj.templates :refer :all]
            [pinaclj.date-time :as date]
            [pinaclj.page :as page]))

(def pages (map transforms/apply-all
                [{:url "/1" :title "First post" :raw-content "first post content."}
             {:url "/2" :title "Second post" :raw-content "second post content." }
             {:url "/3" :title "Third post" :raw-content "<h1>third</h1> post content." :third-key "Hello, world!"
              :published-at (date/make 2014 11 30 0 0 0)
              }
            {:url "/4" :title "Fourth post" :raw-content "published"
             :published-at (date/make 2014 12 31 0 0 0)
             }]))

(def list-page
  (transforms/apply-all {:pages pages}))

(defn render-page []
  (to-str ((:template-func test-templates/page) (first pages))))

(defn render-third-page []
  (to-str ((:template-func test-templates/page) (nth pages 2))))

(defn render-page-list []
  (to-str ((:template-func test-templates/page-list) list-page)))

(defn render-split-list []
  (to-str ((:template-func test-templates/split-list) list-page)))

(defn render-feed []
  (to-str ((:template-func test-templates/feed-list) list-page)))

(defn- apply-func-a [page]
  (page/set-lazy-value page
                       :func
                       (fn [page opts] (str "format=" (:format opts)))))

(defn render-func-params-page []
  (to-str ((:template-func test-templates/func-params) (apply-func-a (first pages)))))

(describe "page list"
  (it "contains item href"
    (should-contain "href=\"/1\"" (render-page-list)))

  (it "contains title"
    (should-contain "First post" (render-page-list)))

  (it "contains correct number of items"
    (should= (count pages) (count (re-seq #"data-id=\"page-list\"" (render-page-list))))))

(describe "split list"
  (it "contains only max items"
    (should= 2 (count (re-seq #"<li" (render-split-list))))))

(describe "feed"
   (it "contains correct number of items"
     (should= (count pages) (count (re-seq #"data-id=\"page-list\"" (render-feed)))))
   (it "outputs updated date"
     (should-contain "2014-12-31T00:00:00Z</updated>" (render-feed))))

(describe "page"
  (it "renders title"
    (should-contain "First post" (render-page)))

  (it "renders content"
    (should-contain "first post content" (render-page)))

  (it "renders all keys"
    (should-contain "Hello, world!" (render-third-page))))

(describe "func params"
  (it "passes through parameters"
    (should-contain "format=123" (render-func-params-page))))
