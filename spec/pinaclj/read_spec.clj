(ns pinaclj.read-spec
  (:require [speclj.core :refer :all]
            [pinaclj.read :refer :all]
            [pinaclj.date-time :as date-time]
            [pinaclj.files :as files]
            [pinaclj.test-fs :as test-fs]))

(def published-at
  (date-time/make 2014 10 31 10 5 0))

(def test-pages
  [{:path "first"
    :content "title: Test\npublished-at: 2014-10-31T10:05:00Z\n---\ncontent body"}
   {:path "second"
    :content "title: foo\nhello: World\n---\none\ntwo" }])

(defn do-read [fs path-str]
  (read-page (files/resolve-path fs path-str)))

(describe "read-page"
  (with fs (test-fs/create-from test-pages))

  (it "sets the title"
    (should= "Test" (:title (do-read @fs "first"))))

  (it "sets published-at"
    (should= published-at (:published-at (do-read @fs "first"))))

  (it "sets the content"
    (should= "content body" (:content (do-read @fs "first"))))

  (it "sets content with multiple lines"
    (should= "one\ntwo" (:content (do-read @fs "second"))))

  (it "does not set published-at for unpublished pages"
    (should= nil (:published-at (do-read @fs "second"))))

  (it "includes arbitrary headers"
    (should= "World" (:hello (do-read @fs "second")))))
