(ns untangle.core
  (:require [reagent.core :as reagent :refer [atom]]
            [untangle.circle :as circle :refer [Circle]]
            [untangle.line :as line :refer [Line]]
            [untangle.levels :as levels :refer [levels]]
            [untangle.constants :as consts
             :refer [circle-radius line-thickness intersect-thickness]]
            [untangle.intersection :as intersection
             :refer [detect-intersections]]
            [untangle.protocols :as prot :refer [draw]]))

(enable-console-print!)

(defn canvas []
  [:canvas#game {:width 800
                 :height 480
                 :padding-left 20}])

(defn random-circle [width height]
  (let [x (rand-int width)
        y (rand-int height)]
    (Circle. x y circle-radius)))

(defn draw-lines [lines ctx]
  (doseq [line lines]
    (draw line ctx)))

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
  [^Atom movable ^Atom level ctx width height]
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

(defn render
  "state = derefed atom"
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
        movable-circle (atom nil)
        level (atom (:1 levels))]
    (add-watch level :level-watcher
               (fn [_ _ _ state]
                 (render state ctx width height)))
                       
                                      
    (.addEventListener canvas "mousedown" (circle-select-handler movable-circle level))
    (.addEventListener canvas "mousemove" (drag-handler
                                           movable-circle
                                           level
                                           ctx width height))
    (.addEventListener canvas "mouseup" ((fn [m] #(reset! m nil)) movable-circle))
    (render @level ctx width height)))


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
