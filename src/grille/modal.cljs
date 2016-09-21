(ns grille.modal
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as d]))


;; https://github.com/racehub/om-bootstrap/blob/develop/src/om_bootstrap/modal.cljs
;; http://om-bootstrap.herokuapp.com/components#modals

(defn modal [data owner {:keys [on-click]}]
  (reify
    om/IInitState
    (init-state [_])
    om/IRenderState
    (render-state [_ {:keys [editing]}]
      (let [{:keys [header footer body]} (om/get-props owner)]
        (d/div #js {:className "cmodal cfade"
                    :onClick on-click}
               (d/div #js {:className "cmodal-dialog"
                           :onClick #(.stopPropagation %)}
                      (d/div #js {:className "cmodal-content"}
                             (d/div #js {:className "cmodal-header"}
                                    (d/div #js {:className "cmodal-dispose"
                                                :onClick on-click}
                                           "CLOSE")
                                    header)
                             (d/div #js {:className "cmodal-body"} body)
                             (d/div #js {:className "cmodal-footer"} footer))))))))


(defn modal-simple [data owner {:keys [on-click on-next on-prev]}]
  (reify
    om/IInitState
    (init-state [_])
    om/IRenderState
    (render-state [_ {:keys [editing]}]
      (let [{:keys [header footer body prev? next?]} (om/get-props owner)]
        (prn "modal-simple: " prev? next?)
        (d/div #js {:className "cmodal cfade"
                    :onClick on-click}
               (d/div #js {:className "cmodal-simple-dialog"
                           :onClick #(.stopPropagation %)}
                      (d/div #js {:className "cmodal-simple-dialog-inner"}
                        (d/div #js {:className "cmodal-content"}
                                body))
                      (when (and on-prev prev?)
                        (d/div #js {:className "cmodal-arrow-left"
                                    :onClick (fn [e]
                                               #(.stopPropagation e)
                                               (on-prev prev?))}))
                      (when (and on-next next?)
                        (d/div #js {:className "cmodal-arrow-right"
                                    :onClick (fn [e]
                                               #(.stopPropagation e)
                                               (on-next next?))}))
                      ))))))

