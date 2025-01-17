(ns heraldry.coat-of-arms.ordinary.type.bend
  (:require [heraldry.coat-of-arms.angle :as angle]
            [heraldry.coat-of-arms.cottising :as cottising]
            [heraldry.coat-of-arms.field.shared :as field-shared]
            [heraldry.coat-of-arms.line.core :as line]
            [heraldry.coat-of-arms.ordinary.interface :as ordinary-interface]
            [heraldry.math.vector :as v]
            [heraldry.interface :as interface]
            [heraldry.math.svg.path :as path]
            [heraldry.math.svg.core :as svg]
            [heraldry.util :as util]))

(def ordinary-type :heraldry.ordinary.type/bend)

(defmethod ordinary-interface/display-name ordinary-type [_] "Bend")

(defmethod ordinary-interface/render-ordinary ordinary-type
  [path _parent-path environment {:keys [override-middle-real-start
                                         override-middle-real-end
                                         override-real-start
                                         override-real-end
                                         override-center-point] :as context}]
  (let [line (interface/get-sanitized-data (conj path :line) context)
        opposite-line (interface/get-sanitized-data (conj path :opposite-line) context)
        origin (interface/get-sanitized-data (conj path :origin) context)
        anchor (interface/get-sanitized-data (conj path :anchor) context)
        size (interface/get-sanitized-data (conj path :geometry :size) context)
        outline? (or (interface/render-option :outline? context)
                     (interface/get-sanitized-data (conj path :outline?) context))
        points (:points environment)
        top (:top points)
        bottom (:bottom points)
        width (:width environment)
        height (:height environment)
        band-height (-> size
                        ((util/percent-of height)))
        {origin-point :real-origin
         anchor-point :real-anchor} (angle/calculate-origin-and-anchor
                                     environment
                                     origin
                                     anchor
                                     band-height
                                     nil)
        center-point (or override-center-point
                         (v/line-intersection origin-point anchor-point
                                              top bottom))
        direction (v/sub anchor-point origin-point)
        direction (-> (v/v (-> direction :x Math/abs)
                           (-> direction :y Math/abs))
                      v/normal)
        direction-orthogonal (v/orthogonal direction)
        [middle-real-start
         middle-real-end] (if (and override-middle-real-start
                                   override-middle-real-end)
                            [override-middle-real-start
                             override-middle-real-end]
                            (v/environment-intersections
                             origin-point
                             (v/add origin-point direction)
                             environment))
        band-length (-> (v/sub middle-real-start center-point)
                        v/abs
                        (* 2))
        middle-start (-> direction
                         (v/mul -30)
                         (v/add middle-real-start))
        middle-end (-> direction
                       (v/mul 30)
                       (v/add middle-real-end))
        width-offset (-> direction-orthogonal
                         (v/mul band-height)
                         (v/div 2))
        ordinary-top-left (v/add middle-real-start width-offset)
        first-start (v/add middle-start width-offset)
        first-end (v/add middle-end width-offset)
        second-start (v/sub middle-start width-offset)
        second-end (v/sub middle-end width-offset)
        [first-real-start
         first-real-end] (v/environment-intersections
                          first-start
                          first-end
                          environment)
        [second-real-start
         second-real-end] (v/environment-intersections
                           second-start
                           second-end
                           environment)
        real-start (or override-real-start
                       (min (-> (v/sub first-real-start first-start)
                                (v/abs))
                            (-> (v/sub second-real-start second-start)
                                (v/abs))))
        real-end (or override-real-end
                     (max (-> (v/sub first-real-start first-start)
                              (v/abs))
                          (-> (v/sub second-real-end second-start)
                              (v/abs))))
        angle (v/angle-to-point middle-start middle-end)
        line (-> line
                 (update-in [:fimbriation :thickness-1] (util/percent-of height))
                 (update-in [:fimbriation :thickness-2] (util/percent-of height)))
        opposite-line (-> opposite-line
                          (update-in [:fimbriation :thickness-1] (util/percent-of height))
                          (update-in [:fimbriation :thickness-2] (util/percent-of height)))
        {line-one :line
         line-one-start :line-start
         line-one-min :line-min
         :as line-one-data} (line/create line
                                         first-start
                                         first-end
                                         :real-start real-start
                                         :real-end real-end
                                         :context context
                                         :environment environment)
        {line-reversed :line
         line-reversed-start :line-start
         line-reversed-min :line-min
         :as line-reversed-data} (line/create opposite-line
                                              second-start
                                              second-end
                                              :reversed? true
                                              :real-start real-start
                                              :real-end real-end
                                              :context context
                                              :environment environment)
        counterchanged? (interface/get-sanitized-data (conj path :field :counterchanged?) context)
        inherit-environment? (interface/get-sanitized-data (conj path :field :inherit-environment?) context)
        use-parent-environment? (or counterchanged?
                                    inherit-environment?)
        part [["M" (v/add first-start
                          line-one-start)
               (path/stitch line-one)
               "L" (v/add second-end
                          line-reversed-start)
               (path/stitch line-reversed)
               "L" (v/add first-start
                          line-one-start)
               "z"]
              (if use-parent-environment?
                [first-real-start first-real-end
                 second-real-start second-real-end]
                [(v/v 0 0)
                 (v/v band-length band-height)])]
        cottise-context (merge
                         context
                         {:override-real-start real-start
                          :override-real-end real-end})]
    [:<>
     [field-shared/make-subfield
      (conj path :field) part
      :all
      environment
      (-> context
          (assoc :transform (when (not use-parent-environment?)
                              (str "translate(" (v/->str ordinary-top-left) ")"
                                   "rotate(" angle ")"))))]
     [line/render line [line-one-data] first-start outline? context]
     [line/render opposite-line [line-reversed-data] second-end outline? context]
     [cottising/render-bend-cottise
      :cottise-1 :cottise-2 :cottise-1
      path environment cottise-context
      :distance-fn (fn [distance thickness]
                     (-> (+ distance)
                         (+ (/ thickness 2))
                         (/ 100)
                         (* height)
                         (+ (/ band-height 2))
                         (- line-one-min)))
      :alignment :right
      :width width
      :height height
      :angle angle
      :direction-orthogonal direction-orthogonal
      :center-point center-point
      :middle-real-start-fn (fn [point-offset]
                              (v/add middle-real-start point-offset))
      :middle-real-end-fn (fn [point-offset]
                            (v/add middle-real-end point-offset))]
     [cottising/render-bend-cottise
      :cottise-opposite-1 :cottise-opposite-2 :cottise-opposite-1
      path environment cottise-context
      :distance-fn (fn [distance thickness]
                     (-> (+ distance)
                         (+ (/ thickness 2))
                         (/ 100)
                         (* height)
                         (+ (/ band-height 2))
                         (- line-reversed-min)))
      :alignment :left
      :width width
      :height height
      :angle angle
      :direction-orthogonal direction-orthogonal
      :center-point center-point
      :middle-real-start-fn (fn [point-offset]
                              (v/sub middle-real-start point-offset))
      :middle-real-end-fn (fn [point-offset]
                            (v/sub middle-real-end point-offset))
      :swap-lines? true]]))
