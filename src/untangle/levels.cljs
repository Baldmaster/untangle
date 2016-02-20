(ns untangle.levels
  (:require [untangle.circle :as circle :refer [Circle]]
            [untangle.constants :as consts
             :refer [circle-radius]]))

(def levels
  {:1 
   {:circles {:0 (Circle. 50 40 circle-radius)
              :1 (Circle. 400 70 circle-radius)
              :2 (Circle. 500 400 circle-radius)
              :3 (Circle. 80 350 circle-radius)}
    :adjacent {:0 [:2 :3]
               :1 [:2 :3]}}
   :2
   {:circles {:0 (Circle. 400 90 circle-radius)
              :1 (Circle. 550 140 circle-radius)
              :2 (Circle. 550 240 circle-radius)
              :3 (Circle. 400 290 circle-radius)
              :4 (Circle. 250 240 circle-radius)
              :5 (Circle. 250 140 circle-radius)}
    :adjacent {:0 [:2 :4]
               :1 [:3 :4 :5]
               :2 [:5 :4]
               :3 [:5]}}})



(defn random-ball
  [width height]
  (let [x (rand-int width)
        y (rand-int height)]
    (Circle. x y circle-radius)))

(defn compute-adjacent
  [[outer inner]]
  (let [[b1 b2 b3] (condp = (count inner)
                     1 (replicate 3 (first inner))
                     2 (cons (first inner) inner)
                     3 inner)
        [a1 a2 a3] outer]
    {a1 [a2 a3 b1]
     a2 [a3 b2]
     a3 [b3]}))


(defn build-level
  [n width height]
  (let [v (+ 3 n)
        split3 (partial partition 3)
        to-pairs (partial partition 2 1)
        remainder (rem v 3)
        idxs (->> (take v (iterate inc 1))
                  (map #(keyword (str %))))
        groups (-> (split3 idxs)
                   (vec)
                   (conj (take-last remainder idxs))
                   (#(remove nil? %))
                   (to-pairs))]
    (loop [[head & tail] groups adj {}]
      (if (empty? head)
        (do
          (println "done")
        {:circles (reduce #(assoc %1 %2 (random-ball width height)) {} idxs)
         :adjacent adj})
        (let [adjacent (compute-adjacent head)]
          (recur tail (merge adj adjacent)))))))
