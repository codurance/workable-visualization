(ns workable-reagent.workable-test
  (:require [cljs.test :refer-macros [deftest is testing]]))

(deftest empty-test 
  (testing "useless things"
    (is (= 2 2))))
