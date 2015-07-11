(ns pinaclj.core-spec
  (:require [pinaclj.core :refer :all]
            [pinaclj.files :as files]
            [pinaclj.test-fs :as test-fs]
            [pinaclj.test-templates :as test-templates]
            [pinaclj.templates :as templates]
            [speclj.core :refer :all]))

(def nested-page
   {:path "pages/nested/another_post.md"
    :content "title: Nested Title\npublished-at: 2014-10-31T11:05:00Z\n---\ncontent\n"})

(def simple-page
  {:path "pages/post.md"
    :content "title: Test\npublished-at: 2014-10-31T12:05:00Z\n---\n###Markdown header\nMarkdown paragraph."})

(def draft-page
  {:path "pages/a-draft.md"
   :content "title: Not sure yet\n---\nDraft post"})

(def url-page
  {:path "pages/a-test-path.md"
   :content "title: Three\nurl: /a/blog/page.html\npublished-at: 2014-10-31T13:05:00Z\n---\nContent"})

(def url-index-page
  {:path "pages/a-wordpress-style-path.md"
   :content "title: Four\nurl: /a/blog/page/\npublished-at: 2014-10-31T14:05:00Z\n---\nContent"})

(def quote-page
  {:path "pages/quote_test.md"
   :content "published-at: 2014-10-31T15:05:00Z\n---\n'"})

(def tag-page
  {:path "pages/tag_test.md"
   :content "published-at: 2014-10-31T15:05:00Z\ntags: tagA,tagB,tagC\n---\n'"})

(def published-pages
  [nested-page simple-page url-page url-index-page quote-page tag-page])

(def all-pages
  [nested-page simple-page draft-page url-page url-index-page quote-page tag-page])

(defn- do-compile-all [fs]
  (compile-all (files/resolve-path fs "pages")
               (files/resolve-path fs "published")
               test-templates/page
               test-templates/page-list
               test-templates/feed-list))

(defn- render-page-list [fs]
  (templates/to-str (compile-all fs)))

(defn- index-contents [fs]
  (files/content (files/resolve-path fs "published/index.html")))

(defn- feed-contents [fs]
  (files/content (files/resolve-path fs "published/feed.xml")))

(describe "compile-all"
  (with fs (test-fs/create-from all-pages))

  (before (do-compile-all @fs))

  (describe "page results"

    (it "creates the file"
      (should (files/exists? (files/resolve-path @fs "published/post.html"))))

    (it "renders the title"
      (should-contain "<h1 data-id=\"title\">Test</h1>"
                      (files/content (files/resolve-path @fs "published/post.html"))))

    (it "renders the content without escaping"
      (should-contain "<h3>Markdown header</h3>"
                      (files/content (files/resolve-path @fs "published/post.html"))))

    (it "compiles files in subdirectories"
      (should (files/exists? (files/resolve-path @fs "published/nested/another_post.html"))))

    (it "does not publish drafts"
      (should-not (files/exists? (files/resolve-path @fs "published/a-draft.html"))))

    (it "uses the url header if one is present"
      (should (files/exists? (files/resolve-path @fs "published/a/blog/page.html"))))

    (it "adds html extension if it isn't present"
      (should (files/exists? (files/resolve-path @fs "published/a/blog/page/index.html"))))

    (it "transforms quotes"
      (should-contain "‘" (files/content (files/resolve-path @fs "published/quote_test.html"))))

    (it "transforms relative urls"
      (should-contain "../styles.css" (files/content (files/resolve-path @fs "published/nested/another_post.html"))))

    (it "adds tags"
      (should-contain "/tags/tagA/" (files/content (files/resolve-path @fs "published/tag_test.html")))))

  (describe "index page"
    (it "renders an index page"
      (should (files/exists? (files/resolve-path @fs "published/index.html"))))

    (it "renders right number of non-draft pages"
      (should= (count published-pages) (count (re-seq #"<a" (index-contents @fs)))))

    (it "renders page title"
      (should-contain "Nested Title" (index-contents @fs)))

    (it "orders pages in reverse chronological order"
      (should (re-find #"(?s)Four.*Three.*Test.*Nested" (index-contents @fs)))))

  (describe "feed xml"
    (it "renders a feed xml file"
      (should (files/exists? (files/resolve-path @fs "published/feed.xml"))))))

(def published-content
  "published-at: 2014-04-24T00:00:00Z\n---\n")
(def published-index-page {:path "published/index.html" :modified 1 :content ""})
(def old-page {:path "pages/old.md" :modified 1 :content published-content})
(def new-page {:path "pages/new.md" :modified 2 :content published-content})

(describe "write-single-page"
  (with fs (test-fs/create-from [published-index-page old-page new-page]))

  (before (do-compile-all @fs))

  (it "writes new page"
    (should (files/exists? (files/resolve-path @fs "published/new.html"))))

  (it "does not write old page"
    (should-not (files/exists? (files/resolve-path @fs "published/old.html")))))
