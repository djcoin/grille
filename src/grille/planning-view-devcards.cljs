
(ns grille.planning-view-devcards
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom]
    [clojure.walk :as walk]
    [cljs-time.core :as time]
    [grille.widget :as widget]
    [grille.time-fr :as time-fr]
    [grille.fixtures :as fix]
    [grille.event-detail :as edetail]
    [grille.planning-view :as planning]
    [sablono.core :as html :refer-macros [html]]
    [grille.modal :as modal])

  (:require-macros
    [devcards.core :as dc :refer [defcard deftest defcard-om]])
  )

(def event fix/event)

(def dstart (time-fr/parse-date "2016-08-30")) 

(defn next-date [period]
  [(time-fr/unparse (time/plus dstart period))])


(def modevents
  [
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
])


(def events 
  (for [x (take 40 (cycle modevents))]
    (merge event x)))


(def opt {:shared {:event-type-config-dct fix/event-type-config-dct
                   :categories-config-dct fix/categories-config-dct
                   }})

(defcard popup
  "Popup normale et complète"
   (dc/om-root planning/planning)
    (fix/init-data events)
    opt)

