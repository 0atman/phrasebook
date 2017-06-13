(ns phrase.handler
  (:require [hiccup.page :refer [include-js include-css html5]]
            [config.core :refer [env]]
            [compojure.route :refer [resources]]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [clj-http.client :as client]
            [clojure.data.json :as json]))


(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css")])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

(defn store-get [auth key]
  (:body (client/get
           (str "https://store.oat.sh/api/" auth "/" key "/json")
           {:as :json})))

(defn store-post [auth key data]
  (:body (client/post
           (str "https://store.oat.sh/api/" auth "/" key)
           {:form-params {:data data}})))
(def app
  (api
    (resources "/")
    (GET "/" [] (loading-page))
    (GET "/about" [] (loading-page))

    (context "/api" []

      (GET "/ping" []
        (ok {:ping "pong"}))

      (GET "/context" [auth key]
        (ok (store-get auth key)))

      (POST "/context/:auth/:key" [data]
        :path-params [auth :- String, key  :- String]
        (ok (store-post auth key (json/write-str data)))))
    (ANY "/*" [] (not-found "404 Not Found"))))
