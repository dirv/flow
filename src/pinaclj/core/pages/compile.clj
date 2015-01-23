(ns pinaclj.core.pages.compile
  (:require [pinaclj.core.files :as files]
            [pinaclj.core.pages.read :as rd]
            [markdown.core :as markdown]))

(defn- render-markdown [page]
  (assoc page :content (markdown/md-to-html-string (:content page))))

(defn- render [page-path template]
  (->> page-path
       rd/read-page
       render-markdown
       template
       (apply str)))

(def build-destination
  (comp files/change-extension-to-html files/change-root))

(defn- compile-page [src dest page template]
  (files/create (build-destination src dest page)
                (render page template)))

(defn compile-all [src dest template]
  (doseq [page (files/all-in src)]
    (compile-page src dest page template)))
