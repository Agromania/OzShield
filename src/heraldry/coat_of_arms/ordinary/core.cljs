(ns heraldry.coat-of-arms.ordinary.core
  (:require [heraldry.coat-of-arms.ordinary.type.base :as base]
            [heraldry.coat-of-arms.ordinary.type.bend :as bend]
            [heraldry.coat-of-arms.ordinary.type.bend-sinister :as bend-sinister]
            [heraldry.coat-of-arms.ordinary.type.chevron :as chevron]
            [heraldry.coat-of-arms.ordinary.type.chief :as chief]
            [heraldry.coat-of-arms.ordinary.type.cross :as cross]
            [heraldry.coat-of-arms.ordinary.type.fess :as fess]
            [heraldry.coat-of-arms.ordinary.type.pale :as pale]
            [heraldry.coat-of-arms.ordinary.type.saltire :as saltire]
            [heraldry.util :as util]))

(def ordinaries
  [#'pale/render
   #'fess/render
   #'chief/render
   #'base/render
   #'bend/render
   #'bend-sinister/render
   #'cross/render
   #'saltire/render
   #'chevron/render])

(def kinds-function-map
  (->> ordinaries
       (map (fn [function]
              [(-> function meta :value) function]))
       (into {})))

(def choices
  (->> ordinaries
       (map (fn [function]
              [(-> function meta :display-name) (-> function meta :value)]))))

(def ordinary-map
  (util/choices->map choices))

(defn render [{:keys [type] :as ordinary} parent environment context]
  (let [function (get kinds-function-map type)]
    [function ordinary parent environment context]))