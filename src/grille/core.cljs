(ns grille.core
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom]
    [clojure.walk :as walk]
    [cljs-time.core :as time]
    [grille.widget :as widget]
    [grille.planning-view :as planning]
    [grille.time-fr :as time-fr]
    [grille.modal :as modal]
    [grille.calendar :as cal]
    [grille.utils :as utils]
    [grille.fixtures :as fix]
    [grille.routing :as routing]
    [secretary.core :as secretary :refer-macros [defroute]]
    )

  ; [sablono.core :as sab :include-macros true])
  (:require-macros
    [devcards.core :as dc :refer [defcard deftest defcard-om]]))

(enable-console-print!)


(def opt {:shared {:event-type-config-dct fix/event-type-config-dct
                   :categories-config-dct fix/categories-config-dct
                   }})



(defn fetch-events-update [start end f]
  (when (.-getEvents js/window)
        (js/getEvents start end f)))

(defn ajax-fetch-event [id f]
  (js/getEvent id f))


(defn fetch-events
  "Take two dates format YYYY-MM-DD
  Reset events!"
  [start end cb]
  (fetch-events-update start end
      (fn [x]
        (let [c (fix/convert x)
              date-range (-> start time-fr/parse-date fix/make-date-range)
              date-range-str (set (map time-fr/unparse date-range))
              ]
           (cb
             (fn [data]
               (-> data
                   (assoc :cal c :events c)
                   (assoc-in [:view :date-range] date-range-str)
                   (assoc-in [:view :editing] nil)
                   (widget/filter-events)))))
          )))


(defn fetch-events-from-event [!data id cb]
  (if (seq (filter #(= id (:id %)) (:cal @!data)))
    (cb #(assoc-in % [:view :editing] id))
    (ajax-fetch-event
      id
      (fn [c]
        (let [event (fix/convert #js [c])
              date-str (first (:date (first event)))
              monday (time-fr/get-monday (time-fr/parse-date date-str))]

          (fetch-events
            (time-fr/unparse monday)
            (time-fr/unparse (time/plus monday (time/days 6)))
            (fn [update-fn]
              (-> (comp #(assoc-in % [:view :editing] id) update-fn)
                  (cb))))
      )))))


(defn remove-dups [f coll]
  (vals
    (reduce (fn [m v]
              (let [applied (f v)]
                (if-not (contains? m applied)
                  (assoc m applied v)
                  m)))
            {}
            coll)))


(defn fetch-events-planning
  "Take two dates format YYYY-MM-DD.
  Accumulate events!"
  [atom-data start end]
  (fetch-events-update start end
      (fn [x]
        (let [c (fix/convert x)]
          ;; Merge events as we go along ;;
          (swap! atom-data 
                 (fn [data]
                   (-> data
                     (update :events (fn [events]
                                       (->> (concat events c)
                                            (remove-dups :id)
                                             (vec))))
                       (update-in [:view :date-range] conj start end)
                       )))
  ))))


(defn launch-widget []

  ;; fetch for current week
  (let [start fix/this-monday
        ; start (time-fr/parse-date "2016-08-22")
        !data (atom (fix/init-data [] start))
        update-data! (partial swap! !data)
        fetch (fn [start end] (fetch-events start end update-data!))
        fetch-event (fn [id]
                      (fetch-events-from-event !data id update-data!))
        end (time/plus start (time/days 6))

        !redirect (atom)

        fdict
        {:event-popup
         (defroute "/event/:id" [id]
           (fetch-event (js/parseInt id)))
         :cal-date
         (defroute "/date/:date" [date]
           (let [start (time-fr/get-monday (time-fr/parse-date date))
                 end (time/plus start (time/days 6))]

             (fetch (time-fr/unparse start) (time-fr/unparse end))))
         :cal
         (defroute "/" []
           ;; on start always redirect to "/"
           (let [redirect @!redirect]
              (redirect :cal-date {:date (apply min (-> @!data :view :date-range))}))
           )}

        nav! (partial routing/routes-nav! fdict)
        ]

  (reset! !redirect nav!)

  (om/root widget/widget
           !data
           (-> opt
               (assoc-in [:shared :nav!] nav!)
               (assoc :target (. js/document (getElementById "scal")))))

   (routing/setup-history)
   ))



(set! js/launchWidget launch-widget)



(defn launch-planning []
  ;; fetch for current week
  (let [!data (atom (fix/init-data []))
        fetch (partial fetch-events-planning !data)
        start fix/this-monday
        end (time/plus start (time/days 0))]

  (swap! !data assoc :fetch-events fetch)
  (fetch (time-fr/unparse start) (time-fr/unparse end))

  (om/root planning/planning !data
           (assoc opt :target (. js/document (getElementById "scal"))))))


(set! js/launchPlanning launch-planning)

(defn fetch-events-cal
  "Take two dates format YYYY-MM-DD.
  Accumulate events!"
  [atom-data start end]
  (fetch-events-update start end
      (fn [x]
        (let [events (fix/convert x)
              date (-> (time-fr/parse-date start)
                       (time/plus (time/days 15))
                       (time-fr/unparse))]
          (swap! atom-data
                 (fn [data]
                    (-> data
                       (assoc :cal events :events events)
                       (assoc-in [:view :date-range] #{date})
                       )))
                 ))
  ))


(defn launch-calendar []
  ;; fetch for current week
  (let [!data (atom (fix/init-data []))
        fetch (partial fetch-events-cal !data)
        [start end] (map time-fr/unparse
                         (cal/get-date-range (time/today-at 10 10)))]

  (swap! !data assoc :fetch-events fetch)
  (fetch start end)

  (om/root cal/calendar !data
           (assoc opt :target (. js/document (getElementById "scal"))))))


(set! js/launchCalendar launch-calendar)



;; conditionally start the app based on whether the #main-app-area
; (if-let [node (.getElementById js/document "main-app-area")])


;; remember to run lein figwheel and then browse to
;; http://localhost:3449/cards.html


"
TODO:

* multitabs when event collide
; https://github.com/omcljs/om/blob/master/examples/instrument/src/core.cljs

* ouverture sur un évènement...
* régions / thématique

ok
* grille : ne pas prendre en compte la durée


; http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/swap!
; IAtom
; https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/IAtom.java


; https://github.com/reagent-project/reagent/blob/master/src/reagent/ratom.cljs
; https://github.com/mylesmegyesi/clojure.core/blob/master/src/clj/clojure/lang/atom.clj

"


