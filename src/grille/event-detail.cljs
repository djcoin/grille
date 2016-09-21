(ns grille.event-detail
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom]
    [sablono.core :as html :refer-macros [html]]
    [clojure.string :as str]
    [cljsjs.marked]
    [grille.modal :as modal]
    [grille.time-fr :as time-fr]
    [grille.utils :as utils]))


;; :template   "**Bonjour ceci est un test!** {{start-time}}"

;; FIXME: delete (copy in widget)

(defn category->class
  ([category]
   (category->class category category))
  ([category current-cat]
   (if (= category current-cat)
     (str "cat-" (name category))
     "")))

(defn category->border [category]
  (str "border-cat-" (name category)))



(def base-tmpl
  "**Bonjour ceci est un test!** {{start-time}}")

(defn generate-html [tmpl clj-data]
  (utils/generate-html tmpl (clj->js clj-data)))


(defn link
  ([url]
   (link url url))
  ([url text]
   (link url text true))
  ([url text blank?]
    [:a {:href url :target (when blank? "_bank")} text]))


(defn gen-location [{:keys [city area building address url]}]
  [
   [:div.adress-text
    [:div building ]
    [:div address ]
    [:div (str area " " city)]
    ]
   ; (apply str (interpose ", " (filter seq [address building area city])))


   ; (when url [:br])
   ; (when url [:a {:href url :target "_blank" :class "plan"} "(plan)"])

   ])


(defn str-time [s]
  (str/join "h" (str/split s ":")))


(defn gen-div [{:keys [start-time
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
                       category
                       ] :as data}
               cat-config
               event-type-config]
  (let [; has-left-col (or location (seq poster))
        has-left-col true
        has-footer (or location (seq organizers))
        src-event-type (:src (get event-type-config event))


        date (first date)
        date-str  (->> (time-fr/parse-date date)
                       (time-fr/unparse "EEEE d MMMM"))

        full-date-str (str date-str " " (str-time start-time) " - " (str-time end-time))
        ]
    (html
      ;; si photo: block left

      [:div {:class (str "event-detail"
                         (when has-footer " has-footer")
                         (when has-left-col " has-left-col")
                         )}
        [:div {:class (str "border-line " (category->class (first category)))}
         ;; Text of category
         ; [:span (:text (get cat-config (first category)))]
         ]

       [:div.body
         [:div.main
          (when poster
            [:div.poster [:img {:src poster} ]])
          [:div.main-col
           [:div.title name]

           [:div.attendees
           "Participant(s):"
            [:ul (for [a attendees] [:li (str "- " (:name a))])]]
           ]

          [:div.info
             ; [:div.event-type {:style {:background-image (str "url(" src-event-type ")")}}]
             [:img.event-type {:src src-event-type}]
             (when reg-price [:div.reg-price "Prix: " reg-price])
             (when (or reg-website reg-facebook reg-email)
               [:div.reg-detail "S'inscrire"
                 (when reg-website [:div.reg-website reg-website])
                 (when reg-facebook [:div.reg-facebook reg-facebook])
                 (when reg-email [:div.reg-email reg-email])
                ])
          ]

           ;; youtube video ?

         ]
         [:div.sep [:div.sep-in]]
         [:div.footer

             ;; todo: adresse + gmap/lien
          [:div.location 
            [:div full-date-str]
            [:br]
            (when location (gen-location location))]

           (when (seq organizers)
             [:div.organizers
             "Organisé par: "
              [:ul (for [o organizers] [:li (:name o)])]])
         ]

        ]
       ]
      )))


(defn event-overview
  "tuile"
  [{:keys [start-time end-time date name event organizers attendees location] :as data}
                      config]
  (let [event-type-text (:text (get config event))]
    [(dom/div #js {:className "event-type"} event-type-text)
     (dom/div #js {:className "sep"} "-")
     (dom/div #js {:className "title"} name)

     (when (seq attendees)
       (dom/div #js {:className "sep"} "-"))
     (when (seq attendees)
       (dom/div #js {:className "attendees"}
                (for [{:keys [name]} attendees] (dom/div nil name))))
     ; (str name " | " start-time " - " end-time " - (" (first date) ")")
     (dom/br nil)

     (when-let [city (seq (:city location))]
       (dom/div #js {:className "location"} city))

     #_(when (seq organizers)
       (dom/div #js {:className "organizers"}
               "Org.: " (for [{:keys [name]} organizers] (dom/div nil name))))
     ]))



(defn event-modal [{:keys [name] :as data} owner opts]
  (om/component
    (let [{:keys [categories-config-dct event-type-config-dct]} (om/get-shared owner)
          body (gen-div data categories-config-dct event-type-config-dct)
          #_(om.dom/div #js {:dangerouslySetInnerHTML
                                  #js {:__html (generate-html (or (:template data) base-tmpl) data)}}
                             nil)]

      (dom/div nil
        (om/build modal/modal-simple
                  {:header (str name)
                   :footer ""
                   :body   body}
                  {:opts opts}))
      )))




;;;
;;;
;;;


(defn gen-location-simon [{:keys [city area building address url]}]
  [
   [:span.adress-text
    (apply str (interpose ", " (filter seq [building address (str (if area (str area " ") "") city)])))
    " "
    (when url [:a {:href url :target "_blank"}
               " (plan)"
               #_[:i.fa.fa-location-arrow {:style {:font-size 16}}]
               #_[:i.fa.fa-map {:style {:font-size 16}}]
               " "])
    ]
   ])


(def any-seq (some-fn seq))


(defn gen-div-simon [{:keys [start-time
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
                     {:keys [no-img] :as opts}
                     ]
  (om/component
    (html

  (let [; has-left-col (or location (seq poster))
        {:keys [categories-config-dct event-type-config-dct]} (om/get-shared owner)
        cat-config categories-config-dct
        event-type-config event-type-config-dct

        has-left-col true
        has-footer (or location (seq organizers))
        src-event-type (:src (get event-type-config event))


        date (first date)
        date-str  (->> (time-fr/parse-date date)
                       (time-fr/unparse "EEEE d MMMM"))

        ; full-date-str (str date-str " " (str-time start-time) " - " (str-time end-time))
        time-bracket (str (str-time start-time) " - " (str-time end-time))
        ]
      ;; si photo: block left


      [:div {:class (str "event-detail"
                         (when has-footer " has-footer")
                         (when has-left-col " has-left-col")
                         )}
        [:div {:class (str "border-line " (category->class (first category)))}
         ;; Text of category
         ; [:span (:text (get cat-config (first category)))]
         ]

       [:div.body {:class (if poster "has-poster" "no-poster")}

        [:div.top
         (when (and (not no-img) poster )
           [:div.info-pratique
              [:div.poster [:img {:src poster} ]]
             ;; youtube video ?
            ])

          [:div.main-col-simon

           [:div.title name]

           [:table.date-location
            [:tr.title-date
             [:td
             [:span.fa.fa-calendar {:style {:margin-right "10px" :text-align "center" :width "20px"}}]
              ]
             [:td
             [:div
              [:div date-str] [:div time-bracket]]
              ]
             [:td]
             ]

            [:tr.location-simon
             [:td
              [:span.fa.fa-map-marker {:style {:margin-right "10px" :text-align "center" :width "20px"}}]
              ]
             [:td
              (when location (gen-location location))
              ]
             [:td
              (when-not (clojure.string/blank? (:url location))
                [:div.plan (link (:url location) "(plan)")])
              ]
            ]
            ]

           (when (seq organizers)
             [:div.attendees-simon
              [:span.attendees-title "Participant(s) : "]
              [:span (interpose ", " (for [a attendees] [:span (:name a)]))]])

           (when (seq organizers)
             [:span.organizers-simon
              [:span.organizers-title "Organisateur(s) : "]
              [:span (interpose ", " (for [o organizers] [:span (str (:name o))]))]])

          [:div.spacer]

          [:table.info-simon
             ; [:div.event-type {:style {:background-image (str "url(" src-event-type ")")}}]
             ; [:img.event-type {:src src-event-type}]
             (filter seq
               [
               (when (any-seq facebook website)
                 [:tr.event-info-detail
                   [:td "Liens utiles : "]
                   [:td
                     (when (seq website) [:div.event-website (link website "Site")])
                     (when (seq facebook) [:div.event-facebook (link facebook "Facebook")])
                    ]
                 ])
               (when (seq reg-price)
                 [:tr.reg-price
                   [:td "Prix : " ]
                   [:td reg-price]
                 ])
               (when (any-seq reg-website reg-facebook reg-email)
                 [:tr.reg-detail
                   [:td "Inscription : "]
                   [:td
                     (when (seq reg-website) [:div.reg-website (link reg-website "Site")])
                     (when (seq reg-facebook) [:div.reg-facebook (link reg-facebook "Facebook")])
                     (when (seq reg-email) [:div.reg-email (link reg-email "Email")])
                    ]
                 ])
                ])
            ]
           ]
           ;; main-col
         ] ;; top

           (when description
             [:div.description
               {:dangerouslySetInnerHTML {:__html (js/marked description) }}
              ])

         ] ;; body

        ]
      ))
  ))


(defn event-modal-simon [data owner opts]
  (om/component
    (let [{:keys [prev? current next?]} data]
      (dom/div #js {:className "simon"}
        (om/build modal/modal-simple
                  {:header nil
                   :footer nil
                   :body   (om/build gen-div-simon current {:key :id})
                   :prev? prev?
                   :next? next?}
                  {:opts opts}))
      )))

