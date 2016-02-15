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
