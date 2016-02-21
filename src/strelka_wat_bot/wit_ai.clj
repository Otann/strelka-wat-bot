(ns strelka-wat-bot.wit-ai
  (:require [taoensso.timbre :as log]
            [clj-http.client :as http]
            [omniconf.core :as cfg]))

(def base-url "https://api.wit.ai")

(defn parse [text]
  (let [url (str base-url "/message")
        query {:v "20160221"
               :q text}
        headers {:Authorization (str "Bearer " (cfg/get :wit-token))}
        response (http/get url {:as :json
                                :query-params query
                                :headers headers})]
    (-> response :body :outcomes)))
