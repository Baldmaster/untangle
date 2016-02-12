(ns untangle.core
  (:require [reagent.core :as reagent :refer [atom]]
            [untangle.circle :as circle :refer [Circle]]
            [untangle.line :as line :refer [Line]]
            [untangle.protocols :as prot :refer [draw]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;;(def Circle circle/Circle)

(defonce circle-radius 10)
(defonce line-thickness 3)

(defn canvas []
  [:canvas#game {:width 800
                 :height 480
                 :padding-left 20}])

(defn random-circle [width height]
  (let [x (rand-int width)
        y (rand-int height)]
    (Circle. x y circle-radius)))

(defn connect-points [points]
  (letfn [(new-line [start end]
            (Line. start end line-thickness))]
    (loop [[head & tail] points acc []]
      (if (empty? tail)
        acc
        (recur tail (concat acc (map new-line (repeat head) tail)))))))

(defn draw-lines [lines ctx]
  (doseq [line lines]
    (draw line ctx)))

(defn circle-selected?
  [movable steady]
  (fn [event]
    (let [lx (.-layerX event)
          ly (.-layerY event)
          selected-circle (->> @steady
                               (filter (fn [circle]
                                        (let [{:keys [x y radius]} circle]
                                          (< (+ (Math/pow (- lx x) 2) 
                                                (Math/pow (- ly y) 2))
                                             (Math/pow radius 2)))))
                               (first))]
      (do
        (reset! movable selected-circle)
        (reset! steady (filter #(not (= % selected-circle)) @steady))))))

(defn set-state! [movable steady]
  (fn [event]
    (let [circles (if (nil? @movable)
                    @steady
                    (cons @movable @steady))]
      (reset! steady circles)
      (reset! movable nil))))

(defn drag-circle! [movable event]
    (let [lx (.-layerX event)
          ly (.-layerY event)
          {:keys [x y radius]} @movable]
      (reset! movable (Circle. lx ly radius))))
          
(defn drag-handler
  [movable steady ctx width height]
  (fn [event]
    (when-not (nil? @movable)
      (drag-circle! movable event)
      (let [circles (cons @movable @steady)
            lines (connect-points circles)]
        (.clearRect ctx 0 0 width height)
        (doseq [line lines]
          (draw line ctx))
        (doseq [circle circles]
          (draw circle ctx))))))

(defn game []
  (let [canvas (. js/document (getElementById "game"))
        ctx (.getContext canvas "2d")
        width (.-width canvas)
        height (.-height canvas)
        circles (take 5 (repeatedly #(random-circle width height)))
        movable-circle (atom nil)
        steady-circles (atom circles)]
    (.addEventListener canvas "mousedown" (circle-selected? movable-circle steady-circles))
    (.addEventListener canvas "mousemove" (drag-handler
                                           movable-circle
                                           steady-circles
                                           ctx width height))
    (.addEventListener canvas "mouseup" (set-state! movable-circle steady-circles))
    (doseq [line (connect-points circles)]
      (draw line ctx))
    (doseq [circle circles]
      (draw circle ctx))))


(defn game-component []
  (reagent/create-class {:component-did-mount game
                         :reagent-render canvas}))

(reagent/render-component [game-component]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
