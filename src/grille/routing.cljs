(ns grille.routing
  (:require
    [secretary.core :as secretary :refer-macros [defroute]]
    [goog.events :as events]
    [goog.history.EventType :as EventType]
    )
  (:import
    [goog.history Html5History]
    ; [goog History]
    ))

; goog.History

; Html5History EventType]

; (secretary/set-config! :prefix "#")


(def !history (atom))

; var h = goog.history.Html5History.isSupported() ?
; new goog.history.Html5History()  : new goog.History();

; http://www.lispcast.com/mastering-client-side-routing-with-secretary-and-goog-history

(defn setup-history []
  (let [; history (History. false)
        history (Html5History. )]
      (reset! !history history)

      (goog.events/listen history
                          EventType/NAVIGATE
                          #(secretary/dispatch! (.-token %)))
      (doto history (.setEnabled true))
    ))



(defn routes-nav!
  "f receives a callback that will modify om datastructure"
  [fdict kw opts]
  ; "example: {:id 1 :query-params {:action "delete"}}
    (let [url ((fdict kw) opts)]
      (.setToken @!history url)))


