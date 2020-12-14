(ns workable-reagent.workable
  (:require [clj-http.client :as http]))

(defn workable-get [config path query-params]
  (http/get (str "https://"
                 (:subdomain config)
                 ".workable.com/spi/v3"
                 path)
            {:throw-exceptions false
             :content-type "application/json"
             :query-params query-params
             :headers {"Authorization" (str "Bearer " (:token config))}}))

(defn get-stages [config]
  (workable-get config "/stages" nil))

(defn get-active-jobs [config]
  (workable-get config "/jobs" {"status" "published"}))
