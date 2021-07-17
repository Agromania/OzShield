(ns heraldry.frontend.ui.form.collection
  (:require [heraldry.collection.options] ;; needed for defmethods
            [heraldry.frontend.state :as state]
            [heraldry.frontend.ui.interface :as interface]))

(defn form [path _]
  [:<>
   (for [option [:num-columns]]
     ^{:key option} [interface/form-element (conj path option)])])

(defmethod interface/component-node-data :heraldry.component/collection [path component-data _component-options]
  {:title "Arms"
   :buttons [{:icon "fas fa-plus"
              :title "Add"
              :menu [{:title "Arms"
                      :handler #(state/dispatch-on-event % [:add-element (conj path :elements) {}])}]}]
   :nodes (->> component-data
               :elements
               count
               range
               (map (fn [idx]
                      (let [component-path (conj path :elements idx)]
                        {:path component-path
                         :buttons [{:icon "fas fa-chevron-up"
                                    :disabled? (zero? idx)
                                    :tooltip "move down"
                                    :handler #(state/dispatch-on-event % [:move-element component-path (dec idx)])}
                                   {:icon "fas fa-chevron-down"
                                    :disabled? (= idx (dec (count (:elements component-data))))
                                    :tooltip "move up"
                                    :handler #(state/dispatch-on-event % [:move-element component-path (inc idx)])}
                                   {:icon "far fa-trash-alt"
                                    :tooltip "remove"
                                    :handler #(state/dispatch-on-event
                                               % [:remove-element component-path])}]})))
               vec)})

(defmethod interface/component-form-data :heraldry.component/collection [_path _component-data _component-options]
  {:form form})
