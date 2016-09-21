(ns grille.tracking)

; https://yandex.com/support/metrica/code/ajax-flash.xml
; https://yandex.com/support/metrica/objects/hit.xml

(def default-counter (.-yaCounter39554475 js/window))
(def default-counter nil)


(defn counter-hit [counter url opts]
  (when counter
    (if (string? opts)
      (.hit counter url #js {:title opts})
      (do
        (assert (every? #{:callback :ctx :params :referer :title} (keys opts)) "Counter tracking hit: some keys are not valid")
        (.hit counter url (clj->js opts))))))


(def hit (partial counter-hit default-counter))


