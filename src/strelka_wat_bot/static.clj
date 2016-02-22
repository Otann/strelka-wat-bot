(ns strelka-wat-bot.static
  "Layouts for server-side rendered pages"
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]))

(def home
  (html
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      [:title "Strelka Wat Bot"]

      (include-css "//cdn.jsdelivr.net/octicons/3.3.0/octicons.css")
      (include-css "//oss.maxcdn.com/semantic-ui/2.1.7/semantic.min.css")]

     [:body
      [:div.ui.container
       [:div.ui.vertical.masthead.center.aligned.segment
        {:style "padding-top: 100px"}
        [:div.ui.test.container
         [:img {:src "/img/1024.png" :style "height: 256px; width: 256px;"}]
         [:h1.header {:style {:margin-top "2em"
                              :font-size "3em"}}
          "Welcome to Strelka Wat Bot"]
         [:h3 "Telegram bot that knows what's happening in Strelka Institute"]
         [:a.ui.huge.basic.button {:href "https://github.com/otann/strelka-wat-bot"}
          "View Source"
          [:i.icon.right.github]]
         [:a.ui.huge.button {:href "https://telegram.me/StrelkaWatBot"}
          "Start Conversation"
          [:i.icon.right.arrow]]
         [:p {:style "padding-top: 80px;"} "Created by "
          [:a {:href "https://github.com/otann"} "Anton Chebotaev" ]]]]]]]))
