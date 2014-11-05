(ns flow.core.handler
  (:require
    [flow.core.pages :as pages]
    [flow.core.nio :as nio]
    [ring.util.response :as response]
    [ring.util.anti-forgery :as af]
    [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defn- not-found []
  (fn [req]
    (response/not-found "Not found")))

(defn- string-from-stream [stream]
  (.reset stream)
  (slurp stream))

(defn- post-handler [app fs-root]
  (fn [req]
    (if-not (= :post (:request-method req))
      (app req)
      (let [path (subs (:uri req) 1)
            file (nio/child-path fs-root path)]
        (nio/create-file file (string-from-stream (:body req)))
        {:status 200}))))

(defn- page-handler [app fs-root]
  (fn [req]
    (if-not (= :get (:request-method req))
      (app req)
      (let [path (subs (:uri req) 1)
            file (nio/existing-child-path fs-root path)]
        (if file
          {:status 200
           :body (nio/content file)
           :headers {"Content-Length" 0 ; todo
                     "Content-Type" 0 }}
          (app req))))))

(defn- index-handler [app fs-root]
  (fn [req]
    (if (and (= :get (:request-method req))
             (= "/" (:uri req)))
      {:status 200
       :body (pages/build-page-list fs-root) }
      (app req))))

(defn page-app [fs-root]
  (-> (not-found)
    (page-handler fs-root)
    (index-handler fs-root)
    (post-handler fs-root)
    (wrap-defaults api-defaults)))

(def app
  (-> (nio/default-file-system)
    (nio/get-path "")
    page-app))
