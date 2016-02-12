(ns untangle.line
  (:require [untangle.protocols :as prot :refer [Draw draw]]))

(defrecord Line [start end thikness]
  Draw
  (draw [this ctx]
    (let [{x1 :x, y1 :y} start
          {x2 :x, y2 :y} end]
      (do
        (.beginPath ctx)
        (. ctx (moveTo x1 y1))
        (. ctx (lineTo x2 y2))
        (set! (.-lineWidth ctx) thickness)
        (set! (.-strokeStyle ctx) "#cfc")
        (.stroke ctx)))))
  
