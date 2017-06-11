(ns phrase.handler
  (:require [hiccup.page :refer [include-js include-css html5]]
            [config.core :refer [env]]
            [compojure.route :refer [resources]]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]))


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

(def app
  (api
    (resources "/")
    (ANY "/*" [] (not-found "404 Not Found"))
    (GET "/" [] (loading-page))
    (GET "/about" [] (loading-page))

    (context "/api" []

      (GET "/ping" []
        (ok {:ping "pong"})))))
