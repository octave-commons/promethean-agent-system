
(ns promethean.cephalon-tools
  "Duck Cephalon tool registration"
  (:require [promethean.tools.registry :as reg]
            [promethean.discord.tools :as discord-tools]
            [promethean.self.tools :as self-tools]))

(defn register-cephalon-tools!
  "Register all Discord and self-action tools with the duck cephalon registry"
  [registry]
  (reg/register-tools!
    registry
    [discord-tools/discord.update_status_text
     discord-tools/discord.update_profile
     self-tools/self.set_desire
     self-tools/self.set_mood
     self-tools/self.add_aspiration
     self-tools/self.add_goal
     self-tools/self.add_interest
     self-tools/self.remove_aspiration
     self-tools/self.remove_goal
     self-tools/self.remove_interest]))
