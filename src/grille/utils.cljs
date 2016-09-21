(ns grille.utils
  (:require
    [cljsjs.mustache]
    [cljsjs.marked]))


(defn generate-html [s opt]
  (-> (.render js/Mustache s opt)
      (js/marked)))


(defn split-event-by-day
  "only take the first date for now
  "
  ([events]
   (split-event-by-day events nil))
  ([events valid-dates]
    (let [gevents (group-by (comp first :date) events)
          valid-dates (if (seq valid-dates)
                        valid-dates
                        (keys gevents))]
      (for [d (sort valid-dates)]
        [d (get gevents d)]))))



(defn split-event-by-day-data
    "only take the first date for now"
    [data]
    (split-event-by-day (:events data) (-> data :view :date-range)))


