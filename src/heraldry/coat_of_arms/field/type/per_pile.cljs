(ns heraldry.coat-of-arms.field.type.per-pile
  (:require [heraldry.coat-of-arms.field.interface :as field-interface]
            [heraldry.coat-of-arms.field.shared :as shared]
            [heraldry.coat-of-arms.infinity :as infinity]
            [heraldry.coat-of-arms.line.core :as line]
            [heraldry.coat-of-arms.shared.pile :as pile]
            [heraldry.math.vector :as v]
            [heraldry.interface :as interface]
            [heraldry.math.svg.path :as path]
            [heraldry.math.svg.core :as svg]
            [heraldry.util :as util]))

(def field-type :heraldry.field.type/per-pile)

(defmethod field-interface/display-name field-type [_] "Per pile")

(defmethod field-interface/part-names field-type [_] nil)

(defmethod field-interface/render-field field-type
  [path environment context]
  (let [line (interface/get-sanitized-data (conj path :line) context)
        opposite-line (interface/get-sanitized-data (conj path :opposite-line) context)
        origin (interface/get-sanitized-data (conj path :origin) context)
        anchor (interface/get-sanitized-data (conj path :anchor) context)
        geometry (interface/get-sanitized-data (conj path :geometry) context)
        outline? (or (interface/render-option :outline? context)
                     (interface/get-sanitized-data (conj path :outline?) context))
        anchor (-> anchor
                   (assoc :type :edge))
        geometry (-> geometry
                     (assoc :stretch 1))
        points (:points environment)
        top-left (:top-left points)
        top-right (:top-right points)
        bottom-left (:bottom-left points)
        bottom-right (:bottom-right points)
        thickness-base (if (#{:left :right} (:point origin))
                         (:height environment)
                         (:width environment))
        {origin-point :origin
         point :point
         thickness :thickness} (pile/calculate-properties
                                environment
                                origin
                                (cond-> anchor
                                  (#{:top-right
                                     :right
                                     :bottom-left} (:point origin)) (update :angle #(when %
                                                                                      (- %))))
                                geometry
                                thickness-base
                                (case (:point origin)
                                  :top-left 0
                                  :top 90
                                  :top-right 180
                                  :left 0
                                  :right 180
                                  :bottom-left 0
                                  :bottom -90
                                  :bottom-right 180
                                  0))
        {left-point :left
         right-point :right} (pile/diagonals origin-point point thickness)
        intersection-left (-> (v/environment-intersections point left-point environment)
                              last)
        intersection-right (-> (v/environment-intersections point right-point environment)
                               last)
        end-left (-> intersection-left
                     (v/sub point)
                     v/abs)
        end-right (-> intersection-right
                      (v/sub point)
                      v/abs)
        end (max end-left end-right)
        line (-> line
                 (update-in [:fimbriation :thickness-1] (util/percent-of thickness-base))
                 (update-in [:fimbriation :thickness-2] (util/percent-of thickness-base)))
        {line-left :line
         line-left-start :line-start
         line-left-end :line-end
         :as line-left-data} (line/create line
                                          point left-point
                                          :reversed? true
                                          :real-start 0
                                          :real-end end
                                          :context context
                                          :environment environment)
        {line-right :line
         line-right-start :line-start
         line-right-end :line-end
         :as line-right-data} (line/create opposite-line
                                           point right-point
                                           :real-start 0
                                           :real-end end
                                           :context context
                                           :environment environment)
        parts [[["M" (v/add point
                            line-right-start)
                 (path/stitch line-right)
                 (infinity/path
                  :counter-clockwise
                  (cond
                    (#{:top-left
                       :top
                       :top-right} (:point origin)) [:top :bottom]
                    (#{:left} (:point origin)) [:left :right]
                    (#{:right} (:point origin)) [:right :left]
                    (#{:bottom-left
                       :bottom
                       :bottom-right} (:point origin)) [:bottom :top]
                    :else [:top :bottom])
                  [(v/add point
                          line-right-end)
                   (v/add point
                          line-right-start)])
                 "z"]
                                             ;; TODO: these fields inherit the whole parent
                                             ;; environment points, but it can probably be reduced
                [top-left top-right
                 bottom-left bottom-right]]

               [["M" (v/add left-point
                            line-left-start)
                 (path/stitch line-left)
                 (path/stitch line-right)
                 "z"]
                                             ;; TODO: these fields inherit the whole parent
                                             ;; environment points, but it can probably be reduced
                [top-left top-right
                 bottom-left bottom-right]]

               [["M" (v/add left-point
                            line-left-start)
                 (path/stitch line-left)
                 (infinity/path
                  :counter-clockwise
                  (cond
                    (#{:top-left
                       :top
                       :top-right} (:point origin)) [:bottom :top]
                    (#{:left} (:point origin)) [:right :left]
                    (#{:right} (:point origin)) [:left :right]
                    (#{:bottom-left
                       :bottom
                       :bottom-right} (:point origin)) [:top :bottom]
                    :else [:bottom :top])
                  [(v/add left-point
                          line-left-end)
                   (v/add left-point
                          line-left-start)])
                 "z"]
                                             ;; TODO: these fields inherit the whole parent
                                             ;; environment points, but it can probably be reduced
                [top-left top-right
                 bottom-left bottom-right]]]]
    [:<>
     [shared/make-subfields
      path parts
      [:all nil nil]
      environment context]
     [line/render line [line-left-data
                        line-right-data] left-point outline? context]]))
