(ns workable-reagent.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [workable-reagent.workable :refer :all]
            [workable-reagent.handler :refer :all]))

(deftest workable-stages-handler-test
  (testing "handler provides success"
    (let [expected-response {:status 200 
                             :headers {"Content-Type" "application/json"} 
                             :body "[{\"slug\":\"sourced\", \"name\": \"sourced\", \"kind\": \"sourced\", \"position\": 0}]"}]
      (with-redefs [get-stages (fn [c] expected-response)]
        (let [actual-response (stages-handler {})]
          (is (= actual-response expected-response))))))
  (testing "handler provides failure"
    (let [expected-response {:status 401 
                             :headers {"Content-Type" "application/json"}
                             :body  "{\"error\": \"not authorized\"}"}]
      (with-redefs [get-stages (fn [c] expected-response)]
        (let [actual-response (stages-handler {})]
          (is (= actual-response expected-response)))))))
