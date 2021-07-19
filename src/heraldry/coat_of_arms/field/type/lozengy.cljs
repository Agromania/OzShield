(ns heraldry.coat-of-arms.field.type.lozengy
  (:require [heraldry.coat-of-arms.field.interface :as interface]
            [heraldry.options :as options]
            [heraldry.coat-of-arms.outline :as outline]
            [heraldry.coat-of-arms.svg :as svg]
            [heraldry.coat-of-arms.tincture.core :as tincture]
            [heraldry.util :as util]))

(def field-type
  :heraldry.field.type/lozengy)

(defmethod interface/display-name field-type [_] "Lozengy")

(defmethod interface/part-names field-type [_] nil)

(defmethod interface/render-field field-type
  [path environment context]
  (let [num-fields-x (options/sanitized-value (conj path :layout :num-fields-x) context)
        num-fields-y (options/sanitized-value (conj path :layout :num-fields-y) context)
        raw-num-fields-y (options/raw-value (conj path :layout :num-fields-y) context)
        offset-x (options/sanitized-value (conj path :layout :offset-x) context)
        offset-y (options/sanitized-value (conj path :layout :offset-y) context)
        stretch-x (options/sanitized-value (conj path :layout :stretch-x) context)
        stretch-y (options/sanitized-value (conj path :layout :stretch-y) context)
        rotation (options/sanitized-value (conj path :layout :rotation) context)
        outline? (or (options/render-option :outline? context)
                     (options/sanitized-value (conj path :outline?) context))
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
                                  part-width)
        part-height (-> unstretched-part-height
                        (* stretch-y))
        middle-x (/ width 2)
        pattern-id (util/id "lozengy")
        lozenge-shape (svg/make-path ["M" [(/ part-width 2) 0]
                                      "L" [part-width (/ part-height 2)]
                                      "L" [(/ part-width 2) part-height]
                                      "L" [0 (/ part-height 2)]
                                      "z"])]
    [:g
     [:defs
      (when outline?
        [:pattern {:id (str pattern-id "-outline")
                   :width part-width
                   :height part-height
                   :x (+ (* part-width offset-x)
                         (:x top-left)
                         (- middle-x
                            (* middle-x stretch-x)))
                   :y (+ (* part-height offset-y)
                         (:y top-left))
                   :pattern-units "userSpaceOnUse"}
         [:g outline/style
          [:path {:d lozenge-shape}]]])
      [:pattern {:id (str pattern-id "-0")
                 :width part-width
                 :height part-height
                 :x (+ (* part-width offset-x)
                       (:x top-left)
                       (- middle-x
                          (* middle-x stretch-x)))
                 :y (+ (* part-height offset-y)
                       (:y top-left))
                 :pattern-units "userSpaceOnUse"}
       [:rect {:x 0
               :y 0
               :width part-width
               :height part-height
               :fill "#000000"}]
       [:path {:d lozenge-shape
               :fill "#ffffff"}]]
      [:pattern {:id (str pattern-id "-1")
                 :width part-width
                 :height part-height
                 :x (+ (* part-width offset-x)
                       (:x top-left)
                       (- middle-x
                          (* middle-x stretch-x)))
                 :y (+ (* part-height offset-y)
                       (:y top-left))
                 :pattern-units "userSpaceOnUse"}
       [:rect {:x 0
               :y 0
               :width part-width
               :height part-height
               :fill "#ffffff"}]
       [:path {:d lozenge-shape
               :fill "#000000"}]]]
     [:g {:transform (str "rotate(" (- rotation) ")")}
      (doall
       (for [idx (range 2)]
         (let [mask-id (util/id "mask")
               tincture (options/sanitized-value (conj path :fields idx :tincture) context)]
           ^{:key idx}
           [:<>
            [:mask {:id mask-id}
             [:rect {:x -500
                     :y -500
                     :width 1100
                     :height 1100
                     :fill (str "url(#" pattern-id "-" idx ")")}]]
            [:g {:mask (str "url(#" mask-id ")")}
             [:rect {:x -500
                     :y -500
                     :width 1100
                     :height 1100
                     :transform (str "rotate(" rotation ")")
                     :fill (tincture/pick2 tincture context)}]]])))
      (when outline?
        [:rect {:x -500
                :y -500
                :width 1100
                :height 1100
                :fill (str "url(#" pattern-id "-outline)")}])]]))
