(ns heraldry.frontend.ui.element.submenu
  (:require [heraldry.frontend.state :as state]
            [re-frame.core :as rf]))

(rf/reg-sub :ui-submenu-open?
  (fn [db [_ path]]
    (get-in db (conj state/ui-submenu-open?-path path))))

(rf/reg-event-db :ui-submenu-close-all
  (fn [db _]
    (assoc-in db state/ui-submenu-open?-path nil)))

(rf/reg-event-db :ui-submenu-open
  (fn [db [_ path]]
    (-> db
        (update-in state/ui-submenu-open?-path
                   (fn [open-flags]
                     (->> open-flags
                          (keep (fn [[key value]]
                                  (when (= key
                                           (take (count key) path))
                                    [key value])))
                          (into {}))))
        (assoc-in (conj state/ui-submenu-open?-path path) true))))

(rf/reg-event-db :ui-submenu-close
  (fn [db [_ path]]
    (assoc-in db (conj state/ui-submenu-open?-path path) false)))

(defn submenu [path title link-name styles & content]
  (let [submenu-id path
        submenu-open? @(rf/subscribe [:ui-submenu-open? submenu-id])]
    [:div.ui-submenu-setting {:style {:display "inline-block"}
                              :on-click #(.stopPropagation %)}
     [:a {:on-click #(state/dispatch-on-event % [:ui-submenu-open submenu-id])}
      link-name]
     (when submenu-open?
       [:div.ui-component.ui-submenu {:style styles}
        [:div.ui-component-header [:a {:on-click #(state/dispatch-on-event % [:ui-submenu-close submenu-id])}
                                   [:i.far.fa-times-circle]]
         " " title]
        (into [:div.content]
              content)])]))
