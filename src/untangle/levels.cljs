(ns untangle.levels
  (:require [untangle.circle :as circle :refer [Circle]]
            [untangle.constants :as consts
             :refer [circle-radius]]))


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

(defn deg-to-rad
  [deg]
  (* (/ Math/PI 180) deg))

(defn cos [n] (Math/cos n))
(defn sin [n] (Math/sin n))
(defn floor [n] (Math/floor n))

(defn compute-coords
  [x0 y0 r deg]
  (let [angle (deg-to-rad deg)
        x (+ x0 (floor (* r (cos angle))))
        y (+ y0 (floor (* r (sin angle))))]
    [x y]))

(defn compute-points
  [idxs width height]
  (let [[x0 y0] [(floor (/ width 2)) (floor (/ height 2))]
        r (- (min x0 y0) 50)
        n (count idxs)
        step (floor (/ 360 n))
        steps (take n (iterate #(+ step %) 0))
        comp-coords (partial compute-coords x0 y0 r)
        coords (map comp-coords steps)]
  (map (fn [idx [x y]]
         {idx (Circle. x y circle-radius)}) (shuffle idxs) coords)))
  
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
        (let [lst (second (last groups))
              adj' (condp = (count lst)
                         1 {}
                         2 {(first lst) [(second lst)]}
                         3 {(first lst) [(second lst) (last lst)]
                            (second lst) [(last lst)]})]
        {:circles (apply merge (compute-points idxs width height))
         :adjacent (merge adj adj')})
        (let [adjacent (compute-adjacent head)]
          (recur tail (merge adj adjacent)))))))
