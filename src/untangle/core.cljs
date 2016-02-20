(ns untangle.core
  (:require [reagent.core :as reagent :refer [atom]]
            [untangle.circle :as circle :refer [Circle]]
            [untangle.line :as line :refer [Line]]
            [untangle.levels :as levels :refer [levels build-level]]
            [untangle.constants :as consts
             :refer [circle-radius line-thickness intersect-thickness]]
            [untangle.intersection :as intersection
             :refer [detect-intersections]]
            [untangle.protocols :as prot :refer [draw]]))

(enable-console-print!)


(defn circle-select-handler
  [movable level]
  (fn [event]
    (let [lx (.-layerX event)
          ly (.-layerY event)
          selected-circle (->> (:circles @level)
                               (filter (fn [circle]
                                        (let [[k {:keys [x y radius]}] circle]
                                          (< (+ (Math/pow (- lx x) 2) 
                                                (Math/pow (- ly y) 2))
                                             (Math/pow radius 2)))))
                               (first))]
      (when-not (nil? selected-circle)
        (reset! movable (first selected-circle))))))
          
(defn drag-handler
  [^Atom movable ^Atom level]
  (fn [event]
    (when-not (nil? @movable)
      (let [lx (.-layerX event)
            ly (.-layerY event)
            k @movable
            c (assoc (-> @level
                         (:circles)
                         (k)) :x lx :y ly)]
        (swap! level assoc-in [:circles k] c)))))

(defn compute-elements
  ;; Compute lines and circles"
  [level]
  (let [{:keys [circles adjacent]} level
        points (vals circles)
        lines (-> (map
                   (fn [[k adj]]
                     (map
                      #(Line. (%1 circles) (%2 circles) 2) (repeat k) adj))
                   adjacent)
                  (flatten))]
    [points lines]))

(defn solved-level?
  [level]
  (let [[_ lines] (compute-elements level)]
    (->> (detect-intersections lines :thickness intersect-thickness)
         (not-any? #(= intersect-thickness (:thickness %))))))

(defn render
  "Render current level state on canvas"
  [state ctx width height]
  (let [[points lines] (compute-elements state)]
    (.clearRect ctx 0 0 width height)
    (doseq [line (detect-intersections lines :thickness intersect-thickness)]
      (draw line ctx))
    (doseq [point points]
      (draw point ctx))))

(defn game []
  (let [canvas (. js/document (getElementById "game"))
        ctx (.getContext canvas "2d")
        width (.-width canvas)
        height (.-height canvas)
        curr-level (atom 1)
        movable-circle (atom nil)
        level (atom (build-level 1 width height))]

    ;; State atoms' watchers
    (add-watch curr-level :curr-level-number
               (fn [_ _ _ new-value]
                 (let [lc   (-> (count levels) (str) (keyword))
                       next (-> (str new-value) (keyword))]
                   (reset! level (build-level new-value width height)))))
                      
    (add-watch level :level-watcher
               (fn [_ _ _ state]
                 (render state ctx width height)))

    ;; Canvas event listeners
    (.addEventListener canvas "mousedown"
                       (circle-select-handler movable-circle level))
    
    (.addEventListener canvas "mousemove"
                       (drag-handler movable-circle level))
    
    (.addEventListener canvas "mouseup"
                       ((fn [m l]
                          (fn []
                            (reset! m nil)
                            (if (solved-level? @l)
                              (swap! curr-level inc)))) movable-circle level))

    ;; Initial rendering
    (render @level ctx width height)))


(defn canvas []
  [:canvas#game {:width 800
                 :height 480}])

(defn game-component []
  (reagent/create-class {:component-did-mount game
                         :reagent-render canvas}))

(defn game-container []
  [:div#game-container
   [game-component]])

(reagent/render-component [game-container]
                          (. js/document (getElementById "app")))
