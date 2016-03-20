(ns strelka-wat-bot.bot
  (:require [clojure.string :as s]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [telegram.api :as api]
            [cheshire.core :as json]
            [taoensso.timbre :as log]
            [omniconf.core :as cfg]

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

(def messages
  {:welcome (str "Hi! " (emoji :rolled-eyes) "\n"
                 "I can help you to know what events are planned on Strelka "
                 "at certain days. Just ask me thing like "
                 "“What's happening today?” or “What is planned for tomorrow?”")

   :help (str "Just ask me thing like "
              "“What's happening today?” or “What is planned for tomorrow?”")

   :unknown-command (str (emoji :rolled-eyes) " I don't know this command yet")})

(defn date->str
  "Method of displaying dates to look more locally"
  [date]
  (f/unparse (f/formatter "MMMM d" (t/time-zone-for-id "Europe/Moscow"))
             date))

(defn event->str
  "Helps to render an event data to a string"
  [event]
  (str "*" (:name event) "*\n"
       (:description_short event) "\n"
       (:url event)))

(defn handle-date
  "For passed data sends information about it to a provided chat"
  [chat-id requested-date]
  (let [timezone (t/time-zone-for-id (cfg/get :org-timezone))
        local-date (t/to-time-zone requested-date timezone)
        events (timepad/events local-date)]
    (if (not-empty events)
      (let [event (peek events)
            event-date (-> event :starts_at (f/parse))
            matches-date (t/within? (t/interval requested-date (t/plus requested-date (t/days 1)))
                                    event-date)]
        (if matches-date
          (api/send-message chat-id {:parse_mode "Markdown"}
                            (str "Looks like this will happen on "
                                 (date->str event-date) ":\n"
                                 (event->str event)))
          (api/send-message chat-id {:parse_mode "Markdown"}
                            (str "Sorry " (emoji :grimacing) ", nothing is happening on Strelka on "
                                 (date->str local-date) ".\n"
                                 "But here is an event that will happen next, on "
                                 (date->str event-date) ":\n"
                                 (event->str event)))))

      (api/send-message chat-id (str (emoji :confused)
                                     " Sorry, I cant find anything on Timepad"
                                     " for the nearest future.")))))

(defn handle-message
  "Handles raw text message from a user"
  [{{chat-id :id} :chat text :text}]
  (if-let [outcome (-> (wit/parse text)
                       (peek))]
    (let [intent (:intent outcome)
          confidence (:confidence outcome)
          intent-datetime (-> outcome :entities :datetime (peek) :value)
          request-datetime (if intent-datetime
                               ;FIXME: Dirty hack, Wit.ai have +4 timezone for Moscow =/
                               (-> intent-datetime (f/parse) (t/plus (t/hours 1)))
                               (t/today-at 00 00))]
      (log/debug (str "Recognized intent - " intent " (" confidence ") with datetime: " intent-datetime))
      (handle-date chat-id request-datetime))

    (api/send-message chat-id (str "Can't understand you, sorry " (emoji :grimacing)))))

(defn handle-command
  "Describes how to handle commands from a user"
  [{{chat-id :id} :chat text :text}]
  (let [parts (s/split text #"\s")
        command (peek parts)]
    (case command
      "/start" (api/send-message chat-id (messages :welcome))
      "/help"  (api/send-message chat-id (messages :help))
      "/today" (handle-date chat-id (t/today-at 00 00))
      "/tomorrow" (handle-date chat-id (t/plus (t/today-at 00 00) (t/days 1)))

      (api/send-message chat-id (messages :unknown-command)))))

(defn handler
  "Handles update object that the bot received from a Telegram API"
  [update]
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


