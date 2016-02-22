(ns strelka-wat-bot.timepad
  (:require [clj-time.format :as f]
            [clj-http.client :as http]
            [omniconf.core :as cfg]))

(def base-url "https://api.timepad.ru/v1")

(def ^:private iso-8601 (f/formatter "yyyy-MM-dd'T'HH:mm:ss"))

(defn events [from-date]
  (let [url (str base-url "/events.json")
        query {:fields "registration_data,properties,locale,description_html,description_short"
               :sort "+starts_at"
               :organization_ids (cfg/get :timepad-org-id)
               :starts_at_min (f/unparse iso-8601 from-date)}
        response (http/get url {:as :json :query-params query})]
    (-> response :body :values)))
