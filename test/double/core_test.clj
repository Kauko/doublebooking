(ns double.core-test
  (:require [clojure.test :refer :all]
            [double.core :as double]))

(deftest overlaps
  (testing "Overlapping; second starts during the first"
    (is (true? (double/overlaps? {:start 2 :end 5} {:start 3 :end 6}))))

  (testing "Overlapping; first starts during the second"
    (is (true? (double/overlaps? {:start 3 :end 6} {:start 2 :end 5}))))

  (testing "Overlapping; first contains the second"
    (is (true? (double/overlaps? {:start 1 :end 10} {:start 2 :end 8})))
    (is (true? (double/overlaps? {:start 1 :end 10} {:start 1 :end 8})))
    (is (true? (double/overlaps? {:start 1 :end 10} {:start 2 :end 10}))))

  (testing "Overlapping; second contains the first"
    (is (true? (double/overlaps? {:start 2 :end 8} {:start 1 :end 10})))
    (is (true? (double/overlaps? {:start 1 :end 8} {:start 1 :end 10})))
    (is (true? (double/overlaps? {:start 2 :end 10} {:start 1 :end 10}))))

  (testing "Overlapping; both are equal"
    (is (true? (double/overlaps? {:start 1 :end 10} {:start 1 :end 10}))))

  (testing "Shouldn't overlap; second is after the first"
    (is (false? (double/overlaps? {:start 0 :end 2} {:start 5 :end 10}))))

  (testing "Shouldn't overlap; second is before the first"
    (is (false? (double/overlaps? {:start 5 :end 10} {:start 0 :end 2}))))

  (testing "Shouldn't overlap; second closely follows the first"
    (is (false? (double/overlaps? {:start 1 :end 3} {:start 3 :end 5}))))

  (testing "Shouldn't overlap; first closely follows the second"
    (is (false? (double/overlaps? {:start 3 :end 5} {:start 1 :end 3}))))

  (testing "Assertion errors"
    (is (thrown? AssertionError (double/overlaps? {:start 0 :end 0} {:start 1 :end 2})))
    (is (thrown? AssertionError (double/overlaps? {:start 2 :end 4} {:start 5 :end 5})))
    (is (thrown? AssertionError (double/overlaps? {:start 4 :end 3} {:start 1 :end 2})))
    (is (thrown? AssertionError (double/overlaps? {:start 4 :end 5} {:start 5 :end 2})))))

(deftest overlapping-elements*
  (def overlapping-elements* #'double/overlapping-elements*)

  (testing "Empty input = empty set"
    (is (= #{} (overlapping-elements* nil nil)))
    (is (= #{} (overlapping-elements* {} nil)))
    (is (= #{} (overlapping-elements* {} []))))

  (testing "Happy case"
    (is (= #{#{1 2}} (overlapping-elements* {:id 1 :start 1 :end 5} [{:id 2 :start 3 :end 6}
                                                                     {:id 3 :start 6 :end 7}])))

    (is (= #{#{1 2} #{1 3} #{1 4}}
           (overlapping-elements* {:id 1 :start 1 :end 5} [{:id 2 :start 1 :end 6}
                                                           {:id 3 :start 1 :end 3}
                                                           {:id 4 :start 1 :end 5}
                                                           {:id 5 :start 5 :end 6}]))))

  (testing "No overlaps = empty set"
    (is (= #{} (overlapping-elements* {:id 1 :start 1 :end 5} [{:id 2 :start 5 :end 6}
                                                               {:id 3 :start 6 :end 7}]))))

  (testing "Stops after first non-overlapping element"
    (is (= #{#{1 2}} (overlapping-elements* {:id 1 :start 1 :end 5} [{:id 2 :start 3 :end 6}
                                                                     {:id 3 :start 6 :end 7}
                                                                     {:id 4 :start 3 :end 6}])))))

(deftest overlapping-elements
  ;; Big black box test of the whole app!
  (let [data [{:id 1 :start 1 :end 10}
              {:id 2 :start 1 :end 10}
              {:id 3 :start 2 :end 5}
              {:id 4 :start 3 :end 7}
              {:id 5 :start 6 :end 7}
              {:id 6 :start 2 :end 3}
              {:id 7 :start 7 :end 8}
              {:id 8 :start 8 :end 10}]
        overlaps #{#{1 2} #{1 3} #{1 4} #{1 5} #{1 6} #{1 7} #{1 8}
                   #{2 3} #{2 4} #{2 5} #{2 6} #{2 7} #{2 8}
                   #{3 4} #{3 6}
                   #{4 5}}]
    (is (= overlaps (double/overlapping-elements data)))))