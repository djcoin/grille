(ns grille.calendar-devcards
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom]
    [clojure.walk :as walk]
    [cljs-time.core :as time]
    [grille.widget :as widget]
    [grille.time-fr :as time-fr]
    [grille.fixtures :as fix]
    [grille.event-detail :as edetail]

    [grille.calendar :as cal]

    ; [grille.planning-view-devcards :as planning]

    [sablono.core :as html :refer-macros [html]]
    [grille.modal :as modal])

  (:require-macros
    [devcards.core :as dc :refer [defcard deftest defcard-om]])
  )

(def opt {:shared {:event-type-config-dct fix/event-type-config-dct
                   :categories-config-dct fix/categories-config-dct
                   }})


; (def events (take 6 planning/events))

;; COPY/PASTED ;;

(def event fix/event)


(defn create-events [date-str]
  (let [dstart (time-fr/parse-date date-str)
        next-date (fn [period] [(time-fr/unparse (time/plus dstart period))])
        modevents [
          {:name "Mon nom"
           :date [(-> dstart time-fr/unparse)]
           :location {:city "Nantes"}}

          {:name "Meme date autre event!"
           :date [(-> dstart time-fr/unparse)]
           :location {:city "Strasbourg"}}

          {:name "Super évènement"
           :date (next-date (time/days 1))}

          {:name "Encore 1 semaine pro"
           :date (next-date (time/days 7))}

          {:name "Dans 2 semaine"
           :date (next-date (time/days 15))}

          {:name "Event le mois prochain !"
           :date (next-date (time/months 1))}
        ]]

    (vec
      (for [x (take 10 (cycle modevents))]
            (merge event x)))))


(defn fetch-events-cal [!data start end]
     (let [events (create-events start)
           ; start could be a date before the month
           ; we want to extract a month from the date, add 15 days to be sure to be there
           date (-> (time-fr/parse-date start)
                    (time/plus (time/days 15))
                    (time-fr/unparse))]
      (swap! !data
             (fn [data]
               (-> data
                   (assoc :cal events :events events)
                   (assoc-in [:view :date-range] #{date})
                   )))
       ))

(defn make-data []
  (let [!data (-> (fix/init-data (create-events "2016-09-01"))
                  (assoc-in [:view :date-range] #{"2016-09-01"})
                  (atom))]
    (swap! !data assoc :fetch-events (partial fetch-events-cal !data))
    !data
    ))



(defcard popup
   (dc/om-root cal/calendar)
   (make-data)
   opt)


