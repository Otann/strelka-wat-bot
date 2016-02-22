(ns strelka-wat-bot.bot
  (:require [clojure.string :as s]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [telegram.api :as api]
            [cheshire.core :as json]
            [taoensso.timbre :as log]

            [strelka-wat-bot.wit-ai :as wit]
            [strelka-wat-bot.timepad :as timepad]
            [omniconf.core :as cfg]))

(def emoji {:bird        "\uD83D\uDC26"
            :rolled-eyes "\uD83D\uDE44"
            :thumbs-up   "\uD83D\uDC4D"
            :thinking    "\uD83E\uDD14"
            :grimacing   "\uD83D\uDE2C"
            :confused    "\uD83D\uDE15"
            :beer        "\uD83C\uDF7A"
            :wine        "\uD83C\uDF77"
            :martini     "\uD83C\uDF78"
            :coffee      "☕️"})

(def messages
  {:welcome (str "Hi! " (emoji :rolled-eyes) "\n"
                 "I can help you to know what events are planned on Strelka "
                 "at certain days. Just ask me thing like "
                 "“What's happening today?” or “What is planned for tomorrow?”")

   :help (str "Just ask me thing like "
              "“What's happening today?” or “What is planned for tomorrow?”")

   :unknown-command (str (emoji :rolled-eyes) " I don't know this command yet")})

(defn handle-command [{{chat-id :id} :chat text :text}]
  (let [parts (s/split text #"\s")
        command (peek parts)
        args (pop parts)]
    (case command
      "/start" (api/send-message chat-id (messages :welcome))
      "/help"  (api/send-message chat-id (messages :help))

      (api/send-message chat-id (messages :unknown-command)))))

(defn describe-events [chat-id events]
  (if (not-empty events)
    (let [event (peek events)
          photo-url (-> event :poster_image :default_url)
          photo-full (subs photo-url 0 (- (count photo-url) 18))]
      (api/send-message chat-id "Looks like this is will happen next:")
      (api/send-message chat-id {:parse_mode "Markdown"} (str "*" (:name event) "*"))
      (api/send-message chat-id (:description_short event))
      (api/send-message chat-id (:url event))
      #_(api/send-photo   chat-id photo-full))

    (api/send-message chat-id (str (emoji :confused)
                                   " Sorry, I cant find anything on Timepad"
                                   " for the nearest future."))))

(defn handle-message [{{chat-id :id} :chat text :text}]
  (if-let [outcome (-> (wit/parse text)
                       (peek))]
    (let [intent (:intent outcome)
          confidence (:confidence outcome)
          intent-datetime (-> outcome :entities :datetime (peek) :value)
          datetime (or (f/parse intent-datetime) (t/now))
          events (timepad/events datetime)]
      (log/debug (str "Recognized intent - " intent " (" confidence ") with datetime: " intent-datetime))
      (describe-events chat-id events))

    (api/send-message chat-id (str "Can't understand you, sorry " (emoji :grimacing)))))

(defn handler [update]
  (log/debug "Got update from bot:\n"
             (json/generate-string update {:pretty true}))

  (when-let [message (:message update)]
    (if-let [text (:text message)]
      (if (.startsWith text "/")
        (handle-command message)
        (handle-message message))
      
      (api/send-message (-> update :message :chat :id)
                        (str "I can only work with just text yet. "
                             "I am sorry " (emoji :grimacing) ". "
                             "But don't worry, I think I will learn soon.")))))


