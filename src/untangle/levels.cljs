(ns untangle.levels
  (:require [untangle.circle :as circle :refer [Circle]]))

(def levels
  {:1 
   {:circles {:0 (Circle. 50 40 10)
              :1 (Circle. 400 70 10)
              :2 (Circle. 500 400 10)
              :3 (Circle. 80 350 10)}
    :adjacent {:0 [:2 :3]
               :1 [:2 :3]}}})
