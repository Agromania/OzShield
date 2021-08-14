(ns heraldry.ribbon
  (:require [heraldry.coat-of-arms.catmullrom :as catmullrom]
            [heraldry.coat-of-arms.vector :as v]
            [heraldry.font :as font]
            [heraldry.interface :as interface]
            [heraldry.options :as options]))

(def default-options
  {:thickness {:type :range
               :default 10
               :min 5
               :max 30
               :ui {:label "Thickness"
                    :step 0.1}}
   :edge-angle {:type :range
                :default 0
                :min -90
                :max 90
                :ui {:label "Edge angle"
                     :step 1}}
   :end-split {:type :range
               :default 0
               :min 0
               :max 80
               :ui {:label "End split"
                    :step 1}}})

(defn options [data]
  default-options)

(defn curve-segments [full-curve
                      last-index end-t last-edge-vector
                      index ts edge-vector]
  (assert (-> ts count (<= 2)) "curve-segments only supports 2 tangent points per segment")
  (let [full-curve (vec full-curve)
        first-leg (when-not (= last-index index 0)
                    (cond-> (get full-curve last-index)
                      end-t (->
                             (catmullrom/split-bezier end-t)
                             :curve2)))]
    (if (empty? ts)
      [[(-> (concat (when first-leg [first-leg])
                    (subvec full-curve (inc last-index) (inc index)))
            vec)
        last-edge-vector
        edge-vector]]
      (let [[t1 t2] ts
            first-split (-> full-curve
                            (get index)
                            (catmullrom/split-bezier t1))]
        (cond-> [[(-> (concat (when first-leg [first-leg])
                              (when (> index
                                       (inc last-index))
                                (subvec full-curve (inc last-index) index))
                              [(:curve1 first-split)])
                      vec)
                  last-edge-vector
                  edge-vector]]
          t2 (conj [[(-> (:curve2 first-split)
                         (catmullrom/split-bezier (/ (- t2 t1)
                                                     (- 1 t1)))
                         :curve1)]
                    edge-vector
                    edge-vector]))))))

(defn split-curve [full-curve tangent-points min-edge-vector max-edge-vector]
  (if (empty? tangent-points)
    [full-curve]
    (->> (concat [[0 nil min-edge-vector]]
                 tangent-points
                 [[(-> full-curve count dec) nil max-edge-vector]])
         (partition 2 1)
         (mapcat (fn [[[last-index last-ts last-edge-vector]
                       [index ts edge-vector]]]
                   (curve-segments full-curve
                                   last-index (last last-ts) last-edge-vector
                                   index ts edge-vector)))
         vec)))

(defn generate-curves [points edge-angle]
  (let [curve (catmullrom/catmullrom points)
        num-legs (count curve)
        tangent-points (-> (keep-indexed
                            (fn [idx leg]
                              ;; TODO: probably better to calculate the angle based
                              ;; on the average x-value of the leg
                              (let [base-edge-vector (v/v 0 1)
                                    leg-edge-angle (-> (* 2 edge-angle)
                                                       (/ (max 1
                                                               (dec num-legs)))
                                                       (* idx)
                                                       (- edge-angle))
                                    edge-vector (-> base-edge-vector
                                                    (v/rotate (- leg-edge-angle)))
                                    ts (catmullrom/calculate-tangent-points leg ((juxt :x :y) edge-vector))]
                                (when (seq ts)
                                  [idx ts edge-vector])))
                            curve))
        curves-and-edge-vectors (split-curve curve tangent-points
                                             (-> (v/v 0 1)
                                                 (v/rotate edge-angle))
                                             (-> (v/v 0 1)
                                                 (v/rotate (- edge-angle))))]
    {:curve curve
     :curves (->> curves-and-edge-vectors
                  (map first)
                  vec)
     :edge-vectors (->> curves-and-edge-vectors
                        (map (comp vec (partial drop 1)))
                        vec)}))

(def default-segment-options
  {:type {:type :choice
          :choices [["Text" :heraldry.ribbon.segment/foreground-with-text]
                    ["Foreground" :heraldry.ribbon.segment/foreground]
                    ["Background" :heraldry.ribbon.segment/background]]
          :ui {:label "Type"
               :form-type :radio-select}}
   :z-index {:type :range
             :min 0
             :max 100
             :integer? true
             :ui {:label "Layer"}}
   :offset-x {:type :range
              :default 0
              :min -0.5
              :max 0.5
              :ui {:label "Offset x"
                   :step 0.01}}
   :offset-y {:type :range
              :default 0
              :min -0.5
              :max 0.5
              :ui {:label "Offset y"
                   :step 0.01}}
   :font-scale {:type :range
                :default 0.8
                :min 0.01
                :max 1
                :ui {:label "Font scale"
                     :step 0.01}}
   :spacing {:type :range
             :default 0.25
             :min -0.5
             :max 2
             :ui {:label "Spacing"
                  :step 0.01}}
   :font font/default-options})

(defn segment-options [data]
  (when-let [segment-type (:type data)]
    (case segment-type
      :heraldry.ribbon.segment/foreground-with-text (options/pick default-segment-options
                                                                  [[:type]
                                                                   [:z-index]
                                                                   [:font]
                                                                   [:font-scale]
                                                                   [:spacing]
                                                                   [:offset-x]
                                                                   [:offset-y]])
      (options/pick default-segment-options
                    [[:type]
                     [:z-index]]))))

(defmethod interface/component-options :heraldry.component/ribbon-segment [_path data]
  (segment-options data))
