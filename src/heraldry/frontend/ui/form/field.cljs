(ns heraldry.frontend.ui.form.field
  (:require [heraldry.coat-of-arms.field.core :as field]
            [heraldry.coat-of-arms.field.options :as field-options]
            [heraldry.frontend.ui.interface :as interface]
            [heraldry.frontend.util :as util]
            [re-frame.core :as rf]))

(defn form [path {:keys [options]}]
  [:<>
   (for [option [:inherit-environment?
                 :counterchanged?
                 :type
                 :tincture
                 :line
                 :opposite-line
                 :extra-line
                 :variant
                 :thickness
                 :layout]]
     ^{:key option} [interface/form-element (conj path option) (get options option)])])

(defn name-prefix-for-part [path]
  (let [index (last path)
        parent-field-type @(rf/subscribe [:get-value (-> (drop-last 2 path)
                                                         vec
                                                         (conj :type))])]
    (when (and (int? index)
               (-> parent-field-type (or :dummy) namespace (= "heraldry.field.type")))
      (-> (field/part-name parent-field-type index)
          util/upper-case-first))))

(defmethod interface/component-node-data :heraldry.type/field [path component-data]
  {:title (util/combine ": "
                        [(name-prefix-for-part path)
                         (field/title component-data)])
   :nodes (concat (when (-> component-data :type name keyword (not= :plain))
                    [{:title "Parts"
                      :path (conj path :fields)}])
                  [{:title "Components"
                    :path (conj path :components)}])})

(defmethod interface/component-form-data :heraldry.type/field [component-data]
  {:form form
   :form-args {:options (field-options/options component-data)}})
