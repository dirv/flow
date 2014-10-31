(ns flow.core.test-fs
  (:require [speclj.core :refer :all]
            [flow.core.nio :as nio])
  (:import (com.google.common.jimfs Jimfs Configuration)))

(def ^:const sample-pages
  [{:path "test"
    :content "Title: Test\nPublished at: 2014-10-31T10:05:00.00Z\n\ncontent body"
    :published-at 2
    }
   {:path "second"
    :content "Title: foo\nPublished at: 2014-10-30T09:00:00.00Z\n\nsecond page"
    :published-at 1}])

(def test-fs
  (Jimfs/newFileSystem (Configuration/unix)))

(defn create-file-system []
  (let [fs-root (nio/get-path test-fs "/work")]
    (doseq [page sample-pages]
      (let [file (nio/child-path fs-root (:path page))]
        (nio/create-file file (:content page) fs-root)
        (nio/set-last-modified file (:published-at page))))
    fs-root))
