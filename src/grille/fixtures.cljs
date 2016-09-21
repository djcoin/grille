(ns grille.fixtures
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom]
    [clojure.walk :as walk]
    [cljs-time.core :as time]
    [grille.generated :as generated]
    [grille.widget :as widget]
    [grille.time-fr :as time-fr]
    [grille.modal :as modal]

  [sablono.core :as sab :include-macros true]
    )
  (:require-macros
    [devcards.core :as dc :refer [defcard deftest defcard-om]]))


(defn- make-date-range
  ([]
   (make-date-range (time/today)))
  ([date]
   (let [first-monday (time-fr/get-monday date)]
     (set (time-fr/next-days first-monday 0 7)))))


(defn transform-data [{:keys [cal categories view date-range] :as data}]
  (let [new-cal (for [event cal]
                  (mapv time-fr/parse-date (:date event)))
        new-date-range (if date-range date-range (make-date-range))]

    {:cal        new-cal
     :events     new-cal
     :filter     nil
     :categories categories
     :date-range new-date-range}))




(def simple-data
  [{:date       ["2016-01-01"]
    :name       "My event"
    :event      "conference"
    :category   [:geopolitic]
    :start-time "06:00"
    :end-time   "07:30"}])


(def multi-data
  [{:date       ["2016-01-01"]
    :name       "My event"
    :event      "conference"
    :category   [:geopolitic]
    :start-time "06:00"
    :end-time   "07:30"}
   {:date       ["2016-01-01"]
    :name       "My event 2"
    :event      "conference"
    :category   [:geopolitic]
    :start-time "09:15"
    :end-time   "10:30"}])


(def data-spread-days
  [{:date       ["2016-01-01"]
    :name       "My event"
    :event      "conference"
    :category   [:geopolitic]
    :start-time "18:00"
    :end-time   "19:30"}
   {:date       ["2016-01-02"]
    :name       "My event 2"
    :event      "conference"
    :category   [:economic]
    :start-time "19:15"
    :end-time   "20:30"}
   {:date       ["2016-07-27"]
    :name       "Conf geo"
    :event      "conference"
    :category   [:geopolitic]
    :start-time "16:30"
    :end-time   "18:00"
    :template   "**Bonjour ceci est un test!** {{start-time}}"}])

; (def data-spread-days
;   (js->clj (js* "DATA")))

(def colors generated/colors)

(def categories
  [{:type :geopolitic :text "Géopolitique" :color (colors :geopolitic)}
   {:type :politic :text "Politique" :color (colors :politic)}
   {:type :economy :text "Économie" :color (colors :economy)}
   {:type :ecology :text "Écologie" :color (colors :ecology)}
   {:type :alternative :text "Alternatives" :color (colors :alternative)}
   {:type :media :text "Média" :color (colors :media)}
   {:type :history :text "Histoire" :color (colors :history)}
   {:type :spirituality :text "Spiritualité" :color (colors :spirituality)}
   {:type :culture :text "Culture" :color (colors :culture)}
   {:type :science :text "Sciences" :color (colors :science)}])


(defn url-img [x] (str "/site_media/static/public/img/" x))


(def widget
  {:email (url-img (str "popup/" "email.png"))
   :facebook (url-img (str "popup/" "facebook.png"))
   :map (url-img (str "popup/" "map.png"))
   :www (url-img (str "popup/" "www.png"))
   })



(def event-type-config
 [
  {:type "conference" :text "Conférence" :src (url-img "conference.png")}
  {:type "manifestation" :text "Manifestation" :src (url-img "manifestation.png")}
  {:type "salon" :text "Salon" :src (url-img "salon.png")}
  {:type "dedicace" :text "Dédicace" :src (url-img "dedicace.png")}
  {:type "mumble" :text "Mumble" :src (url-img "radio_mumble.png")}
  {:type "emission TV" :text "Émission TV" :src (url-img "emissiontv.png")}
  ; img are not good ;
  {:type "theatre" :text "Théatre" :src (url-img "radio_mumble.png")}
  {:type "cinema" :text "Cinéma" :src (url-img "radio_mumble.png")}
  {:type "emission-radio" :text "Émission radio" :src (url-img "radio_mumble.png")}
  ])

(def event-type-config-dct (reduce (fn [acc x] (assoc acc (:type x) x)) {} event-type-config))

(def categories-config-dct (reduce (fn [acc x] (assoc acc (:type x) x)) {} categories))



(def this-monday
  (time-fr/get-monday (time/today))
  ; (time-fr/parse-date "2016-07-27")
  )


(defn init-data
  ([data]
   (init-data data this-monday))
  ([data start-date]
    {:cal        data
     :events     data
     :categories categories
     :filter-cat nil
     :filter-event nil
     :event-type-config event-type-config
     ;; year, week number
     :view       {:date-range (set (map time-fr/unparse (make-date-range start-date)))}
     }
   ))




(defn assoc-dissoc [d]
  (reduce
    (fn [acc [src dest]]
      (-> acc (assoc dest (get d src)) (dissoc src)))
    d
    ['("reg_price" :reg-price)
     '("reg_website" :reg-website)
     '("reg_facebook" :reg-facebook)
     '("reg_email" :reg-email)
     '("reg_type" :reg-type)
     '("event_type" :event)]))


(defn convert
  "Convert json datastructure"
  [json]
  (let [ds (js->clj json)]
    (into []
      (for [d ds]
        (let [start (get d "start")
              end (get d "end")]
        (-> d
            (assoc-dissoc)
            (assoc :category [ (keyword (get d "event_subject")) ])
            (dissoc "event_subject")
            ;; extract date
            (dissoc "start" "end")
            (assoc :date [(subs start 0 10)])
            (assoc :start-time (subs start 11 16))
            (assoc :end-time (subs end 11 16))
            (walk/keywordize-keys))
          )))))



(def markdown-descr
   "
   Ceci est un exemple de description.
   De l'*italique*, du **gras**, des [liens vers la syntaxe !](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet).

   Il contient différent **éléments**:
   - celui-ci
   - celui-là

  [//]: # (![CDV logo](https://www.upr.fr/wp-content/uploads/2012/03/cercle-des-volontaires.jpg))
   "
  )

(def event
  {:category [:alternative]
   :date ["2016-08-27"]
   :facebook "http://facebok.com/website"
   :organizers [{:website "" :facebook "" :name "Ttot"}]
   :name "Autre évènement"
   :start-time "15:30"
   :end-time "18:08"
   :reg-email "reg@tutu.com"
   :reg-website "http://regwebsite.com"
   :reg-type "Prix libre"
   :attendees [{:name "Jack" :description "fdf"}]
   :event "salon"
   :poster "http://assets.pokemon.com/assets/cms2-fr-fr/img/cards/web/G1/G1_FR_26.png"
   :landscape nil
   :website "http://website.com"
   :reg-price "10 euros"
   :location {:building "Ma maison"
   :city "Nantes"
   :address "9 rue du roi baco"
   :url "https://goo.gl/maps/PvQtCXvCHso"
   :area "44100"}
   :reg-facebook "http://facebook.com/reg"
   :description markdown-descr
   ; :description "Courte description pas *grand chose à dire*..."
   })


