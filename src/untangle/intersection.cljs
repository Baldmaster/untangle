;;Obviously, the bottleneck is here
(ns untangle.intersection)


(defn line-coefficients
  [{:keys [start end]}]
  (let [{x1 :x y1 :y} start
        {x2 :x y2 :y} end
        a (- y1 y2)
        b (- x2 x1)
        c (- (* x1 y2) (* x2 y1))]
    [a b c]))

(defn lies-on?
  [{:keys [start end]} x y]
  (let [{x1 :x y1 :y} start
        {x2 :x y2 :y} end]
    (or (or (and (< x1 x) (< x x2))
            (and (< x2 x) (< x x1)))
        (or (and (< y1 y) (< y y2))
            (and (< y2 y) (< y y1))))))


(defn intersect?
  [l1 l2]
  (let [[a1 b1 c1] (line-coefficients l1)
        [a2 b2 c2] (line-coefficients l2)
        d (- (* a1 b2) (* a2 b1))
        x (- (/ (- (* b2 c1) (* b1 c2)) d))
        y (- (/ (- (* a1 c2) (* a2 c1)) d))]
    (if (= d 0)
      false
      (and (lies-on? l1 x y) (lies-on? l2 x y)))))

(defn detect-intersections
  [lines & {:keys [thickness]
            :or {thickness 5}}]
  (loop [acc [] [head & tail] lines]
    (if (empty? head)
      acc
      (let [k (map intersect? (repeat head) (if (empty? tail) '() tail))
            m (map intersect? (repeat head) acc)
            l (if (and (not-any? true? k) (not-any? true? m))
                head
                (assoc head :thickness thickness))]
        (recur (cons l acc) tail)))))
      
