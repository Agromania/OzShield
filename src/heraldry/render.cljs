(ns heraldry.render
  (:require [heraldry.coat-of-arms.escutcheon :as escutcheon]
            [heraldry.coat-of-arms.field.environment :as environment]
            [heraldry.coat-of-arms.field.shared :as field-shared]
            [heraldry.coat-of-arms.filter :as filter]
            [heraldry.coat-of-arms.hatching :as hatching]
            [heraldry.coat-of-arms.metadata :as metadata]
            [heraldry.coat-of-arms.outline :as outline]
            [heraldry.coat-of-arms.svg :as svg]
            [heraldry.coat-of-arms.texture :as texture]
            [heraldry.coat-of-arms.tincture.core :as tincture]
            [heraldry.interface :as interface]
            [heraldry.util :as util]))

(defn coat-of-arms [path width
                    {:keys
                     [svg-export?
                      metadata-path
                      root-transform
                      texture-link] :as context}]
  (let [mode (interface/render-option :mode context)
        escutcheon (interface/render-option :escutcheon context)
        escutcheon-shadow? (when-not svg-export?
                             (interface/render-option :escutcheon-shadow? context))
        escutcheon-outline? (interface/render-option :escutcheon-outline? context)
        outline? (interface/render-option :outline? context)
        shiny? (interface/render-option :shiny? context)
        squiggly? (interface/render-option :squiggly? context)
        theme (interface/render-option :theme context)
        texture (interface/render-option :texture context)
        texture-displacement? (interface/render-option :texture-displacement? context)
        shield (escutcheon/field escutcheon)
        environment (-> (environment/transform-to-width shield width)
                        (cond->
                         squiggly? (update :shape svg/squiggly-path)))
        mask-id (util/id "mask")
        texture-id (util/id "texture")
        shiny-id (util/id "shiny")
        texture-link (or texture-link (texture/full-path texture))]
    {:environment environment
     :result [:g {:filter (when escutcheon-shadow?
                            "url(#shadow)")}
              [:g {:transform root-transform}
               (when metadata-path
                 [metadata/attribution metadata-path :arms context])
               [:defs
                (when shiny?
                  [:filter {:id shiny-id}
                   [:feDiffuseLighting {:in "SourceGraphic"
                                        :result "light"
                                        :lighting-color "white"}
                    [:fePointLight {:x 75
                                    :y 20
                                    :z 20}]]
                   [:feComposite {:in "SourceGraphic"
                                  :in2 "light"
                                  :operator "arithmetic"
                                  :k1 1
                                  :k2 0
                                  :k3 0
                                  :k4 0}]])
                (when texture-link
                  [:filter {:id texture-id}
                   [:feImage {:href texture-link
                              :x 0
                              :y 0
                              :width 150
                              :height 150
                              :preserveAspectRatio "none"
                              :result "image"}]
                   (when texture-displacement?
                     [:feDisplacementMap {:in "SourceGraphic"
                                          :in2 "image"
                                          :scale (texture/displacement texture)
                                          :xChannelSelector "R"
                                          :yChannelSelector "R"
                                          :result "displaced"}])
                   [:feComposite {:in (if texture-displacement?
                                        "displaced"
                                        "SourceGraphic")
                                  :in2 "image"
                                  :operator "arithmetic"
                                  :k1 1
                                  :k2 0
                                  :k3 0
                                  :k4 0}]])
                (when-not svg-export?
                  filter/shadow)
                [tincture/patterns theme]
                (when (= mode :hatching)
                  hatching/patterns)]
               [:defs
                [(if svg-export?
                   :mask
                   :clipPath)
                 {:id mask-id}
                 [:path {:d (:shape environment)
                         :fill "#fff"
                         :stroke "none"}]]]
               [:g {(if svg-export?
                      :mask
                      :clip-path) (str "url(#" mask-id ")")}
                [:g {:filter (when texture-link (str "url(#" texture-id ")"))}
                 [:g {:filter (when shiny?
                                (str "url(#" shiny-id ")"))}
                  [:path {:d (:shape environment)
                          :fill "#f0f0f0"}]
                  [field-shared/render (conj path :field)
                   environment
                   (-> context
                       (dissoc :metadata-path)
                       (assoc :root-escutcheon escutcheon))]]]]
               (when (or escutcheon-outline?
                         outline?)
                 [:g (outline/style context)
                  [:path {:d (:shape environment)}]])]]}))

(defn helm [path environment context]
  (let [components-path (conj path :components)
        num-components (interface/get-list-size components-path context)]
    [:<>
     (doall
      (for [idx (range num-components)]
        ^{:key idx}
        [interface/render-component
         (conj components-path idx)
         path environment
         context]))]))

(defn helms [path width {:keys
                         [root-transform] :as context}]
  (let [elements-path (conj path :elements)
        num-helms (interface/get-list-size elements-path context)]
    (if (zero? num-helms)
      {:width 0
       :height 0
       :result nil}
      (let [gap-part-fn (fn [n] (+ 2 (* 2 (- n 1))))
            gap-part (gap-part-fn num-helms)
            helm-width (/ (* gap-part width)
                          (+ (* (+ gap-part 1)
                                num-helms)
                             1))
            helm-height (* 2 helm-width)
            total-height helm-height
            gap (/ helm-width gap-part)
            helm-width-with-gap (+ helm-width gap)]
        {:width (- width
                   (* 2 gap))
         :height total-height
         :result [:g {:transform root-transform}
                  (doall
                   (for [idx (range num-helms)]
                     (let [helm-environment (environment/create
                                             nil
                                             {:bounding-box [(+ (* idx helm-width-with-gap)
                                                                gap)
                                                             (+ (* idx helm-width-with-gap)
                                                                gap
                                                                helm-width)
                                                             (- helm-height)
                                                             0]})]
                       ^{:key idx}
                       [helm (conj elements-path idx) helm-environment context])))]}))))

(defn achievement [path {:keys [short-url
                                svg-export?] :as context}]
  (let [root-scale 5
        context (assoc context :root-transform (str "scale(" root-scale "," root-scale ")"))
        coat-of-arms-angle (interface/render-option :coat-of-arms-angle context)
        scope (interface/render-option :scope context)
        coa-angle-rad (-> coat-of-arms-angle
                          (* Math/PI)
                          (/ 180))
        coa-angle-counter-rad (- (/ Math/PI 2)
                                 coa-angle-rad)
        {coat-of-arms :result
         environment :environment} (coat-of-arms
                                    (conj path :coat-of-arms)
                                    100
                                    context)
        {coat-of-arms-width :width
         coat-of-arms-height :height} environment
        {helms :result
         helms-width :width
         helms-height :height} (if (= scope :coat-of-arms)
                                 {:width 0
                                  :height 0
                                  :result nil}
                                 (helms
                                  (conj path :helms)
                                  100
                                  context))
        coat-of-arms-width (* root-scale coat-of-arms-width)
        coat-of-arms-height (* root-scale coat-of-arms-height)
        helms-width (* root-scale helms-width)
        helms-height (* root-scale helms-height)
        rotated-width-left (max
                            (* coat-of-arms-width (Math/cos coa-angle-rad))
                            (/ helms-width 2))
        rotated-width-right (max
                             (* coat-of-arms-height (Math/cos coa-angle-counter-rad))
                             (/ helms-width 2))
        rotated-height (+ (* coat-of-arms-width (Math/sin coa-angle-rad))
                          (* coat-of-arms-height (Math/sin coa-angle-counter-rad)))
        rotated? (pos? coat-of-arms-angle)
        effective-width (if rotated?
                          (+ rotated-width-left rotated-width-right)
                          coat-of-arms-width)
        effective-height (if rotated?
                           rotated-height
                           coat-of-arms-height)
        total-width effective-width
        total-height (+ effective-height helms-height)
        target-width 500
        target-height (-> target-width
                          (/ total-width)
                          (* total-height))
        target-height (if svg-export?
                        target-height
                        (min target-height
                             (* 1.5 target-width)))
        scale (min (/ target-width total-width)
                   (/ target-height total-height))
        margin 10
        font-size 20
        document-width (-> target-width (+ (* 2 margin)))
        document-height (-> target-height (+ (* 2 margin)) (+ 20)
                            (cond-> short-url
                              (+ font-size margin)))]
    [:svg (merge
           {:viewBox (str "0 0 " document-width " " document-height)}
           (if svg-export?
             {:xmlns "http://www.w3.org/2000/svg"
              :version "1.1"
              :width document-width
              :height document-height}
             {:style {:width "100%"}
              :preserveAspectRatio "xMidYMin meet"}))
     [:g {:transform (str "translate(" margin "," margin ")")}
      [:g {:transform (str
                       "translate(" (/ target-width 2) "," (/ target-height 2) ")"
                       "scale(" scale "," scale ")"
                       "translate(" (- (/ total-width 2)) "," (- (/ total-height 2)) ")")
           :style {:transition "transform 0.5s"}}
       #_[:rect {:x 0
                 :y 0
                 :width total-width
                 :height total-height
                 :style {:fill "none"
                         :stroke "#000"}}]
       [:g {:transform (str "translate(" 0 "," helms-height ")")
            :style {:transition "transform 0.5s"}}
        [:g {:transform (when rotated?
                          (str "translate(" (- rotated-width-left
                                               coat-of-arms-width) "," 0 ")"))
             :style {:transition "transform 0.5s"}}
         [:g {:transform (when rotated?
                           (str "translate(" coat-of-arms-width "," 0 ")"
                                "rotate(" (- coat-of-arms-angle) ")"
                                "translate(" (- coat-of-arms-width) "," 0 ")"))
              :style {:transition "transform 0.5s"}}
          coat-of-arms]
         [:g {:transform (when rotated?
                           (str "translate(" (/ coat-of-arms-width 2) "," 0 ")"))}
          helms]]]]]
     (when short-url
       [:text {:x margin
               :y (- document-height
                     margin)
               :text-align "start"
               :fill "#888"
               :style {:font-family "DejaVuSans"
                       :font-size font-size}}
        short-url])]))
