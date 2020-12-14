(ns workable-reagent.workable-test
  (:require [clj-http.client :as http]
            [clj-http-mock.core :as hmock]
            [clojure.test :refer [deftest is testing]]
            [workable-reagent.workable :refer :all]))

(deftest get-stages-test
  (testing "Failed to get stages"
    (let [expected-response {:status 401 
                             :headers {"Content-Type" "application/json"}
                             :body  "{\"error\": \"Not authorized\"}"}]
      (hmock/with-mock-routes
        [(hmock/route :get "https://test.workable.com/spi/v3/stages")
         (constantly expected-response)]
        (let [actual-response (get-stages {:subdomain "test" :token "lol"})]
          (is (and (= (:status expected-response)
                      (:status actual-response))
                   (= (:body expected-response)
                      (:body actual-response))
                   (= (:headers expected-response)
                      (:headers actual-response))))))))

  (testing "Succeeded to get stages"
    (let [expected-response {:status 200 
                             :headers {"Content-Type" "application/json"}
                             :body  "[{\"slug\":\"sourced\", \"name\": \"sourced\", \"kind\": \"sourced\", \"position\": 0}]"}]
      (hmock/with-mock-routes
        [(hmock/route :get "https://test.workable.com/spi/v3/stages")
         (constantly expected-response)]
        (let [actual-response (get-stages {:subdomain "test" :token "lol"})]
          (is (and (= (:status expected-response)
                      (:status actual-response))
                   (= (:body expected-response)
                      (:body actual-response))
                   (= (:headers expected-response)
                      (:headers actual-response)))))))))

