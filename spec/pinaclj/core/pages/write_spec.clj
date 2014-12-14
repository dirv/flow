(ns pinaclj.core.pages.write-spec
  (:require [speclj.core :refer :all]
            [pinaclj.core.files :as files]
            [pinaclj.core.pages.write :refer :all]
            [pinaclj.core.pages.date-time :as date-time]
            [pinaclj.core.test-fs :as test-fs])
  (:import (java.time ZonedDateTime LocalDateTime Month ZoneId)))

(def published-at
  (date-time/make 2014 10 31 10 5 0))

(defn make-page [path page]
  (write-page path page)
  (files/content path))

(describe "write-page"
  (before (test-fs/create-file-system))

  (it "writes the title"
    (should-contain "title: Title" (make-page "title_page" {:headers {:title "Title"}})))

  (it "writes the published_at"
    (should-contain "published-at: 2014-10-31T10:05:00Z" (make-page "pub_page" {:headers {:published-at published-at}})))

  (it "writes the content"
    (should-contain "content yo" (make-page "content_page" {:content "content yo"})))

  (it "excludes published at if not present"
    (should-not-contain "published-at: " (make-page "pub_page2" {:content "content yo"})))

  (it "writes arbitrary headers"
    (should-contain "hello: world" (make-page "helloworld" {:content "content" :headers {:hello "world"}})))

  (it "writes page in correct format"
    (should= "title: title\npublished-at: 2014-10-31T10:05:00Z\n\ncontent"
             (make-page "format_page" {:headers {:title "title" :published-at published-at} :content "content"}))))
