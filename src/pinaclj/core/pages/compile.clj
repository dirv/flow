(ns pinaclj.core.pages.compile
  (:require [pinaclj.core.files :as files]
            [pinaclj.core.nio :as nio]
            [pinaclj.core.pages.read :as rd]
            [markdown.core :as markdown]))

(defn- remove-extension [path]
  (subs path 0 (.lastIndexOf path ".")))

(defn- change-extension-to-html [path]
  (str (remove-extension (.toString path)) ".html"))

(defn- change-root [src dest path]
  (nio/resolve dest (nio/relativize src path)))

(defn- render-markdown [page]
  (assoc page :content (markdown/md-to-html-string (:content page))))

(defn- render [page-path template]
  (->> page-path
       rd/read-page
       render-markdown
       template
       (apply str)))

(defn- compile-all [source-dir destination-dir template]
  (doseq [page (files/all-in source-dir)]
    (files/create (change-extension-to-html (change-root source-dir destination-dir page))
                  (render page template))))

(defn run [source-dir destination-dir template]
  (compile-all (files/resolve-path source-dir)
               (files/resolve-path destination-dir)
               template))
