(ns grille.hooks
  (:require [robert.hooke]
            ; [leiningen.clean :as lclean]
            ; [leiningen.compile :as lcompile]
            ; [leiningen.core.main :as lmain]
            [clojure.java.io :as io]
  ))
; https://github.com/vladh/lein-sassy/blob/master/src/leiningen/sass.clj
; https://github.com/vladh/lein-sassy/blob/master/src/leiningen/lein_sassy/file_utils.clj

(defn do-compile [file-out]
  (let [colors {
          :geopolitic "#3939FF"
          :politic "#399BFF"
          :economy "#39A9A9"
          :ecology "#39A939"
          :alternative "#FF9B39"
          :media "#FF3939"
          :history "#FF399B"
          :spirituality "#FF39FF"
          :culture "#9B39FF"
          :science "#A939A9"
        }]

    (spit "resources/public/css/generated.css"
      (clojure.string/join "\n"
        (reduce concat
          (for [[k color] colors]
            [(str ".cat-" (name k) "{ background-color: " color "; }")
             (str ".border-cat-" (name k) " { border-color: " color "; }")
             ""]))))

    (spit "src/grille/generated.cljs"
          (clojure.string/join
            "\n" 
            ["(ns grille.generated)"
             ""
             "(def colors"
             (pr-str colors)
             ")"]))

    ))



;;;

(defn delete-file!
    [file]
    (when (.exists file)
          (println (str "Deleting: " file))
          (io/delete-file file)))

(defn get-options [project]
    (if-let [grille-opt (:grille project)]
      grille-opt
      #_(lmain/warn "No grille entry found in project definition.")))



(defn- clean-hook [task & args]
    (apply task args)
    (some-> (get-options (first args)) :out delete-file!))



(defn- compile-hook [task & args]
    (apply task args)
    (when-let [out (:out (get-options (first args)))]
      (do-compile out)))


#_(defn activate []
    (hooke/add-hook #'lclean/clean #'clean-hook)
    (hooke/add-hook #'lcompile/compile #'compile-hook))
