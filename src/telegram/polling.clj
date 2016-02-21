(ns telegram.polling
  "Declares ways co communicate with Telegram Bot API"
  (:require [clojure.core.async :as a :refer [>!! <! go chan close! thread]]
            [telegram.api :as api]
            [telegram.handlers :refer [handle]]
            [taoensso.timbre :as log]))

;; this holds messages from Telegram
(def message-chan (atom nil))

;; this controls if loops are rolling
(def running (atom false))

(defn start!
  "Starts long-polling process"
  []
  (log/debug "Trying to start polling threads")
  (reset! message-chan (a/chan))
  (reset! running true)

  ;; Start infinite loop inside go-routine
  ;; that will pull messages from channel
  (go (loop []
        (handle (<! @message-chan))
        (if @running (recur))))

  ;; Start thread with polling process
  ;; that will populate channel
  (thread (loop [offset 0]
            (let [updates (api/get-updates {:offset offset})
                  new-offset (if (empty? updates)
                               offset
                               (-> updates last :update_id inc))]
              (doseq [update updates] (>!! @message-chan update))
              (if @running (recur new-offset)))))

  (log/info "Started long-polling for Telegram updates"))

(defn stop!
  "Stops everything"
  []
  (reset! running false)
  (close! @message-chan))


