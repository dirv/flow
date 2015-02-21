(ns pinaclj.link-transform-spec
  (require [speclj.core :refer :all]
           [pinaclj.link-transform :refer :all]))

(def root-page
  {:content {:attrs {:src "index.html"}}
   :url "test.html" })

(def one-deep
  {:content {:attrs {:src "styles.css"}}
   :url "dir/test.html" })

(def two-deep
  {:content {:attrs {:src "styles.css"}}
   :url "dir/two/test.html" })

(def href
  {:content {:attrs {:href "hello.png"}}
   :url "another/page/here.html"})

(def hierarchy
  {:content {:content '({:attrs {:src "deep.png"}})}
   :url "one/two/three.html"})

(def external-site
  {:content {:attrs {:src "//another.com/page.png"}}
    :url "dir/index.html"})

(def unpack-single-node
  (comp :src :attrs :content))

(describe "transform"
  (it "does not change for root level"
    (should= "index.html"
      (unpack-single-node (transform root-page))))
  (it "changes for one level up"
    (should= "../styles.css"
      (unpack-single-node (transform one-deep))))
  (it "changes for two levels up"
    (should= "../../styles.css"
      (unpack-single-node (transform two-deep))))
  (it "changes href"
    (should= "../../hello.png"
      (:href (:attrs (:content (transform href))))))
  (it "traverses tree"
    (should= "../../deep.png"
      (:src (:attrs (first (:content (:content (transform hierarchy))))))))
  (it "does not change external pages"
    (should= "//another.com/page.png"
      (unpack-single-node (transform external-site)))))
