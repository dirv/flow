(ns pinaclj.site-spec
  (require [speclj.core :refer :all]
           [pinaclj.transforms.transforms :as transforms]
           [pinaclj.site :refer :all]
           [pinaclj.date-time-helpers :as dt]))

(defn- title-writing-template [all-pages]
  (fn [page]
    (apply str (map #(:title (get all-pages %)) (:pages page)))))

(def test-theme
  {:post.html {:template-fn (fn [pages] (title-writing-template pages))}
   :index.html {:template-fn (fn [pages] (title-writing-template pages))}})

(def base-page
  {:modified 2
   :category :uncategorized
   :path "/test.md"
   :published-at (dt/make 2015 1 1 1 1 1)
   :title "test"})

(defn- output-with-theme [pages theme]
  (build pages theme 1))

(defn- files-output [pages]
  (map first (output-with-theme pages test-theme)))

(defn- in [page-name pages]
  (second (first (filter #(= page-name (first %)) pages))))


(describe "new page"
  (def new-page (assoc base-page
                       :destination "new.html"
                       :title "new-page"))

  (it "writes page"
    (should-contain "new.html" (files-output [new-page])))
  (it "writes to index"
    (should-contain "new-page" (in "index.html"
                                   (output-with-theme [new-page] test-theme)))))

(describe "old page"
  (def old-page (assoc base-page
                       :modified 1
                       :destination "old.html"
                       :title "old-page"))

  (it "does not write page"
    (should-not-contain "old.html" (files-output [old-page])))
  (it "writes to index"
    (should-contain "old-page" (in "index.html"
                                   (output-with-theme [old-page] test-theme)))))

(describe "draft page"
  (def draft-page {:destination "draft.html" :title "draft"})

  (it "does not write page"
    (should-not-contain "draft.html" (files-output [draft-page])))
  (it "does not write to index"
    (should-not-contain "draft" (in "index.html"
                                    (output-with-theme [draft-page] test-theme)))))

(describe "tag page"
  (def tag-page (assoc base-page
                       :tags '("tagA" "tagB" "tagC")
                       :destination "test.html"))

  (it "creates tag pages"
    (let [pages (files-output [tag-page])]
      (should-contain "tag/tagA/index.html" pages)
      (should-contain "tag/tagB/index.html" pages)
      (should-contain "tag/tagC/index.html" pages))))

(describe "category page"
  (def category-page (assoc base-page
                            :category :a))

  (it "creates category pages"
    (should-contain "category/a/index.html" (files-output [category-page]))))

(describe "split page list"
  (def theme-with-max-page
    (assoc-in test-theme [:index.html :max-pages] 2))

  (def five-pages
    (map #(assoc base-page
                 :title %
                 :destination %) (range 1 6)))

  (defn- build-split-index []
    (output-with-theme five-pages theme-with-max-page))

  (it "splits index page"
    (should-contain "index.html" (map first (build-split-index)))
    (should-contain "index-2.html" (map first (build-split-index))))

  (it "outputs correct pages in split"
    (should= "12" (in "index.html" (build-split-index)))
    (should= "34" (in "index-2.html" (build-split-index)))
    (should= "5" (in "index-3.html" (build-split-index)))))

(describe "source and theme matching"
  (defn- this-page-template [all-pages]
    (fn [page] (:title page)))

  (def match-theme
    {:post.html {:template-fn (fn [pages] (this-page-template pages))}
     :index.html {:template-fn (fn [pages] (this-page-template pages))}})

  (def index-page
    (transforms/apply-all (assoc base-page :title "test-title"
                                 :generated true
                                 :path "index.md"
                                 :url "index.html")))

  (it "matches source file with theme file"
    (should= "test-title" (in "index.html" (output-with-theme [index-page] match-theme)))))
