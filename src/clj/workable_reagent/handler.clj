(ns workable-reagent.handler
  (:require
   [reitit.ring :as reitit-ring]
   [workable-reagent.middleware :refer [middleware]]
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]
   [clojure.data.json :as json]
   [workable-reagent.workable :refer [get-stages get-active-jobs get-candidates-for-job]]
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

(defn workable-data-handler [_request]
  (defn get-candidates-response [stages jobs]
    (loop [remaing-jobs jobs
           partial-response-body {:stages (map (fn [s] {:name (:name s)
                                                        :kind (:kind s)})
                                               stages)
                                  :candidates '()}]
      (if (empty? remaing-jobs)
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json/write-str { :stages (:stages partial-response-body) 
                               :candidates (map (fn [[ s i ]] {:name s :items i})
                                                (group-by :stage (:candidates partial-response-body)))})}
        (let [current-job (first remaing-jobs)
              candidates-response (get-candidates-for-job default-config (:id current-job))]
          (if (not= (:status candidates-response) 200)
            {:status 404
             :headers {"Content-Type" "application/json"}
             :body (json/write-str {:error "failed to get candidates"})}
            (recur (rest remaing-jobs)
                   {:stages (:stages partial-response-body)
                    :candidates (concat (:candidates partial-response-body)
                                        (map (fn [c] {:name (:name c)
                                                      :profile-url (:profile-url c)
                                                      :stage (:stage c)
                                                      :location (:city (:location current-job))})
                                             (:candidates (json/read-str (:body candidates-response)
                                                                         :key-fn keyword))))
                    }))))))
  (let [stages-response (get-stages default-config)]
    (if (= (:status stages-response) 200)
      (let [jobs-response (get-active-jobs default-config)]
        (if (= (:status jobs-response) 200)
          (let [stages (json/read-str (:body stages-response) :key-fn keyword)
                jobs (json/read-str (:body jobs-response) :key-fn keyword)
                response (get-candidates-response (:stages stages) (:jobs jobs))]
            response)
          {:status 404
           :headers {"Content-Type" "application/json"}
           :body (json/write-str {:error "failed to get active jobs"})}))
      {:status 404
       :headers {"Content-Type" "application/json"}
       :body (json/write-str {:error "failed to get stages"})})))

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/" {:get {:handler index-handler}}]
     ["/lol" {:get {:handler test-handler}}]
     ["/api/stages" {:get {:handler stages-handler}}]
     ["/api/data" {:get {:handler workable-data-handler}}]])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware middleware}))

