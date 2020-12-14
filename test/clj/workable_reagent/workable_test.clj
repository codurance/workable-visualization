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

(deftest get-active-jobs-test
  (testing "Failed to get the active jobs"
    (let [expected-response {:status 401
                             :headers {"Content-Type" "application/json"}
                             :body "{\"error\":\"Not authorized\"}"}]
      (hmock/with-mock-routes [
                               (hmock/route {:method :get
                                             :scheme :https
                                             :host "test.workable.com"
                                             :path "/spi/v3/jobs"
                                             :query-params {:status "published"}})
                               (constantly expected-response)]
        (let [actual-response (get-active-jobs {:subdomain "test" :token "lol"})]
          (is (and (= (:body expected-response)
                      (:body actual-response))
                   (= (:status expected-response)
                      (:status actual-response))
                   (= (:headers expected-response)
                      (:headers actual-response))))))))
  (testing "Succeeded to get the active jobs"
    (let [expected-response {:status 200
                             :headers {"Content-Type" "application/json"}
                             :body "{  \"jobs\": [ { \"id\": \"61884e2\", \"title\": \"Sales Intern\", \"full_title\": \"Sales Intern - US/3/SI\", \"shortcode\": \"GROOV003\", \"code\": \"US/3/SI\", \"state\": \"draft\", \"department\": \"Sales\", \"url\": \"https://groove-tech.workable.com/jobs/102268944\", \"application_url\": \"https://groove-tech.workable.com/jobs/102268944/candidates/new\", \"shortlink\": \"https://groove-tech.workable.com/j/GROOV003\", \"location\": { \"location_str\": \"Portland, Oregon, United States\", \"country\": \"United States\", \"country_code\": \"US\", \"region\": \"Oregon\", \"region_code\": \"OR\", \"city\": \"Portland\", \"zip_code\": \"97201\", \"telecommuting\": false }, \"created_at\": \"2015-07-01T00:00:00Z\" }, { \"id\": \"1166bf8c\", \"title\": \"Operations Manager\", \"full_title\": \"Operations Manager - US/02/CM\", \"shortcode\": \"GROOV001\", \"code\": \"US/02/CM\", \"state\": \"archived\", \"department\": \"Operations\", \"url\": \"https://groove-tech.workable.com/jobs/291945146\", \"application_url\": \"https://groove-tech.workable.com/jobs/291945146/candidates/new\", \"shortlink\": \"https://groove-tech.workable.com/j/GROOV001\", \"location\": { \"location_str\": \"Chicago, Illinois, United States\", \"country\": \"United States\", \"country_code\": \"US\", \"region\": \"Illinois\", \"region_code\": \"IL\", \"city\": \"Chicago\", \"zip_code\": \"60290\", \"telecommuting\": false }, \"created_at\": \"2015-05-07T00:00:00Z\" }, { \"id\": \"167636b1\", \"title\": \"Office Manager\", \"full_title\": \"Office Manager - US/4/OM\", \"shortcode\": \"GROOV005\", \"code\": \"US/4/OM\", \"state\": \"published\", \"department\": \"Administration\", \"url\": \"https://groove-tech.workable.com/jobs/376844767\", \"application_url\": \"https://groove-tech.workable.com/jobs/376844767/candidates/new\", \"shortlink\": \"https://groove-tech.workable.com/j/GROOV005\", \"location\": { \"location_str\": \"Chicago, Illinois, United States\", \"country\": \"United States\", \"country_code\": \"US\", \"region\": \"Illinois\", \"region_code\": \"IL\", \"city\": \"Chicago\", \"zip_code\": \"60290\", \"telecommuting\": false }, \"created_at\": \"2015-06-06T00:00:00Z\" } ], \"paging\": { \"next\": \"https://www.workable.com/spi/v3/accounts/groove-tech/jobs?limit=3&since_id=2700d6df\" } }"}]
      (hmock/with-mock-routes [(hmock/route :get "https://test.workable.com/spi/v3/jobs?status=published")
                               (constantly expected-response)]
        (let [actual-response (get-active-jobs {:subdomain "test" :token "lol"})]
          (is (and (= (:body expected-response)
                      (:body actual-response))
                   (= (:headers expected-response)
                      (:headers actual-response))
                   (= (:status expected-response)
                      (:status actual-response)))))))))
  
