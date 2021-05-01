(ns heraldry.coat-of-arms.cottising
  (:require [heraldry.coat-of-arms.field.options :as field-options]
            [heraldry.coat-of-arms.line.core :as line]
            [heraldry.coat-of-arms.tincture.core :as tincture]))

(def cottise-options
  {:enabled?      {:type    :boolean
                   :default false}
   :line          line/default-options
   :opposite-line line/default-options
   :distance      {:type    :range
                   :min     -10
                   :max     20
                   :default 2}
   :thickness     {:type    :range
                   :min     0.1
                   :max     20
                   :default 2}
   :field         field-options/default-options})

(def options
  {:cottise-1          cottise-options
   :cottise-opposite-1 cottise-options
   :cottise-2          cottise-options
   :cottise-opposite-2 cottise-options})

