(ns pinaclj.tasks.generate-spec
  (:require [pinaclj.tasks.generate :refer :all]
            [pinaclj.test-fs :refer :all]
            [pinaclj.test-templates :as test-templates]
            [speclj.core :refer :all]
            [pinaclj.translate :refer :all]))

(def nested-page
  {:path "pages/nested/another_post.md"
   :content "title: Nested Title\npublished-at: 2014-10-31T11:05:00Z\n---\ncontent\n"})

(def simple-page
  {:path "pages/post.md"
   :content "title: Test\npublished-at: 2014-10-31T12:05:00Z\n---\ncontent"})

(def old-style-page {:path "published/styles.css" :content "old" :modified -2})
(def new-style-page {:path "published/styles.css" :content "new" :modified 5})

(defn publish-message-for [page]
  (t :en :generate/published-page page))

(defn copy-message-for [file]
  (t :en :generate/copy-file file))

(defn did-not-copy-message-for [file]
  (t :en :generate/did-not-copy-file file))

(describe "generate"
  (with fs (create-from [nested-page simple-page]))

  (before (test-templates/write-to-fs @fs))

  (describe "file results"
    (before (generate @fs "pages" "published" "theme"))

    (it "creates simple post"
      (should (file-exists? @fs "published/post.html")))

    (it "compiles files in subdirectories"
      (should (file-exists? @fs "published/nested/another_post.html")))

    (it "copies static files"
      (should (file-exists? @fs "published/styles.css"))))

  (it "overwrites old static pages"
    (create-file @fs old-style-page)
    (generate @fs "pages" "published" "theme")
    (should-not= "old" (read-file @fs "published/styles.css")))

  (it "does not overwrite unchanged static files"
    (create-file @fs new-style-page)
    (generate @fs "pages" "published" "theme")
    (should= "new" (read-file @fs "published/styles.css")))

  (describe "task output"
    (it "outputs successful pages"
      (let [output (generate @fs "pages" "published" "theme")
            messages (map :msg output)]
        (should-contain (publish-message-for "index.html") messages)
        (should-contain (publish-message-for "feed.xml") messages)
        (should-contain (publish-message-for "post.html") messages)
        (should-contain (publish-message-for "category/post/index.html") messages)
        (should-contain (publish-message-for "nested/another_post.html") messages)))
    (it "outputs copied files"
      (let [output (generate @fs "pages" "published" "theme")
            messages (map :msg output)]
        (should-contain (copy-message-for "styles.css") messages)))

    (it "outputs uncopied files"
      (create-file @fs new-style-page)
      (let [output (generate @fs "pages" "published" "theme")
            messages (map :msg output)]
        (should-contain (did-not-copy-message-for "styles.css") messages)))))
