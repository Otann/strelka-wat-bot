(defproject strelka-wat-bot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374"]
                 [com.taoensso/timbre "4.1.4"]              ; Clojure/Script logging

                 [com.grammarly/omniconf "0.2.2"]           ; App configuration

                 [ring "1.4.0"]
                 [ring-server "0.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-json "0.4.0"]

                 [compojure "1.4.0"]                        ; backend routes
                 [prone "0.8.2"]                            ; exceptions middleware
                 [hiccup "1.0.5"]                           ; html generation
                 [environ "1.0.1"]                          ; config from env
                 [cheshire "5.5.0"]                         ; json parsing/generation
                 [clj-http "2.0.0"]                         ; http client for backend
                 [clj-time "0.11.0"]                        ; Time manipulation
                 ]

  :plugins [[lein-ring "0.9.7"]]

  :ring {:handler strelka-wat-bot.handler/app
         :init strelka-wat-bot.main/ring-init}

  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.0"]]}

             :uberjar {:aot :all
                       :omit-source true
                       :main strelka-wat-bot.main
                       :uberjar-name "strelka-wat-bot.jar"}})
