(ns workable-reagent.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.data.json :as json]
            [workable-reagent.workable :refer :all]
            [workable-reagent.handler :refer :all]))

(deftest workable-stages-handler-test
  (testing "handler provides success"
    (let [expected-response {:status 200 
                             :headers {"Content-Type" "application/json"} 
                             :body (json/write-str {:stages '({:slug "sourced"
                                                               :name "Sourced"
                                                               :kind "sourced"
                                                               :position 0})})}]
      (with-redefs [get-stages (fn [c] expected-response)]
        (let [actual-response (stages-handler {})]
          (is (= actual-response expected-response))))))
  (testing "handler provides failure"
    (let [expected-response {:status 401 
                             :headers {"Content-Type" "application/json"}
                             :body (json/write-str {:error "not authorized"})}]
      (with-redefs [get-stages (fn [c] expected-response)]
        (let [actual-response (stages-handler {})]
          (is (= actual-response expected-response)))))))

(deftest workable-data-handler-test
  (testing "fails to get the stages from Workable"
    (let [stages-response {:status 401
                           :headers {"Content-Type" "application/json"}
                           :body (json/write-str {:error "not authorized"})}
          active-jobs-response {:status 200
                                :headers {"Content-Type" "application/json"}
                                :body (json/write-str {})}]
      (with-redefs [get-stages (fn [c] stages-response)
                    get-active-jobs (fn [c] active-jobs-response)]
        (let [expected-response {:status 404
                                 :headers {"Content-Type" "application/json"}
                                 :body (json/write-str {:error "failed to get stages"})}
              actual-response (workable-data-handler {})]
          (is (= actual-response expected-response))))))
  (testing "fails to get the active jobs from Workable"
    (let [stages-response {:status 200
                           :headers {"Content-Type" "application/json"}
                           :body (json/write-str {:stages '()})}
          active-jobs-response {:status 401
                                :headers {"Content-Type" "application/json"}
                                :body (json/write-str {:error "not authorized"})}
          expected-response {:status 404
                             :headers {"Content-Type" "application/json"}
                             :body (json/write-str {:error "failed to get active jobs"})}]
      (with-redefs [get-stages (fn [_] stages-response)
                    get-active-jobs (fn [_] active-jobs-response)]
        (is (= (workable-data-handler {})
               expected-response)))))
  (testing "fails to get candidates for a job"
    (let [stages-response {:status 200
                           :headers {"Content-Type" "application/json"}
                           :body (json/write-str {:stages '()})}
          active-jobs-response {:status 200
                                :headers {"Content-Type" "application/json"}
                                :body (json/write-str {:jobs '({:id "1234"})})}
          candidates-for-1234-response {:status 401
                                        :headers {"Content-Type" "application/json"}
                                        :body (json/write-str {:error "not authorized"})}
          expected-response {:status 404
                             :headers {"Content-Type" "application/json"}
                             :body (json/write-str {:error "failed to get candidates"})}]
      (with-redefs [get-stages (fn [_] stages-response)
                    get-active-jobs (fn [_] active-jobs-response)
                    get-candidates-for-job (fn [_ _] candidates-for-1234-response)]
        (is (= expected-response (workable-data-handler {}))))))

  (testing "successfully gets jobs and candidates"
    (let [stages-response {:status 200
                           :headers {"Content-Type" "application/json"}
                           :body (json/write-str {:stages '({:slug "sourced"
                                                             :name "Sourced"
                                                             :kind "sourced"
                                                             :position 0}
                                                            {:slug "applied"
                                                             :name "Applied"
                                                             :kind "applied"
                                                             :position 1}
                                                            {:slug "phone-screen"
                                                             :name "Phone Screen"
                                                             :kind "phone-screen"
                                                             :position 2})})}
          active-jobs-response {:status 200
                                :headers {"Content-Type" "application/json"}
                                :body (json/write-str {:jobs '({:id "1234"
                                                                :title "developer"
                                                                :location {
                                                                           :city "developerland"
                                                                           }}
                                                               {:id "5678"
                                                                :title "qa"
                                                                :location {
                                                                           :city "qaland"
                                                                           }}
                                                               {:id "9012"
                                                                :title "ba"
                                                                :location {
                                                                           :city "baland"
                                                                           }})})}
          candidates-for-1234-response {:status 200
                                        :headers {"Content-Type" "application/json"}
                                        :body (json/write-str {:candidates '({:name "John Doe"
                                                                              :profile-url "http://john-doe"
                                                                              :stage "Sourced"
                                                                              }
                                                                             )})}
          candidates-for-5678-response {:status 200
                                        :headers {"Content-Type" "application/json"}
                                        :body (json/write-str {:candidates '({:name "James Doe"
                                                                              :profile-url "http://james-doe"
                                                                              :stage "Applied"
                                                                              }
                                                                             {:name "Jane Doe"
                                                                              :profile-url "http://jane-doe"
                                                                              :stage "Phone Screen"
                                                                              }
                                                                             )})}
          candidates-for-9012-response {:status 200
                                        :headers {"Content-Type" "application/json"}
                                        :body (json/write-str {:candidates '({:name "Tracy Doe"
                                                                              :profile-url "http://tracy-doe"
                                                                              :stage "Applied"
                                                                              }
                                                                             {:name "Thomas Doe"
                                                                              :profile-url "http://thomas-doe"
                                                                              :stage "Sourced"
                                                                              }
                                                                             {:name "Michael Doe"
                                                                              :profile-url "http://michael-doe"
                                                                              :stage "Applied"
                                                                              }
                                                                             )})}
          expected-response {:status 200
                             :headers {"Content-Type" "application/json"}
                             :body (json/write-str {:stages [
                                                             {:name "Sourced"
                                                              :kind "sourced"
                                                              }
                                                             {:name "Applied"
                                                              :kind "applied"
                                                              }
                                                             {
                                                              :name "Phone Screen"
                                                              :kind "phone-screen"
                                                              }]
                                                             :candidates [
                                                                          {
                                                                           :name "Sourced"
                                                                           :items [
                                                                                   {
                                                                                    :name "John Doe"
                                                                                    :profile-url "http://john-doe"
                                                                                    :stage "Sourced"
                                                                                    :location "developerland"
                                                                                    }
                                                                                   {
                                                                                    :name "Thomas Doe"
                                                                                    :profile-url "http://thomas-doe"
                                                                                    :stage "Sourced"
                                                                                    :location "baland"
                                                                                    }
                                                                                   ]
                                                                           }
                                                                          {
                                                                           :name "Applied"
                                                                           :items [
                                                                                   {:name "James Doe"
                                                                                    :profile-url "http://james-doe"
                                                                                    :stage "Applied"
                                                                                    :location "qaland"
                                                                                    }
                                                                                   {:name "Tracy Doe"
                                                                                    :profile-url "http://tracy-doe"
                                                                                    :stage "Applied"
                                                                                    :location "baland"
                                                                                    }
                                                                                   {:name "Michael Doe"
                                                                                    :profile-url "http://michael-doe"
                                                                                    :stage "Applied"
                                                                                    :location "baland"
                                                                                    }
                                                                                   ]
                                                                           }
                                                                          {
                                                                           :name "Phone Screen"
                                                                           :items [
                                                                                   {:name "Jane Doe"
                                                                                    :profile-url "http://jane-doe"
                                                                                    :stage "Phone Screen"
                                                                                    :location "qaland"
                                                                                    }
                                                                                   ]
                                                                           }
                                                                          ]
                                                    })}]
(with-redefs [get-stages (fn [_] stages-response)
              get-active-jobs (fn [_] active-jobs-response)
              get-candidates-for-job (fn [_ id] 
                                       (cond
                                         (= id "1234") candidates-for-1234-response
                                         (= id "5678") candidates-for-5678-response
                                         (= id "9012") candidates-for-9012-response))]
  (is (= expected-response (workable-data-handler {})))))))

