(ns pinaclj.page-spec
  (:require [speclj.core :refer :all]
            [pinaclj.page :refer :all]
            [pinaclj.files :as files]
            [pinaclj.nio :as nio]
            [pinaclj.transforms.transforms :as transforms]
            [pinaclj.test-fs :as test-fs]))

(def ^:dynamic counter 0)

(def simple-page
  { :content "test" })

(defn- x-func [page opts]
  123)

(def simple-page
  {:x 123})

(def page-with-simple-func
  (set-lazy-value {}
                  :x (fn [page opts] 123)))

(def page-with-complex-func
  (set-lazy-value {:content "test"}
                  :y (fn [page opts] (:content page))))

(def page-with-differing-func-vals
  (set-lazy-value {}
                  :x (fn [page opts] counter)))

(def page-with-both
  (set-lazy-value {:x "test"}
                  :y (fn [page opts] "test")))

(def page-with-override
  (set-lazy-value {:x "foo"}
                  :x (fn [page opts] "bar")))

(describe "retrieve-value"
  (it "retrieves a value"
    (should= 123 (retrieve-value simple-page :x)))
  (it "computes a simple value"
    (should= 123 (retrieve-value page-with-simple-func :x)))
  (it "computes a value based on page content"
    (should= "test" (retrieve-value page-with-complex-func :y)))
  (it "computes an override value"
    (should= "bar" (retrieve-value page-with-override :x)))
  (it "memoizes"
    (retrieve-value page-with-differing-func-vals :x)
    (binding [counter 1]
      (should= 0 (retrieve-value page-with-differing-func-vals :x)))))

(defn- read-file [fs path]
  (clojure.string/join "\n" (files/read-lines (nio/resolve-path fs path))))

(defn- file-exists? [fs path]
  (nio/exists? (nio/resolve-path fs path)))

(describe "write"
  (with fs (test-fs/create-from []))

  (def page-to-write
    {:path "pages/test.md"
     :raw-content "test"
     :a "a"
     :b "b"})

  (before (write-page page-to-write @fs))

  (it "writes page to disk"
    (should (file-exists? @fs "pages/test.md")))
  (it "writes correct content to disk"
    (should (.endsWith (read-file @fs "pages/test.md") "---\ntest")))
  (it "writes all headers"
    (should-contain "a: a\n" (read-file @fs "pages/test.md"))
    (should-contain "b: b\n" (read-file @fs "pages/test.md")))
  (it "does not write out path or raw-content as headers"
    (should-not-contain "path: " (read-file @fs "pages/test.md"))
    (should-not-contain "raw-content: " (read-file @fs "pages/test.md")))

  (def previously-written-page
    {:path "pages/previous.md"
     :raw-content "test"
     :a "a" :b "b" :c "c"
     :read-headers [:b :a]})

  (before (write-page previously-written-page @fs))

  (it "writes previously written headers first and in order"
    (should (re-find #"(?m)b:.*\na:.*\nc:" (read-file @fs "pages/previous.md")))))

(describe "parents"
  (def parent {:val "test-val" :url "parent.html"})
  (def category {:url "category/a/index.html" :val "category-val"})
  (def page-with-parent {:parent "parent.html" :url "child.html"})
  (def child-with-category {:category :a :url "child.html"})
  (def child-with-override {:parent "parent.html" :val "override-val" :url "override.html"})
  (def index {:url "index.html"})
  (def child-as-parent {:parent "child.html" :url "parent.html"})

  (defn- opts [ps]
    {:all-pages (apply merge (map #(hash-map (:url %) %) ps))})

  (defn- retrieve-val [page pages]
    (retrieve-value (transforms/apply-all page)
                    :val
                    (opts (map transforms/apply-all pages))))

  (describe "retrieve-value"
    (it "retrieves parent value when :parent set"
      (should= "test-val" (retrieve-val page-with-parent [parent])))
    (it "retrieves value when :category set"
      (should= "category-val" (retrieve-val child-with-category [parent category])))
    (it "retrieves override value when one is set"
      (should= "override-val" (retrieve-val child-with-override [parent page-with-parent])))
    (it "stops at index root page"
      (should= nil (retrieve-val index [index])))
    (it "handles cycles"
      (should= nil (retrieve-val child-as-parent [page-with-parent child-as-parent])))))

(def page-set (transforms/apply-all {:path "index.md"}))

(def just-page-set-page {"index.html" page-set})

(def author-page { :category "author" :title "wayne" :destination "wayne.html"})
(def this-author-child-page { :author "wayne" :destination "child1.html"})
(def other-author-child-page { :author "garth" :destination "child2.html"})
(def author-page-set {"wayne.html" author-page
                      "child1.html" this-author-child-page
                      "child2.html" other-author-child-page})
