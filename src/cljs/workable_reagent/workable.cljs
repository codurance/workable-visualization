(ns workable-reagent.workable
(:require   [cljs-http.client :as http]
            [cljs.core.async :as async]))

(defn get-stages []
  (http/get "/api/stages"))

