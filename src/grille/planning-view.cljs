(ns grille.planning-view
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom]
    [clojure.walk :as walk]
    [cljs-time.core :as time]
    [grille.widget :as widget]
    [grille.time-fr :as time-fr]
    [grille.fixtures :as fix]
    [grille.utils :as utils]
    [grille.event-detail :as edetail]
    [sablono.core :as html :refer-macros [html]]
    [grille.modal :as modal])

  (:require-macros
    [devcards.core :as dc :refer [defcard deftest defcard-om]])
  )





(defn render-line [{:keys [start-time
                       end-time
                       date
                       name
                       event
                       organizers
                       attendees
                       poster
                       location
                       reg-price
                       reg-website
                       reg-facebook
                       reg-email
                       facebook
                       website
                       category
                       description
                       ] :as data}
                   owner
                   {:keys [on-click]}]

  (om/component
    (html
      [:div.planning-event {:on-click #(on-click data)}
       [:span {:class (str "color-bar " (edetail/category->class (first category)))}]
       [:span.info-wrapper
         [:span.time (str start-time " - " end-time)]
         [:span.address (:city location)]
         [:span.name name]
        ]
       ]
      )))


(defn format-date [date-str]
  (->> (time-fr/parse-date date-str)
       (time-fr/unparse "EEEE d MMMM")))


(defn bound-date-range [data after?]
  (reduce (if after? max min) (-> data :view :date-range)))

(defn get-more-event-range [data after?]
  (let [four-weeks (time/days 28)
        ; (map (comp first :date) (:events data))
        date-str (bound-date-range data after?)
        date (time-fr/parse-date date-str)]
    (if after?
       [date (time/plus date four-weeks)]
       [(time/minus date four-weeks) date])))

(defn fetch-more-event [data after?]
  (when (:fetch-events data)
    (let [[start end] (map time-fr/unparse (get-more-event-range data after?))]
        ((:fetch-events data) start end))))


(defn event-list [data owner {:keys [on-click]}]
  (om/component
    (html
      [:div.event-list
       (for [[s events] data
             :when (seq events)]
         [:div.planning-group-day
          [:div.planning-day (format-date s)]
          [:div.planning-day-events
           [:div.planning-day-events-tbody
            (om/build-all render-line events {:opts {:on-click on-click} :key :id})]]])
       ])))


(defn planning [data owner opts]
  (reify
    om/IInitState
    (init-state [this]
      {:current-event nil})
    om/IRenderState
    (render-state [this {:keys [current-event] :as state}]
      (let [on-click (fn [data] (om/set-state! owner :current-event data))
            before-date-str (bound-date-range data false)
            after-date-str (bound-date-range data true)
            events-by-day (utils/split-event-by-day (:events data))]

          (html
            [:div.planning
             [:div.planning-grille
              [:div.more-event
                [:div.more-event-before
                 {:on-click (partial fetch-more-event data false)}
                  (str "Charger plus d'évènements (avant " (format-date before-date-str) ")")]]

              (om/build event-list events-by-day {:opts {:on-click on-click}})

              [:div.more-event
               [:div.more-event-after
                 {:on-click (partial fetch-more-event data true)}
                  (str "Charger plus d'évènements (après " (format-date after-date-str) ")")]]
              ]
              [:div.planning-event-detail
               (when current-event
                (om/build edetail/event-modal-simon current-event {:opts opts :key :id}))
               ]
             ])
        )

      )))



