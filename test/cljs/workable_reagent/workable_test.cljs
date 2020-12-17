(ns workable-reagent.workable-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [cljs-http.client :as http]
            [cljs.core.async :as async]
            [workable-reagent.workable :refer [get-stages get-data]]))

(deftest workable-test 
  (testing "gets stages from the server API"
    (let [c (async/chan)]
      (with-redefs [http/get (fn [url] c)]
        (is (= (get-stages)
               c)))))
  (testing "gets data from the server API"
    (let [c (async/chan)]
      (with-redefs [http/get (fn [url] c)]
        (is (= (get-data)
               c))))))
