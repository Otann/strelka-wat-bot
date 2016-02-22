(ns strelka-wat-bot.bot
  (:require [clojure.string :as s]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [telegram.api :as api]
            [cheshire.core :as json]
            [taoensso.timbre :as log]

            [strelka-wat-bot.wit-ai :as wit]
            [strelka-wat-bot.timepad :as timepad]))

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

(defn date->str [date] (f/unparse (f/formatter "MMMM d" (t/time-zone-for-id "Europe/Moscow"))
                                  date))

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
        command (peek parts)]
    (case command
      "/start" (api/send-message chat-id (messages :welcome))
      "/help"  (api/send-message chat-id (messages :help))

      (api/send-message chat-id (messages :unknown-command)))))

(defn event->str [event]
  (str "*" (:name event) "*\n"
       (:description_short event) "\n"
       (:url event)))

(defn describe-events [chat-id requested-date events]
  (if (not-empty events)
    (let [event (peek events)
          event-date (-> event :starts_at (f/parse))
          matches-date (t/within? (t/interval requested-date (t/plus requested-date (t/days 1)))
                                  event-date)]
      (log/debug "Well,"
                 "\n event-date:" event-date
                 "\n requested-date:" requested-date
                 "\n matches:" matches-date)
      (if matches-date
        (api/send-message chat-id {:parse_mode "Markdown"}
                          (str "Looks like this is will happen on "
                               (date->str event-date) ":\n"
                               (event->str event)))
        (api/send-message chat-id {:parse_mode "Markdown"}
                          (str "Sorry " (emoji :grimacing) ", nothing happens on Strelka on "
                               (date->str requested-date) ".\n"
                               "But here is event that will happen next, on "
                               (date->str event-date) ":\n"
                               (event->str event)))))

    (api/send-message chat-id (str (emoji :confused)
                                   " Sorry, I cant find anything on Timepad"
                                   " for the nearest future."))))

(defn handle-message [{{chat-id :id} :chat text :text}]
  (if-let [outcome (-> (wit/parse text)
                       (peek))]
    (let [intent (:intent outcome)
          confidence (:confidence outcome)
          intent-datetime (-> outcome :entities :datetime (peek) :value (f/parse) (t/plus (t/hours 1))) ; Wit.ai have +4 timezone for Moscow =/
          request-datetime (t/to-time-zone (or intent-datetime (t/today-at 00 00))
                                           (t/time-zone-for-id "Europe/Moscow"))
          events (timepad/events request-datetime)]
      (log/debug (str "Recognized intent - " intent " (" confidence ") with datetime: " intent-datetime))
      (describe-events chat-id request-datetime events))

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


