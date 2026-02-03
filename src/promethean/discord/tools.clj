(ns promethean.discord.tools
  (:require [promethean.tool :refer [def-tool]]
            [promethean.proto :as proto]))

;; Discord tool protocol with trace mixin
(proto/def-proto proto/discord-tool
  {:mixins [proto/mixin-trace]
   :defaults {}
   :hooks {:before [] :after [] :around []}})

(def-tool discord.update_status_text
  {:proto proto/discord-tool
   :description "Update bot's Discord status/activity text."
   :inputSchema {:type "object"
                 :properties {:status_text {:type "string"
                                            :description "Status text to display (e.g., 'coding', 'thinking', 'idle')"}
                              :activity_type {:type "string"
                                              :description "Type of activity: 'PLAYING', 'WATCHING', 'LISTENING', 'STREAMING'"
                                              :enum ["PLAYING" "WATCHING" "LISTENING" "STREAMING"]}}
                 :required ["status_text"]}}
  (fn [_ctx args]
    {:action "update_discord_status"
     :status_text (get args "status_text")
     :activity_type (get args "activity_type")
     :updated_at (System/currentTimeMillis)}))

(def-tool discord.update_profile
  {:proto proto/discord-tool
   :description "Update bot's Discord profile (username, avatar)."
   :inputSchema {:type "object"
                 :properties {:username {:type "string"
                                         :description "New username to set"}
                              :avatar_url {:type "string"
                                           :description "URL to new avatar image"}
                              :display_name {:type "string"
                                             :description "New display name"}}
                 :required []}}
  (fn [_ctx args]
    {:action "update_discord_profile"
     :changes {:username (get args "username")
               :avatar_url (get args "avatar_url")
               :display_name (get args "display_name")}
     :updated_at (System/currentTimeMillis)}))
