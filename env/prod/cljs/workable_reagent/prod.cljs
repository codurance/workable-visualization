(ns workable-reagent.prod
  (:require [workable-reagent.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
