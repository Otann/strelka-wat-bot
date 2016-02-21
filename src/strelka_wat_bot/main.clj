(ns strelka-wat-bot.main
  "Responsible for starting application from command line"
  (:gen-class)
  (:require [taoensso.timbre :as log]
            [clojure.java.io :as io]
            [omniconf.core :as cfg]
            [ring.adapter.jetty :refer [run-jetty]]

            [telegram.core :as telegram]
            [telegram.handlers :as th]
            [strelka-wat-bot.handler :refer [app]]
            [strelka-wat-bot.bot :as bot]))

(cfg/define {:dev {:description "Environment mode"
                   :type :boolean
                   :default false}
             :port {:description "HTTP port"
                    :type :number
                    :default 8080}
             :hostname {:description "where service is deployed"
                        :type :string
                        :required true}
             :telegram-token {:description "Token to connect to Telegram API"
                              :type :string
                              :required true
                              :secret true}
             :wit-token {:description "Token for Wit.ai service"
                         :type :string
                         :required true
                         :secret true}})

(defn init []
  (cfg/verify :quit-on-error true)
  (telegram/init! {:token (cfg/get :telegram-token)
                   ;:handlers [bot/sample-handler]
                   :polling true})
  (th/add-handler! bot/handler))

(defn ring-init []
  (let [local-config "dev-config.edn"]
    (if (.exists (io/as-file local-config))
      (cfg/populate-from-file local-config)
      (log/warn "Can't find local dev configuration file" local-config))
    (init)))

(defn -main [& args]
  (cfg/populate-from-env)
  (cfg/populate-from-cmd args)
  (init)
  (log/info "Starting server")
  (run-jetty app {:port (cfg/get :port) :join? false}))
