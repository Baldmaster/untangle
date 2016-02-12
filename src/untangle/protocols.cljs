(ns untangle.protocols)

(defprotocol Draw
  "Draw something 
   on provided canvas context"
  (draw [this ctx]))
