(ns heraldry.coat-of-arms.render
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
        escutcheon-override (interface/render-option :escutcheon-override context)
        escutcheon-shadow? (when-not svg-export?
                             (interface/render-option :escutcheon-shadow? context))
        escutcheon-outline? (interface/render-option :escutcheon-outline? context)
        outline? (interface/render-option :outline? context)
        shiny? (interface/render-option :shiny? context)
        squiggly? (interface/render-option :squiggly? context)
        theme (interface/render-option :theme context)
        texture (interface/render-option :texture context)
        texture-displacement? (interface/render-option :texture-displacement? context)
        escutcheon (interface/get-sanitized-data (conj path :escutcheon) context)
        escutcheon (if (not= escutcheon-override :none)
                     escutcheon-override
                     escutcheon)
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
                 [:g outline/style
                  [:path {:d (:shape environment)}]])]]}))
