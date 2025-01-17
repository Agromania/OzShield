(ns heraldry.coat-of-arms.field.type.vairy
  (:require [heraldry.coat-of-arms.field.interface :as field-interface]
            [heraldry.coat-of-arms.outline :as outline]
            [heraldry.coat-of-arms.tincture.core :as tincture]
            [heraldry.interface :as interface]
            [heraldry.util :as util]))

(def field-type :heraldry.field.type/vairy)

(defmethod field-interface/display-name field-type [_] "Vairy")

(defmethod field-interface/part-names field-type [_] nil)

(def sqr2 1.4142135623730951)

(defn vair-default [part-width part-height]
  (let [width part-width
        height (* 2 part-height)
        middle-x (/ width 2)
        middle-y (/ height 2)
        w (/ width 4)
        h (/ middle-y (+ 1 1 sqr2))]
    {:width width
     :height height
     :pattern [:<>
               [:path {:d (str "M 0," middle-y
                               "l" w "," (- h)
                               "v" (* sqr2 (- h))
                               "L" middle-x ",0"
                               "l" w "," h
                               "v" (* sqr2 h)
                               "L" width "," middle-y
                               "z")}]
               [:path {:d (str "M 0," middle-y
                               "l" w "," h
                               "v" (* sqr2 h)
                               "L" middle-x "," height
                               "L 0," height
                               "z")}]
               [:path {:d (str "M " width "," middle-y
                               "l" (- w) "," h
                               "v" (* sqr2 h)
                               "L" middle-x "," height
                               "L " width "," height
                               "z")}]]
     :outline [:<>
               [:path {:d (str "M 0,0"
                               "h" width)}]
               [:path {:d (str "M 0," middle-y
                               "l" w "," (- h)
                               "v" (* sqr2 (- h))
                               "L" middle-x ",0"
                               "l" w "," h
                               "v" (* sqr2 h)
                               "L" width "," middle-y)}]
               [:path {:d (str "M 0," middle-y
                               "h" width)}]
               [:path {:d (str "M 0," middle-y
                               "l" w "," h
                               "v" (* sqr2 h)
                               "L" middle-x "," height
                               "L 0," height)}]
               [:path {:d (str "M " width "," middle-y
                               "l" (- w) "," h
                               "v" (* sqr2 h)
                               "L" middle-x "," height
                               "L " width "," height)}]]}))

(defn vair-counter [part-width part-height]
  (let [width part-width
        height (* 2 part-height)
        middle-x (/ width 2)
        middle-y (/ height 2)
        w (/ width 4)
        h (/ middle-y (+ 1 1 sqr2))]
    {:width width
     :height height
     :pattern [:<>
               [:path {:d (str "M 0," middle-y
                               "l" w "," (- h)
                               "v" (* sqr2 (- h))
                               "L" middle-x ",0"
                               "l" w "," h
                               "v" (* sqr2 h)
                               "L" width "," middle-y
                               "l" (- w) "," h
                               "v" (* sqr2 h)
                               "L" middle-x "," height
                               "l" (- w) "," (- h)
                               "v" (* sqr2 (- h))
                               "z")}]]
     :outline [:<>
               [:path {:d (str "M 0," middle-y
                               "l" w "," (- h)
                               "v" (* sqr2 (- h))
                               "L" middle-x ",0"
                               "l" w "," h
                               "v" (* sqr2 h)
                               "L" width "," middle-y
                               "l" (- w) "," h
                               "v" (* sqr2 h)
                               "L" middle-x "," height
                               "l" (- w) "," (- h)
                               "v" (* sqr2 (- h))
                               "z")}]]}))

(defn vair-in-pale [part-width part-height]
  (let [width part-width
        height part-height
        middle-x (/ width 2)
        w (/ width 4)
        h (/ height (+ 1 1 sqr2))]
    {:width width
     :height height
     :pattern [:<>
               [:path {:d (str "M 0," height
                               "l" w "," (- h)
                               "v" (* sqr2 (- h))
                               "L" middle-x ",0"
                               "l" w "," h
                               "v" (* sqr2 h)
                               "L" width "," height
                               "z")}]]
     :outline [:<>
               [:path {:d (str "M 0,0"
                               "h" width)}]
               [:path {:d (str "M 0," height
                               "l" w "," (- h)
                               "v" (* sqr2 (- h))
                               "L" middle-x ",0"
                               "l" w "," h
                               "v" (* sqr2 h)
                               "L" width "," height)}]
               [:path {:d (str "M 0," height
                               "h" width)}]]}))

(defn vair-en-point [part-width part-height]
  (let [width part-width
        height (* 2 part-height)
        middle-x (/ width 2)
        middle-y (/ height 2)
        w (/ width 4)
        h (/ middle-y (+ 1 1 sqr2))]
    {:width width
     :height height
     :pattern [:<>
               [:path {:d (str "M 0," middle-y
                               "l" w "," (- h)
                               "v" (* sqr2 (- h))
                               "L" middle-x ",0"
                               "l" w "," h
                               "v" (* sqr2 h)
                               "L" width "," middle-y
                               "L" width "," height
                               "l" (- w) "," (- h)
                               "v" (* sqr2 (- h))
                               "L" middle-x "," middle-y
                               "l" (- w) "," h
                               "v" (* sqr2 h)
                               "L 0," height
                               "z")}]]
     :outline [:<>
               [:path {:d (str "M 0," middle-y
                               "l" w "," (- h)
                               "v" (* sqr2 (- h))
                               "L" middle-x ",0"
                               "l" w "," h
                               "v" (* sqr2 h)
                               "L" width "," middle-y)}]
               [:path {:d (str "M " middle-x "," middle-y
                               "l" w "," h
                               "v" (* sqr2 h)
                               "L" width "," height)}]
               [:path {:d (str "M " middle-x "," middle-y
                               "l" (- w) "," h
                               "v" (* sqr2 h)
                               "L 0," height)}]]}))

(defn vair-ancien [part-width part-height]
  (let [width part-width
        height part-height
        dy 0.333
        w (/ width 4)
        h (/ height (+ 1 1 1 (* 2 dy)))]
    {:width width
     :height height
     :pattern [:<>
               [:path {:d (str "M 0,0"
                               "L" width ",0"
                               "L" width "," (- height (* dy h))
                               "a" w " " h " 0 0 1 " (- w) "," (- h)
                               "v" (- h)
                               "a" w " " h " 0 0 0 " (- (* 2 w)) ",0"
                               "v" h
                               "a" w " " h " 0 0 1 " (- w) "," h
                               "z")}]]
     :outline [:<>
               [:path {:d (str "M 0,0"
                               "h" width)}]
               [:path {:d (str "M" width "," (- height (* dy h))
                               "a" w " " h " 0 0 1 " (- w) "," (- h)
                               "v" (- h)
                               "a" w " " h " 0 0 0 " (- (* 2 w)) ",0"
                               "v" h
                               "a" w " " h " 0 0 1 " (- w) "," h)}]
               [:path {:d (str "M 0," height
                               "h" width)}]]}))

(defmethod field-interface/render-field field-type
  [path environment context]
  (let [variant (interface/get-sanitized-data (conj path :variant) context)
        num-fields-x (interface/get-sanitized-data (conj path :layout :num-fields-x) context)
        num-fields-y (interface/get-sanitized-data (conj path :layout :num-fields-y) context)
        raw-num-fields-y (interface/get-raw-data (conj path :layout :num-fields-y) context)
        offset-x (interface/get-sanitized-data (conj path :layout :offset-x) context)
        offset-y (interface/get-sanitized-data (conj path :layout :offset-y) context)
        stretch-x (interface/get-sanitized-data (conj path :layout :stretch-x) context)
        stretch-y (interface/get-sanitized-data (conj path :layout :stretch-y) context)
        outline? (or (interface/render-option :outline? context)
                     (interface/get-sanitized-data (conj path :outline?) context))
        points (:points environment)
        top-left (:top-left points)
        bottom-right (:bottom-right points)
        width (- (:x bottom-right)
                 (:x top-left))
        unstretched-part-width (-> width
                                   (/ num-fields-x))
        part-width (-> unstretched-part-width
                       (* stretch-x))
        height (- (:y bottom-right)
                  (:y top-left))
        unstretched-part-height (if raw-num-fields-y
                                  (-> height
                                      (/ num-fields-y))
                                  (-> part-width
                                      (/ 4)
                                      (* (+ 1 1 sqr2))))
        part-height (-> unstretched-part-height
                        (* stretch-y))
        middle-x (/ width 2)
        middle-y (/ height 2)
        shift-x (- middle-x
                   (* middle-x stretch-x))
        shift-y (- middle-y
                   (* middle-y stretch-y))
        pattern-id (util/id "vairy")
        vair-function (case variant
                        :counter vair-counter
                        :in-pale vair-in-pale
                        :en-point vair-en-point
                        :ancien vair-ancien
                        vair-default)
        {pattern-width :width
         pattern-height :height
         vair-pattern :pattern
         vair-outline :outline} (vair-function part-width part-height)]
    [:g
     [:defs
      (when outline?
        [:pattern {:id (str pattern-id "-outline")
                   :width pattern-width
                   :height pattern-height
                   :x (+ (:x top-left)
                         (* part-width offset-x)
                         shift-x)
                   :y (+ (:y top-left)
                         (* part-height offset-y)
                         shift-y)
                   :pattern-units "userSpaceOnUse"}
         [:g (outline/style context)
          vair-outline]])
      (for [idx (range 2)]
        ^{:key idx}
        [:pattern {:id (str pattern-id "-" idx)
                   :width pattern-width
                   :height pattern-height
                   :x (+ (:x top-left)
                         (* part-width offset-x)
                         shift-x)
                   :y (+ (:y top-left)
                         (* part-height offset-y)
                         shift-y)
                   :pattern-units "userSpaceOnUse"}
         [:rect {:x 0
                 :y 0
                 :width pattern-width
                 :height pattern-height
                 :fill (get ["#000000" "#ffffff"] idx)}]
         [:g {:fill (get ["#ffffff" "#000000"] idx)}
          vair-pattern]])]
     (doall
      (for [idx (range 2)]
        (let [mask-id (util/id "mask")]
          ^{:key idx}
          [:<>
           [:mask {:id mask-id}
            [:rect {:x -500
                    :y -500
                    :width 1100
                    :height 1100
                    :fill (str "url(#" pattern-id "-" idx ")")}]]
           [tincture/tinctured-field
            (conj path :fields idx :tincture) context
            :mask-id mask-id]])))
     (when outline?
       [:rect {:x -500
               :y -500
               :width 1100
               :height 1100
               :fill (str "url(#" pattern-id "-outline)")}])]))
