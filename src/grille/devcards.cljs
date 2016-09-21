(ns grille.devcards
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom]
    [clojure.walk :as walk]
    [cljs-time.core :as time]
    [grille.widget :as widget]
    [grille.time-fr :as time-fr]
    [grille.fixtures :as fix]
    [grille.event-detail :as edetail]
    [grille.modal :as modal]

    ; [grille.planning-view-devcards]
    ; [grille.calendar-devcards]
    )

  (:require-macros
    [devcards.core :as dc :refer [defcard deftest defcard-om]])
  )


(def opt {:shared {:event-type-config-dct fix/event-type-config-dct
                   :categories-config-dct fix/categories-config-dct
                   }})
#_(def data
    (fix/init-data)
  opt
  {:inspect-data true :frame true :history true}
  )


(def event fix/event)


(defn event-helper
  [data owner opts]
  (om/component
    (dom/div #js {:style #js {:position "relative"}}
             (om/build widget/event data {:opts opts})
    )))

; {:inspect-data true :frame true :history true}
#_(defcard tuile
   (dc/om-root event-helper)
    event
    opt)


(defcard popup
  "Popup normale et complète"
   (dc/om-root edetail/event-modal-simon)
    event
    opt)


(defcard popup-no-image
  "Popup sans image"
   (dc/om-root edetail/event-modal-simon)
    (assoc event :poster nil)
    opt)



(defcard popup-no-attendees-organizers-price
  "Popup, long titre, sans info pratique, organisateur et participant"
   (dc/om-root edetail/event-modal-simon)
    (merge event 
           {:name "Critique de la raison européenne, du mondialisme et de tout un tas d'autres choses très intéressantes"}
           {:attendees []
            :organizers []}
           {:reg-price ""
            :reg-website ""
            :reg-email ""
            :reg-facebook ""}
           {:website ""
            :facebook ""}
           )
    opt)


(defcard popup-description
  "Popup avec une longue description"
   (dc/om-root edetail/event-modal-simon)
    (assoc event :description (clojure.string/join " " (repeat 200 "Bonjour bonjour ! " )))
    opt)


#_(defcard event-grill-multi-col
           (dc/om-root widget/event-grill)
           data-spread-days
           {:inspect-data true :frame true :history true})

