(ns workable-reagent.workable
  (:require [cljs-http.client :as http]))

(defn get-stages []
  (http/get "/api/stages"))

