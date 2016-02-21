(ns telegram.api
  (:require [taoensso.timbre :as log]
            [clj-http.client :as http]))

(def base-url "https://api.telegram.org/bot")

(def token (atom nil))

(defn get-updates
  "Receive updates from Bot via long-polling endpoint"
  [{:keys [limit offset timeout]}]
  (let [url (str base-url @token "/getUpdates")
        query {:timeout (or timeout 1)
               :offset  (or offset 0)
               :limit   (or limit 100)}
        resp (http/get url {:as :json :query-params query})]
    (-> resp :body :result)))

(defn set-webhook
  "Register WebHook to receive updates from chats"
  [webhook-url]
  (let [url   (str base-url @token "/setWebhook")
        query {:url webhook-url}
        resp  (http/get url {:as :json :query-params query})]
    (log/debug "Registering WebHook, Telegram returned:" (:body resp))))

(defn send-message
  "Sends message to user"
  [chat-id text]
  (log/debug "Sending message" text "to" chat-id)
  (let [url (str base-url @token "/sendMessage")
        query {:chat_id chat-id
               :text text}
        resp (http/get url {:as :json :query-params query})]
    (log/debug "Got response from server" (:body resp))))