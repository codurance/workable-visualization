(ns workable-reagent.workable
  (:require [clj-http.client :as http]))

(defn get-stages [config]
  (http/get (str "https://"
                 (:subdomain config)
                 ".workable.com/spi/v3/stages")
            {:throw-exceptions false
             :content-type "application/json"
             :headers {"Authorization" (str "Bearer " (:token config))}}))
