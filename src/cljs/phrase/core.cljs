(ns phrase.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [cljs-http.client :as http]
              [cljs.core.async :refer [<!]])
    (:require-macros [cljs.core.async.macros :refer [go]]))

;; -------------------------
;; Views

(defn table-row [_]
  [:tr
   [:td "1,001"]
   [:td "Lorem"]
   [:td "ipsum"]
   [:td "dolor"]
   [:td "sit"]])

(defn generate-table [name length]
  [:h2 {:class "sub-header"} name]
  [:div {:class "table-responsive"}]
  [:table {:class "table table-striped"}
    [:thead
      [:tr
        [:th "#"]
        [:th "Header"]
        [:th "Header"]
        [:th "Header"]
        [:th "Header"]]]
    [:tbody
      (map table-row (range length))]])

(defn reset-get! [an-atom endpoint]
  "puts `response` into `an-atom`"
  (go (let [response (<! (http/get endpoint))]
        (reset! an-atom (str (:body response))))))

(defn clj->json
  [ds]
  (.stringify js/JSON (clj->js ds)))

(def up-and-atom (atom "NONE"))

(defn home-page []

  [:div {:class "container-fluid"}
    [:p
      [:input {:type "button" :value "UPDATE"
                :on-click #(reset-get! up-and-atom "/api/context?auth=public&key=context")}]
      [:input {:type "button" :value "data1"
                :on-click #(http/post "/api/context/public/context"
                            {:form-params {:data (clj->json {"1" "2"})}})}]
      [:input {:type "button" :value "data2"
                :on-click #(http/post "/api/context/public/context"
                            {:form-params {:data (clj->json {"3" "4"})}})}]
      @up-and-atom]
    [:div
      [:h1 "OAT.SH Phrasebook"]
      (generate-table "table" 5)]])

(defn about-page []
  [:div {:class "container-fluid"}
    [:div
      [:h2 "About phrasebook"]
      [:div [:a {:href "/"} "go to the home page"]]
      [:h2 {:class "sub-header"} "Section title"]
      [:div {:class "table-responsive"}
       [:table {:class "table table-striped"}
        [:thead
         [:tr
          [:th "#"]
          [:th "Header"]
          [:th "Header"]
          [:th "Header"]
          [:th "Header"]]]
        [:tbody
         [:tr
          [:td "1,001"]
          [:td "Lorem"]
          [:td "ipsum"]
          [:td "dolor"]
          [:td "sit"]]
         [:tr
          [:td "1,002"]
          [:td "amet"]
          [:td "consectetur"]
          [:td "adipiscing"]
          [:td "elit"]]]]]]])
;; -------------------------
;; Routes

(def page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

(secretary/defroute "/about" []
  (reset! page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
