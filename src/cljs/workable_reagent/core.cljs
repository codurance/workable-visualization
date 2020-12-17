(ns workable-reagent.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [reagent.dom :as rdom]
    [reagent.session :as session]
    [reitit.frontend :as reitit]
    [clerk.core :as clerk]
    [accountant.core :as accountant]
    [workable-reagent.workable :as workable]
    [cljs.core.async :refer-macros [go]]
    [cljs.core.async :refer [<!]]
    [clojure.string :as str]))

;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/" :index]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

;; -------------------------
;; Page components

(defn home-page []
  (let [data-atom (reagent/atom {})]
    (fn []
      (if (empty? @data-atom)
      (go
        (let [response (<! (workable/get-data))]
          (reset! data-atom (:body response)))))
      (if (empty? @data-atom)
        [:p "Loading..."]
      [:table
       [:tr (map (fn [s] [:th (:name s)]) (:stages @data-atom))]
       (let [org-candidates (map (fn [s]
                                   {:name (:name s)
                                    :items (:items (first (filter (fn [c] (= (str/lower-case (:name s))
                                                                             (str/lower-case (:name c))))
                                                                  (:candidates @data-atom))))})
                                 (:stages @data-atom))]
       (loop [candidates org-candidates
              result []]
         (let [remaining (reduce + (map (fn [s] (count (:items s))) candidates))]
           (if (= remaining 0)
             result
             (recur (map (fn [s] {:name (:name s)
                                  :items (rest (:items s))}) candidates)
                    (concat result 
                            [[:tr
                              (map (fn [s] 
                                     (let [candidate (first (:items s))]
                                       [:td [:a {:href (:profile-url candidate)} 
                                             [:div {:class (if (and (not (nil? (:name candidate)))
                                                                    (nil? (:location candidate)))
                                                             "unknown"
                                                             (:location candidate))}
                                              [:div.name (:name candidate)]
                                              [:div.role (:role candidate)]
                                              [:div.location (:location candidate)]]]])) 
                                   candidates)]]))))))]))))

;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index #'home-page))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [:header
        [:p "We need to think about this header"]]
       [page]
       [:footer
        [:p "We need to think about this footer"]]])))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rdom/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (let [match (reitit/match-by-path router path)
             current-page (:name (:data  match))
             route-params (:path-params match)]
         (reagent/after-render clerk/after-render!)
         (session/put! :route {:current-page (page-for current-page)
                               :route-params route-params})
         (clerk/navigate-page! path)
         ))
     :path-exists?
     (fn [path]
       (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
