(ns grille.widget
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom]
    [goog.dom :as gdom]
    [goog.style :as gstyle]
    [cljs-time.core :as time]
    [cljs-time.format :as timef]
    [grille.time-fr :as time-fr]
    [grille.tracking :as tracking]
    [grille.utils :as utils]
    [grille.event-detail :as edetail]))


"
TODO:

1/ trouver un moyen de préciser un range, un count dessus et voilà
2/ quand il y a une action des controles, refaire toute la vue (sinon c'est le scroll)
3/ trouver un moyen d'accéder aux autres jours de la semaine ?!
4/ évenement superposés (plus tard)

"


(defn category->class
  ([category]
   (category->class category category))
  ([category current-cat]
   (if (= category current-cat)
     (str "cat-" (name category))
     "")))

(defn category->border [category]
  (str "border-cat-" (name category)))

(def time-coll-width* 60)

(def time-coll-width
  (str time-coll-width* "px"))


(def month-width
  (let [vpwidth (.-width (gdom/getViewportSize))]
    (/ (- vpwidth
          time-coll-width*
         ;; padding of body + padding of inner-grille = 20px
         (* 20 2))
       7)))

;; 2h
(def size-minutes 120)

;; change .markercell too
; 120
(def one-hour-height (/ (* month-width 1.33)
                        (/ size-minutes 60)))


; (.log js/console "month width: " month-width)
; (.log js/console "total tuile height " (* month-width 1.33))
; (.log js/console "one hour height: " one-hour-height)

;; .abs-grill-wrapper : 7 cols
;; .col-day



; (def intersect-minutes size-minutes)
(def intersect-minutes 10)

;; 10 minutes => will be grouped
(def group-tab-minutes 10)


(def fixed-event-height
  (* (/ size-minutes 60) one-hour-height))


(defn hour-to-px
  "take a string HH:MM"
  [hour-str]
  (let [hour one-hour-height
        minute (/ one-hour-height 60)
        [h m] (clojure.string/split hour-str ":")]
    (+ (* hour h) (* minute m))))


(defn minutes-to-px
  "take an int"
  [minutes]
  (* minutes (/ one-hour-height 60)))



(defn event-height [top end-time]
  (- (hour-to-px end-time) top))



(defn timestr->minutes
  "Transform HH:MM to seconds"
  [hour-str]
  (let [[h m] (map js/parseInt (clojure.string/split hour-str ":"))]
    (+ (* h 60) m)))


(defn top-bar [{:keys [categories filter-cat]} owner {:keys [on-click]}]
  (om/component
    (dom/div #js {:className "top-bar"}
             (apply dom/ul nil
                    (for [{:keys [type text color]} categories]
                      (dom/li #js {:onClick   (partial on-click type)
                                   #_#_:style     #js {"background-color" color}
                                   :className (str
                                                (if (or (empty? filter-cat) (not (filter-cat type)))
                                                    " enabled"
                                                    " disabled"))}
                              (dom/div #js {:className (category->class type)} text)))))))


(defn event-type-bar [{:keys [event-type-config filter-event]} owner {:keys [on-click]}]
    (om/component
      (dom/div #js {:className "eventt-bar"}
               (apply dom/ul nil
                      (for [{:keys [type text src]} event-type-config]
                        (dom/li #js {:onClick   (partial on-click type)
                                     :className (str
                                                     (if (= type filter-event) " enabled" " disabled"))}
                                (dom/img #js {:src src
                                              :className "event-type"})))))))



(defn ctrl-bar [data owner {:keys [on-click]}]
  (om/component
    (dom/div #js {:className "ctrl-bar"}
             (dom/div #js {:className "cltr-bar-wrapper"}
                      (dom/div #js {:className "backward"}
                               (dom/div #js {:className "x4"
                                             ; :title "Reculer de 4 semaines"
                                             :onClick   (partial on-click :back-month)}
                                        #_"<<")
                               (dom/div #js {:className "x1"
                                             ; :title "Reculer d'une semaine"
                                             :onClick   (partial on-click :back-week)}
                                        #_"<"))
                      (dom/div #js {:className "forward"}
                               (dom/div #js {:className "x1"
                                             ; :title "Avancer d'une semaines"
                                             :onClick   (partial on-click :forward-week)}
                                        #_">")
                               (dom/div #js {:className "x4"
                                             ; :title "Avancer de 4 semaines"
                                             :onClick   (partial on-click :forward-month)}
                                        #_">>"))))))


(defn grill [data owner]
  (om/component
    (let [markercell-style #js {:height (str one-hour-height "px")}
          markercell-inner-style #js {:height (str (/ one-hour-height 2) "px")
                                      :margin-bottom (str (/ one-hour-height 2) "px")}
          ]
    (dom/tr #js {:className "grill"}
            (dom/td #js {:style #js {:width time-coll-width
                                     :minWidth time-coll-width}})
            (dom/td #js {:colSpan (:nb-cols data)}
                    (dom/div
                      #js {:className "rel-grill-wrapper"}
                      (dom/div
                        #js {:className "abs-grill-wrapper"}
                        (for [x (range 24)]
                          (dom/div #js {:className "markercell"
                                        :style markercell-style}
                                   (dom/div #js {:className "markercell-inner"
                                                 :style markercell-inner-style} ""))))))))))


(defn time-grill [data owner]
  (om/component
    (dom/td #js {:className "time-col"}
             (for [x (range 24)]
               (dom/div #js {:className "time-col-cell"
                             :style     #js {:height (str one-hour-height "px")}}
                        (str (when (< x 10) "0") x ":00"))))))


(defn event
  "Une tuile"
  [data owner {:keys [on-click] :as opts}]
  (let [{:keys [start-time end-time category idx-group]} data
        [i len] (if (= nil idx-group) [0 1] idx-group)
        left (/ (* 100 i) len)
        base (/ 100 len)
        width (+ base (if (< i (dec len))
                        (* 0.2 (- 100 left))
                       0))
        ;; FIXME: bad algo

        top (hour-to-px start-time)
        on-click (if on-click on-click (fn noop [& args]))
        height fixed-event-height]

    (om/component
      (dom/div #js {:className "event-wrapper"
                    :style     #js {:top top
                                    :left (str left "%")
                                    :width (str width "%")
                                    :min-height height
                                    :height (event-height top end-time)}}
        (dom/div #js {:className (str "event " (category->border (first category)))}
                 (apply dom/div #js {:className "event-content"
                                     ; :style     #js {:height height}
                                     :onClick   #(on-click % data)}
                        (edetail/event-overview data (:event-type-config-dct (om/get-shared owner)))))))))


(defn event-grouped [data owner {:keys [on-click] :as opts}]
  (reify
    om/IInitState
    (init-state [this] {:timeout nil
                        :slides 0
                        :manual false
                        :tab 0})
    om/IDidMount
    (did-mount [this]
      #_(om/set-state! owner :timeout
                     (js/setInterval #(om/update-state! owner :slides inc) 4000)))
    om/IWillUnmount
    (will-unmount [this]
      (js/clearInterval (om/get-state owner :timeout)))

    om/IRenderState
    (render-state [this {:keys [slides manual tab] :as state}]
      (let [{:keys [events low high]} data
            count-slide (if manual tab (mod slides (count events)))

            {:keys [category] :as event} (nth events count-slide)

            top (minutes-to-px low)
            on-click (if on-click on-click (fn noop [& args]))
            ; height (- (minutes-to-px high) top)
            height fixed-event-height]
          (dom/div #js {:className "event-wrapper"
                        :style     #js {:top top
                                        :height height}}
            (dom/div #js {:className (str "event " (category->border (first category)))}
                     (dom/div #js {:className "event-bubble"}
                              (dom/div #js {:className "event-bubble-inner"}
                                       (str (count events))))

                     (apply dom/div #js {:className "event-content"
                                         :onClick   #(on-click % event)}
                            (edetail/event-overview event (:event-type-config-dct (om/get-shared owner))))

                     (apply dom/ul #js {:className "event-group"}
                            (map-indexed
                              (fn [i event]
                                   (dom/li #js {:className (str "event-group-tab"
                                                              (if (= i count-slide) " active" ""))
                                                  :onClick #(om/update-state! owner
                                                                          (fn [s] (assoc s :manual true
                                                                                           :tab i))) }
                                          (dom/div nil
                                            (dom/div #js {:className
                                                          (str "tab-circle "
                                                               (category->class (first (:category event))))
                                                          }))))
                              events
                              ))
                     ))

        ))
      ))


(defn event-switch [data owner opts]
  (om/component
    (dom/div nil
      (let [events (:events data)]
        (if (= (count events) 1)
          (om/build event (first events) {:opts opts})
          (om/build event-grouped data {:opts opts})
          #_(let [xs (mapv #(assoc %1 :idx-group [%2 (count events)]) events (range))]
            (om/build-all event xs {:opts opts}))
          )
        ))))


;; FIXME less than 2h30 means 2h30

(defn visual-bounds
  "If the time is too small (< to 2h30), the height will still be 2h30 minimum!"
  [l h]
  [l
   (if (< (- h l) (* 2.5 60))
     (+ l (* 2.5 60))
     h)])


(defn- eventless-range-hour [edays]
  (let [[min-min max-min]
        (reduce (fn [[vlow vhigh] {:keys [low high]}]
                  (let [[vlow1 vhigh1] (visual-bounds low high)]
                        [(min vlow vlow1) (max vhigh vhigh1)]))
                [(* 24 60) 1 0]
                edays)]
    [(- min-min 30) (+ max-min 30)]))


(defn eventless-zone [edays owner]
  ; (prn min-sec max-sec min-hour max-hour)
  (om/component
    (if (seq edays)
      (let [[min-min max-min] (eventless-range-hour edays)]
        ; (prn min-min max-min)
        (dom/div
          nil
          (dom/div #js {:className "eventless eventless-before"
                        :style #js {:top 0
                                    :height (minutes-to-px min-min)}})
          (dom/div #js {:className "eventless eventless-after"
                        :style #js {:top (minutes-to-px max-min)
                                    :height (minutes-to-px (- (* 24 60) max-min))}})))
      (dom/div #js {:className "eventless eventless-full"
                    :style #js {:top 0 :height "100%"}})
      )))


(defn event-table [data owner opts]
  (om/component
    (let [valid-dates (-> data :view :date-range)]
      ; (prn valid-dates)
      (dom/table
        #js {:className "event-table"}
        (dom/tbody
          nil
          (om/build grill {:nb-cols (count valid-dates)})
          (apply dom/tr nil
                 (om/build time-grill nil)
                 (for [[i edays] (map-indexed vector (:events-by-day data))]
                   (dom/td #js {:className "col-day"
                                ;; column on the left are higher than on the right
                                :style #js {:zIndex (- 20 i)}}
                          (om/build eventless-zone edays)
                          (om/build-all event-switch edays {:opts opts}))))
                     )))))


(defn header-day-table [data owner]
  (om/component
    (let [valid-dates (-> data :view :date-range)]
      ; (prn valid-dates)
      (dom/table
        #js {:className "day-table"}
        (dom/tbody
          nil
          (apply dom/tr nil
                 (dom/td #js {:style #js {:minWidth "60px"
                                          :width "60px"}})
                 (for [dstring (sort valid-dates)]
                   (dom/td #js {:className "day-header"}
                           (let [d (time-fr/parse-date dstring)]
                             (time-fr/unparse "EEEE dd/MM" d))))))))))


(defn positions
  [pred coll]
  (keep-indexed (fn [idx x] (when (pred x) idx)) coll))


; http://stackoverflow.com/questions/4830900/how-do-i-find-the-index-of-an-item-in-a-vector

(defn change-editing-event
  ""
  ;; Refactor: use zipper...
  [event-id events what?]
  (let [sorted-events (sort-by (juxt (comp first :date) (comp timestr->minutes :start-time)) events)
        event-i (first (positions #(= (:id %) event-id) sorted-events))
        i (case what?
                 :next (inc event-i)
                 :prev (dec event-i)
                 event-i)]

    (when (and (some? i) (not= -1 i))
      {:prev? (>= (dec i) 0)
       :next? (< (inc i) (count sorted-events))
       :current (nth sorted-events i nil)}
      )))


(defn popup-hit
  ([event] (popup-hit event nil))
  ([event action]
    (tracking/hit (str "popup/" (:id event) (if action (str "?action=" action) ""))
                  (:name event))))


(defn popup-nav!
  "Set the nav of the om component to the popup id"
  [owner event]
  ; (prn "popupnav!" event)
  (let [nav! (:nav! (om/get-shared owner))]
    (if event
      (nav! :event-popup {:id (:id event)})
      (nav! :cal nil))))


(defn event-grill [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:on-click (fn [e {:keys [id] :as event}]
                   (let [{:keys [current] :as editing}
                         (change-editing-event id (:events (om/get-props owner)) nil)]

                     (when current (popup-hit current "direct"))
                     (popup-nav! owner current)
                     ))
       :end-edit #(popup-nav! owner nil)
       :on-next #(let [data (om/get-props owner)
                       {:keys [current] :as editing}
                          (change-editing-event (-> data :view :editing) (:events data) :next)]

                      (when current (popup-hit current "arrow"))
                      (popup-nav! owner current))
       :on-prev #(let [data (om/get-props owner)
                       {:keys [current] :as editing}
                          (change-editing-event (-> data :view :editing) (:events data) :prev)]

                      (when current (popup-hit current "arrow"))
                      (popup-nav! owner current))
       })
    om/IRenderState
    (render-state [_ {:keys [on-click end-edit on-next on-prev]}]
      (let [event-id (-> data :view :editing)]
        (js/console.log "event id: " event-id)

      (dom/div #js {:className (str "event-grill" (when event-id " editing"))}
               ;; FIXME HERE
               (when event-id
                 (om/build edetail/event-modal-simon
                           (change-editing-event event-id (:events data) nil)
                           {:opts {:on-click end-edit
                                   :on-next on-next
                                   :on-prev on-prev}
                            }))
               (om/build header-day-table data)

               (dom/div #js {:className "event-grill-viewport"}

                        (dom/div #js {:className "event-grill-wrapper"}

                                 (om/build event-table data {:opts {:on-click on-click}}))))))))


(defn change-date-range [owner kw]
  (let [nb-days 7
        period (case kw
                 :back-week (time/days -7)
                 :forward-week (time/days 7)
                 :back-month (time/days -28)
                 :forward-month (time/days 28))]

    (let [dstart (time-fr/parse-date (apply min (-> (om/get-props owner) :view :date-range)))
          start (time/plus dstart period)]
      ((:nav! (om/get-shared owner)) :cal-date {:date (time-fr/unparse start)}))))



;; TODO: mark current filter


(defn filter-events
  "Beware: used from the outside (core/fetch-events) too."
  [{:keys [cal filter-cat] :as data}]
  ;; if in filter cat, then we will remove it
  ;; consider category is always a one-item array
  (if (seq filter-cat)
    (let [fcat (if filter-cat #(not (filter-cat (-> % :category first))) (constantly true))]
      (assoc data :events (filter (every-pred fcat) cal)))
    (assoc data :events cal)))


(defn filter-category [data kw]
  ;; toggle if already filtered
  (-> data
      (update :filter-cat
              (fn [fcat]
                 (let [s (if (nil? fcat) #{} fcat)]
                   (if (s kw)
                     (disj s kw)
                     (conj s kw)))))
      (filter-events)))


(defn filter-category! [data kw]
  (om/transact! data #(filter-category % kw)))


#_(defn filter-event-type
  "Event: "
  [data kw e]
  (om/transact!
    data
    (fn [data]
      ;; toggle if already filtered
      (-> (assoc data :filter-event (if (= (:filter-event data) kw) nil kw))
          (filter-events))
      )))


(defn get-scroll-to-element [scroll-offset]
  (let [wrapper (aget (.getElementsByClassName js/document "event-grill-wrapper") 0)
        el (-> (gdom/getElementByClass "time-col")
            (gdom/getChildren)
            (aget scroll-offset))]
    (gstyle/scrollIntoContainerView el wrapper true)))



(defn event-space-intersect? [s1 e1 s2 e2]
  "Look if given start/end time of an event and start/end time of a group,
  do they intersect?"
  [s1 e1 s2 e2]
  (let [diff (- s1 s2)]
    (when (< (* diff diff) (* size-minutes size-minutes))
      [(min s1 s2) (max e1 e2)])))


(defn event-intersect-start-exact? [s1 e1 s2 e2]
    "Look if given start/end time of an event and start/end time of a group,
      do they intersect?"
    [s1 e1 s2 e2]
  (when (= s1 s2)
    [(min s1 s2) (max e1 e2)]))


(defn event-intersect?
  "Look if given start/end time of an event and start/end time of a group,
  do they intersect?
  Used to make smart 'intersect'"
  [s1 e1 s2 e2]
  (let [diff (- s1 s2)]
    (when (< (* diff diff) (* intersect-minutes intersect-minutes))
      [(min s1 s2) (max e1 e2)])))


(defn event-group
  "Traverse a list of group and look if the current event can enter into this group."
  [[e start end :as event] [g & gs :as l] res]
  (if-not (seq l)
    (cons {:low start :high end :events [e]} res)
    (let [{:keys [low high events]} g]
      ;; event-space-intersect?
        (if-let [[l1 h1] (seq (event-intersect-start-exact? start end low high))]
          (cons {:low l1 :high h1 :events (conj events e)}
                (concat gs res))
          (recur event gs (cons g res))
          ))))


(defn group-time
  "Inside a day: group by events that are close"
  [[x & r :as l] gs]
  (if-not (seq l)
    gs
    (recur r (event-group x gs (list)))))


;; group-by
(defn group-when-intersects
  "return a list of groups :low : high :events.
  low and high are brackets of minutes for events."
  [xs]
  (group-time
    (map (juxt identity
           (comp timestr->minutes :start-time)
           (comp timestr->minutes :end-time))
       xs)
    (list)))


(defn split-days [data]
   (for [[dstring d] (utils/split-event-by-day-data data)]
     (let [groups (group-when-intersects d)]
       (->> groups
            (map #(update % :events (partial sort-by :start-time)))
            (sort-by :low)))))



;; which nth time-col do you want to start on?
;; here 7pm (1 == 1h)
(defn start-time->scroll-offset
  "From the start time look for the element with the right hour"
  [start-time]
  (let [[h m] (map js/parseInt (clojure.string/split start-time ":"))]
    ;; if exact (= m 0) ?
    (dec (+ 2 h))))


(defn scroll-to-first-event [events]
  (when (seq events)
    (let [stime (-> (sort-by :start-time events) first :start-time)
          scroll-offset (start-time->scroll-offset stime)]
      (get-scroll-to-element scroll-offset))))


(defn widget [data owner]
  (reify
    om/IDidMount
    (did-mount [this]
      (scroll-to-first-event (:events data)))
    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (let [props (om/get-props owner)]
        ; unfortunately rendering can happen several times for now...
        ; (or (empty? (:events prev-props))
        ;     (not= (-> prev-props :view :date-range) (-> props :view :date-range)))
        (when (not= (:cal props) (:cal prev-props))
          (scroll-to-first-event (:events props)))))
    om/IRender
    (render [this]
      (let [nb-days 7
            offset 7
            offset-neg (- offset)]

        (let [bydays (split-days data)]

          (dom/div #js {:className "grille"}
                   (dom/div #js {:className "bars"}
                     (om/build top-bar data {:opts {:on-click #(filter-category! (om/get-props owner) %)}})
                     ; (om/build event-type-bar data {:opts {:on-click (partial filter-event-type data)}})
                     (om/build ctrl-bar nil {:opts {:on-click #(change-date-range owner %)}})
                   )
                   (dom/div #js {:className "inner-grille"}
                            (om/build event-grill (assoc data :events-by-day bydays)))
        ))




      #_(dom/h1 nil (:text data))))))

