(ns double.core
  (:gen-class))

(defn overlaps?
  ([f s] (overlaps? f s < >=))
  ([{f-start :start f-end :end} {s-start :start s-end :end} before?-fn after?-fn]
   (assert (before?-fn f-start f-end) "First element's start timestamp should be before the end timestamp")
   (assert (before?-fn s-start s-end) "Second element's start timestamp should be before the end timestamp")
   (assert (after?-fn f-end f-start) "First element's end timestamp should be after the start timestamp")
   (assert (after?-fn s-end s-start) "Second element's end timestamp should be after the start timestamp")
   (assert (not= f-start f-end) "Events can't have same the same start and end timestamp.")
   (assert (not= s-start s-end) "Events can't have same the same start and end timestamp.")

   (or (and (before?-fn f-start s-end) (after?-fn f-start s-start))
       (and (before?-fn s-start f-end) (after?-fn s-start f-start)))))

(defn- overlapping-elements*
  "Takes a map and vector, and finds elements from the vector that
  overlap with the element given as the first parameter. ASSUMES THAT
  THE AFTER-VECTOR IS SORTED, and stops going through the vector when
  it finds the first element that doesn't overlap.
  Returns a set of sets, where each set contains a pair of ids."
  [element after]
  (->>
    (take-while (partial overlaps? element) after)
    (map :id)
    (interleave (repeat (:id element)))
    (partition 2)
    (map set)
    (into #{})))

(defn overlapping-elements [elements]
  (loop [[el & els] (sort-by :start < elements)
         overlapping-pairs #{}]
    (if (empty? els)
      overlapping-pairs
      (recur els
             (into overlapping-pairs (overlapping-elements* el els))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [bookings [{:id 1 :start 1 :end 10}
                  {:id 2 :start 1 :end 10}
                  {:id 3 :start 2 :end 5}
                  {:id 4 :start 3 :end 7}
                  {:id 5 :start 6 :end 7}
                  {:id 6 :start 2 :end 3}
                  {:id 7 :start 7 :end 8}
                  {:id 8 :start 8 :end 10}]]
    (println "Finding double booked entries from: " (pr-str bookings))

    (let [result (overlapping-elements bookings)]
      (println "Found the following pairs: " (pr-str result))
      result)))
