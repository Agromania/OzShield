(ns heraldry.frontend.ui.interface
  (:require [heraldry.interface :as interface]
            [heraldry.options :as options]
            [re-frame.core :as rf]
            [reagent.core :as r]))

;; component-node-data

(defmulti component-node-data (fn [path]
                                (interface/effective-component-type
                                 path
                                 @(rf/subscribe [:get-value (conj path :type)]))))

(defmethod component-node-data nil [_path]
  {:title "unknown"})

;; component-form-data

(defmulti component-form-data (fn [path component-data _component-options]
                                (interface/effective-component-type
                                 path
                                 @(rf/subscribe [:get-value (conj path :type)]))))

(defmethod component-form-data nil [_path _component-data _component-options]
  {:form (fn [_path _form-data]
           [:div])
   :form-args {}})

;; form-element

(defn default-element [type]
  (case type
    :choice :select
    :boolean :checkbox
    :range :range
    :text :text-field
    nil))

(rf/reg-sub :get-options
  (fn [[_ path] _]
    (rf/subscribe [:get-value path]))

  (fn [data [_ path]]
    (interface/component-options path data)))

(rf/reg-sub :get-relevant-options
  (fn [[_ path] _]
    (or (->> (range (count path) 0 -1)
             (keep (fn [idx]
                     (let [option-path (subvec path 0 idx)
                           relative-path (subvec path idx)
                           sub (rf/subscribe [:get-options option-path])]
                       (when @sub
                         [sub (r/atom relative-path)]))))
             first)
        [(r/atom nil) (r/atom nil)]))

  (fn [[relevant-options relative-path] [_ _path]]
    (get-in relevant-options relative-path)))

(rf/reg-sub :get-sanitized-value
  (fn [[_ path] _]
    [(rf/subscribe [:get-value path])
     (rf/subscribe [:get-relevant-options path])])

  (fn [[value options] [_ _path]]
    ;; TODO: find better way to determine option leaf or branch
    (if (and (map? options)
             (or (-> options :type not)
                 (-> options :type :type)))
      (options/sanitize value options)
      (options/get-value value options))))

(rf/reg-sub :get-form-element-type
  (fn [[_ path] _]
    (rf/subscribe [:get-relevant-options path]))

  (fn [options [_ _path]]
    (or
     (-> options :ui :form-type)
     (-> options :type default-element))))

(defmulti form-element (fn [path]
                         @(rf/subscribe [:get-form-element-type path])))

(defmethod form-element nil [path]
  (when-let [options @(rf/subscribe [:get-relevant-options path])]
    [:div (str "not implemented: " path options)]))
