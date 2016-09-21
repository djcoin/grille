(ns grille.calendar
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom]
    [clojure.walk :as walk]
    [cljs-time.core :as time]
    [grille.widget :as widget]
    [grille.time-fr :as time-fr]
    [cljs-time.format :as timef]
    [grille.fixtures :as fix]
    [grille.utils :as utils]
    [grille.planning-view :as planning]
    [grille.event-detail :as edetail]
    [sablono.core :as html :refer-macros [html]]
    [grille.modal :as modal])
  )


; http://www.lechemindelanature.com/evenements/

; evenement

;; sorted ;;
; (let [period (time/days offset)
;         one-day (time/days 1)]
; 
;     (reduce
;       (fn [acc _]
;         (conj acc (time/plus (last acc) one-day)))
;       [(time/plus start-date period)]
;       (range 1 n)))


(defn- day-range
  "Inclusive"
  [start end]
    (->> (range)
         (map #(time/plus start (time/days %)))
         (take-while #(not (time/after? % end)))))


(defn data->date [data]
  (-> data :view :date-range first time-fr/parse-date))


(defn get-date-range
  "Extract lower and higher bound days for a month to be shown"
  [date]
  (let [start-month (time/first-day-of-the-month date)
        start (time-fr/get-monday start-month)
        end-month (time/last-day-of-the-month date)
        end (if (= (time/day-of-week end-month) 1)
              end-month
              (time/plus (time-fr/get-monday end-month) (time/days 6)))
        ]
    [start end]))



(defn get-shown-dates [data]
  (let [date (data->date data)
        [start end] (get-date-range date)]
    {:days (day-range start end)
     :date date
     :month (time/month date)}
    ))


; getNumberOfDaysInMonth
; first-day-of-month
; last-day-of-month


(defn gen-has-event? 
  [data]
  ;; [:view :date-range] only contains one date,
  ;; extract dates from events that are the only one to be shown
  (let [by-day (utils/split-event-by-day (:events data))
        hm-day (into {} by-day)]
    [
     by-day
    (fn has-event?
      [d]
      (seq (get hm-day (time-fr/unparse d))))
     ]))


(defn change-month [data date after?]
  (let [delta (time/months (if after? 1 -1))
        next-date (time/plus date delta)]
    ; (om/update! data [:view :date-range] #{next-date})
    (when (:fetch-events data)
        (let [[start end] (map time-fr/unparse (get-date-range next-date))]
            ((:fetch-events data) start end)))
    ))


(defn cal-event-list [data owner opts]
  (om/component
    (html
      [:div.cal-event-list
       (om/build planning/event-list data {:opts opts})]
  )))


(defn event-detail-wrapper [data owner opts]
  (om/component
    (dom/div #js {:className "event-detail-wrapper"}
             (om/build edetail/gen-div-simon data {:opts opts}))))


(defn calendar [data owner opts]
  (reify
    om/IInitState
    (init-state [this]
      {:current-event nil
       :current-day nil})
    om/IRenderState
    (render-state [this {:keys [current-event current-day] :as state}]
      (let [{:keys [date days month]} (get-shown-dates data)
            [events-by-day has-event?] (gen-has-event? data)
            on-click (fn [data] (om/set-state! owner :current-event data))
            on-click-day (fn [d] (om/set-state! owner :current-day d))
            today (time-fr/unparse (time/today-at 10 10))
            ]
        (html
          [:div.full-calendar
          [:div.calendar
           [:table
            [:thead
             [:tr
              [:td.back-month {:on-click #(change-month data date false)} "<<"]
              [:td.this-month {:col-span 5} (time-fr/unparse "MMMM YYYY" date)]
              [:td.next-month {:on-click #(change-month data date true)} ">>"]
            ]]
            [:tbody
             [:tr.header
               (for [x ["L" "M" "M" "J" "V" "S" "D"]]
                 [:td x])]
             (for [week (partition 7 days)]
               [:tr
               (for [d week]
                 (let [event? (has-event? d)]
                   [:td (cond->
                          {:class (clojure.string/join
                                  " " 
                                  (filter some?
                                          [(when (not= (time/month d) month) "out-month")
                                           (when event?  "has-event")
                                           (when (= today (time-fr/unparse d)) "today")]))}
                          event? (assoc :on-click #(on-click-day d))
                          )
                      (time/day d)]))])
             ]]
           (om/build cal-event-list events-by-day {:opts {:on-click on-click}})
           ]
           [:div.calendar-event-detail
               (when current-event
                (om/build edetail/event-modal-simon current-event {:opts opts :key :id}))
            ]
           (when current-day
             [:div.calendar-day-detail
              (let [events (has-event? current-day)]

                (om/build-all event-detail-wrapper
                              events
                              {:opts {assoc opts :no-img true} :key :id}))
              ])
           ]
        )))))


