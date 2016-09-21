(defproject grille "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.3"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [devcards "0.2.1-5"]
                 [sablono "0.5.3"]
                 [secretary "1.2.3"]

                 [cljsjs/marked "0.3.5-0"]
                 [cljsjs/mustache "1.1.0-0"]

                 ;; need to specify this for sablono
                 ;; when not using devcards
                 ; [cljsjs/react "0.14.3-0"]
                 ; [cljsjs/react-dom "0.14.3-1"]
                 ; [cljsjs/react-dom-server "0.14.3-0"]

                 ; [org.omcljs/om "1.0.0-alpha31"]
                 [org.omcljs/om "0.9.0"]

                 [com.andrewmcveigh/cljs-time "0.4.0"]

                 [robert/hooke "1.3.0"]

                 #_[reagent "0.5.1"]

                 #_[lein-less "1.7.6-SNAPSHOT"]]


  :plugins [[lein-figwheel "0.5.0-6"]
            [lein-cljsbuild "1.1.4" :exclusions [org.clojure/clojure]]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]

  :source-paths ["src"]

  ; :grille {:out "resources/public/css/generated.css"}

  :cljsbuild {
              :builds [{:id "devcards"
                        :source-paths ["src"]
                        :figwheel { :devcards true} ;; <- note this
                        :compiler { :main       "grille.devcards"
                                    ; :asset-path "js/compiled/devcards_out"
                                    :asset-path "/site_media/static/public/js/compiled/devcards_out"
                                    :output-to  "resources/public/js/compiled/grille_devcards.js"
                                    :output-dir "resources/public/js/compiled/devcards_out"
                                    :source-map-timestamp true}}
                       {:id "devcards-prod"
                        :source-paths ["src"]
                        :figwheel { :devcards true} ;; <- note this
                        :compiler {:main       "grille.core"
                                   ; :asset-path "js/compiled/devcards-prod_out"
                                   :asset-path "/site_media/static/public/js/compiled/devcards_out"
                                   :output-to  "resources/public/js/compiled/grille_devcards-prod.js"
                                   :output-dir "resources/public/js/compiled/devcards-prod_out"
                                   :optimizations :advanced}}
                       {:id "dev"
                        :source-paths ["src"]
                        :compiler {:main       "grille.core"
                                   ; :asset-path "js/compiled/out"
                                   :asset-path "/site_media/static/public/js/compiled/out"
                                   :output-to  "resources/public/js/compiled/grille.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :source-map-timestamp true}}
                       #_{:id "prod"
                        :source-paths ["src"]
                        :compiler {:main       "grille.core"
                                   ; :asset-path "js/compiled/out"
                                   :asset-path "/site_media/static/public/js/compiled/out"
                                   :output-to  "resources/public/js/compiled/grille.js"
                                   :optimizations :advanced}}]}
  ; leiningen.less 
  ; :hooks [grille.hooks]
  :figwheel { :css-dirs ["resources/public/css"]})
