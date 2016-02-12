(ns untangle.circle
  (:require [untangle.protocols :as prot]))


(defrecord Circle [x y radius]
  prot/Draw
  (prot/draw [this ctx]
    (do
      (-> (.-fillStyle ctx)
          (set! "gold"))
      (.beginPath ctx)
      (. ctx (arc x y radius 0 (* Math/PI 2) true))
      (.closePath ctx)
      (.fill ctx))))
    
