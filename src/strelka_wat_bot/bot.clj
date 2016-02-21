(ns strelka-wat-bot.bot
  (:require [telegram.api :as api]
            [cheshire.core :as json]
            [taoensso.timbre :as log]))

(defn sample-handler [update]
  (log/debug "Got update from bot:\n"
             (json/generate-string update {:pretty true}))
  (let [chat (-> update :message :chat)
        text (-> update :message :text)]
    (api/send-message (:id chat) "\uD83D\uDC26")))

