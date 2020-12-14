(ns workable-reagent.handler
  (:require
   [reitit.ring :as reitit-ring]
   [workable-reagent.middleware :refer [middleware]]
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]
   [workable-reagent.workable :refer [get-stages]]
   [workable-reagent.config :refer [default-config]]))

(def mount-target
  [:div#app
   [:h2 "Welcome to the desert of the real fred, barney"]
   [:p "please wait while Figwheel is waking up ..."]
   [:p "(Check the js console for hints if nothing exciting happens.)"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    (include-js "/js/app.js")]))


(defn index-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

(defn test-handler [_request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body "{\"lol\": true }"})

(defn stages-handler [_request]
  (let [workable-response (get-stages default-config)]
    {:status (:status workable-response)
     :headers (:headers workable-response)
     :body (:body workable-response)}))

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/" {:get {:handler index-handler}}]
     ["/lol" {:get {:handler test-handler}}]
     ["/api/stages" {:get {:handler stages-handler}}]])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware middleware}))
