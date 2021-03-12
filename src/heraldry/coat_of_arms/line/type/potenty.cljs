(ns heraldry.coat-of-arms.line.type.potenty
  (:require [heraldry.util :as util]))

(defn pattern
  {:display-name "Potenty"
   :value        :potenty}
  [{:keys [height
           eccentricity
           width]}
   _line-options]
  (let [l (-> width (/ 4) (* (util/map-to-interval eccentricity 0.6 1.4)))
        t (-> width (/ 2) (- l))]
    ["l"
     [0 (- (* t height))]
     [(+ l t l) 0]
     [0 (* t height)]
     [(- l) 0]
     [0 (* t height)]
     [(+ l t l) 0]
     [0 (- (* t height))]
     [(- l) 0]]))