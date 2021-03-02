(ns heraldry.coat-of-arms.division.type.paly
  (:require [heraldry.coat-of-arms.division.options :as division-options]
            [heraldry.coat-of-arms.division.shared :as shared]
            [heraldry.coat-of-arms.infinity :as infinity]
            [heraldry.coat-of-arms.line.core :as line]
            [heraldry.coat-of-arms.options :as options]
            [heraldry.coat-of-arms.outline :as outline]
            [heraldry.coat-of-arms.svg :as svg]
            [heraldry.coat-of-arms.vector :as v]))

(defn paly-parts [{:keys [num-fields-x
                          offset-x
                          stretch-x]} top-left bottom-right line hints render-options]
  (let [offset-x (or offset-x 0)
        stretch-x (or stretch-x 1)
        width (- (:x bottom-right)
                 (:x top-left))
        pallet-width (-> width
                         (/ num-fields-x)
                         (* stretch-x))
        required-width (* pallet-width
                          num-fields-x)
        middle (-> width
                   (/ 2)
                   (+ (:x top-left)))
        x0 (-> middle
               (- (/ required-width 2))
               (+ (* offset-x
                     pallet-width)))
        y1 (:y top-left)
        y2 (:y bottom-right)
        height (- y2 y1)
        {line-down :line
         line-down-start :line-start
         line-down-end :line-end} (line/create line
                                               height
                                               :flipped? true
                                               :angle 90
                                               :render-options render-options)
        {line-up :line
         line-up-start :line-start
         line-up-end :line-end} (line/create line
                                             height
                                             :angle -90
                                             :reversed? true
                                             :render-options render-options)
        parts (->> (range num-fields-x)
                   (map (fn [i]
                          (let [x1 (+ x0 (* i pallet-width))
                                x2 (+ x1 pallet-width)
                                last-part? (-> i inc (= num-fields-x))
                                line-one-top (v/v x1 y1)
                                line-one-bottom (v/v x1 y2)
                                line-two-top (v/v x2 y1)
                                line-two-bottom (v/v x2 y2)]
                            [(cond
                               (zero? i) ["M" (v/+ line-two-top
                                                   line-down-start)
                                          (svg/stitch line-down)
                                          (infinity/path :clockwise
                                                         [:bottom :top]
                                                         [(v/+ line-two-bottom
                                                               line-down-end)
                                                          (v/+ line-two-top
                                                               line-down-start)])
                                          "z"]
                               (even? i) (concat
                                          ["M" (v/+ line-two-bottom
                                                    line-up-start)
                                           (svg/stitch line-up)]
                                          (cond
                                            last-part? [(infinity/path :clockwise
                                                                       [:top :bottom]
                                                                       [(v/+ line-two-top
                                                                             line-up-end)
                                                                        (v/+ line-one-bottom
                                                                             line-up-start)])
                                                        "z"]
                                            :else [(infinity/path :clockwise
                                                                  [:top :top]
                                                                  [(v/+ line-two-top
                                                                        line-up-end)
                                                                   (v/+ line-one-top
                                                                        line-down-start)])
                                                   (svg/stitch line-down)
                                                   (infinity/path :clockwise
                                                                  [:bottom :bottom]
                                                                  [(v/+ line-two-bottom
                                                                        line-down-end)
                                                                   (v/+ line-one-bottom
                                                                        line-up-start)])
                                                   "z"]))
                               :else (concat
                                      ["M" (v/+ line-one-top
                                                line-down-start)
                                       (svg/stitch line-down)]
                                      (cond
                                        last-part? [(infinity/path :counter-clockwise
                                                                   [:bottom :top]
                                                                   [(v/+ line-one-bottom
                                                                         line-down-end)
                                                                    (v/+ line-one-top
                                                                         line-down-start)])
                                                    "z"]
                                        :else [(infinity/path :counter-clockwise
                                                              [:bottom :bottom]
                                                              [(v/+ line-one-bottom
                                                                    line-down-end)
                                                               (v/+ line-two-bottom
                                                                    line-up-start)])
                                               (svg/stitch line-up)
                                               (infinity/path :clockwise
                                                              [:top :top]
                                                              [(v/+ line-two-top
                                                                    line-up-end)
                                                               (v/+ line-one-top
                                                                    line-down-start)])
                                               "z"])))
                             [line-one-top line-two-bottom]])))
                   vec)
        edges (->> num-fields-x
                   dec
                   range
                   (map (fn [i]
                          (let [x1 (+ x0 (* i pallet-width))
                                x2 (+ x1 pallet-width)
                                line-two-top (v/v x2 y1)
                                line-two-bottom (v/v x2 y2)]
                            (if (even? i)
                              (svg/make-path ["M" (v/+ line-two-top
                                                       line-down-start)
                                              (svg/stitch line-down)])
                              (svg/make-path ["M" (v/+ line-two-bottom
                                                       line-up-start)
                                              (svg/stitch line-up)])))))
                   vec)
        overlap (-> edges
                    (->> (map vector))
                    vec
                    (conj nil))
        outlines (when (or (:outline? render-options)
                           (:outline? hints))
                   [:g outline/style
                    (for [i (range (dec num-fields-x))]
                      ^{:key i}
                      [:path {:d (nth edges i)}])])]
    [parts overlap outlines]))

(defn render
  {:display-name "Paly"
   :value :paly
   :parts []}
  [{:keys [type fields hints] :as division} environment {:keys [render-options] :as context}]
  (let [{:keys [line layout]} (options/sanitize division (division-options/options division))
        points (:points environment)
        top-left (:top-left points)
        bottom-right (:bottom-right points)
        [parts overlap outlines] (paly-parts layout top-left bottom-right line hints render-options)]
    [:<>
     [shared/make-division
      (shared/division-context-key type) fields parts
      overlap
      environment division context]
     outlines]))
