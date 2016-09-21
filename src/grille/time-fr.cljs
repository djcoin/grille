(ns grille.time-fr
  (:require
    [cljs-time.core :as time]
    [cljs-time.format :as timef]))

; help on format:
; http://www.joda.org/joda-time/key_format.html

(defn next-days 
  "Fetch next-days: from 0 to n-1 (length n days)"
  [start-date offset n]
  (let [period (time/days offset)
        one-day (time/days 1)]

    (reduce
      (fn [acc _]
        (conj acc (time/plus (last acc) one-day)))
      [(time/plus start-date period)]
      (range 1 n))))


(def date-format (timef/formatters :date))

(defn- datestring->vec [date]
  (map int (clojure.string/split date "-")))

(defn parse-date [date]
  (apply time/date-time (datestring->vec date)))


(def months ["Janvier" "Février" "Mars" "Avril" "Mai" "Juin" "Juillet"
               "Aout" "Septembre" "Octobre" "Novembre" "Décembre"])

(def days ["Dimanche" "Lundi" "Mardi" "Mercredi" "Jeudi" "Vendredi" "Samedi"])


(def date-formatter-fr
  (let [dow #(.getDay %)
        M #(inc (.getMonth %))]
    (assoc timef/date-formatters
      "dow" #(days (dow %))
      "EEE" #(timef/abbreviate 3 (days (dow %)))
      "EEEE" #(days (dow %))
      "MMM" #(timef/abbreviate 3 (months (dec (M %))))
      "MMMM" #(months (dec (M %))))))


(defn formatter
  ([fmts]
   (formatter fmts time/utc))
  ([fmts dtz]
   (with-meta
     {:format-str fmts
      :formatters date-formatter-fr}
     {:type ::timef/formatter})))


(defn unparse
  ([date]
   ;; default to YYYY-MM-DD
   (timef/unparse date-format date))
  ([fmt date]
   (timef/unparse (formatter fmt) date)))



; (time/date-time (time/year date) (time/month date) monday)

(defn get-monday [date]
  (let [nth-day (time/day-of-week date)
        monday 1
        diff (- nth-day monday)
        x (time/minus date (time/days diff))
        ]
    (time/date-time (time/year x) (time/month x) (time/day x))
    ))


;; TODO: a function that iterate from one date to the other


