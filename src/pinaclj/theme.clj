(ns pinaclj.theme
  (:require [pinaclj.templates :as t]
            [pinaclj.page :as page]
            [pinaclj.nio :as nio]
            [pinaclj.files :as f]))

(def default-category :post)

(defn- category-template [category]
  (keyword (str (name category) ".html")))

(defn- load-template [fs page-path]
  (t/build-template (f/read-stream fs page-path)))

(defn get-template [theme n]
  (get theme n))

(defn root-pages [theme]
  (clojure.set/difference
    (set (keys theme))
    #{(category-template default-category)}))

(defn- to-template [fs root-path file-path]
  {(keyword (.toString (nio/relativize (f/resolve-path fs root-path) file-path)))
   (load-template fs file-path)})

(defn- choose-files [fs path]
  (filter #(contains? #{".html" ".xml"} (f/extension %))
          (f/all-in (f/resolve-path fs path))))

(defn build-theme [fs path]
  (apply merge (map #(to-template fs path %) (choose-files fs path))))

(def to-template-path
  (comp f/remove-extension f/trim-url))

(defn- find-template? [theme path-str]
  (let [template-key (keyword path-str)]
    (when (get theme template-key)
      template-key)))

(defn- category-template? [theme page]
  (when-let [category (page/retrieve-value page :category {})]
    (find-template? theme (str (name category) ".html"))))

(defn determine-template [theme page]
  (let [template-path (to-template-path (.toString (:path page)))]
    (or (find-template? theme template-path)
        (find-template? theme (str template-path ".html"))
        (category-template? theme page)
        (category-template default-category))))
