(ns promethean.ollama.tools
  (:require [promethean.tool :refer [def-tool]]
            [promethean.proto :as proto]
            [promethean.mixins :as mixins]))

(proto/def-proto proto/tool-base
  {:mixins [proto/mixin-trace]
   :defaults {}
   :hooks {:before [] :after [] :around []}})

(def-tool overlay_text
  {:proto proto/tool-base
   :description "Overlay text on stream"
   :inputSchema {:type "object" :properties {:text {:type "string"}} :required ["text"]}}
  (fn [_ args] (str "[overlay_text] " (get args "text"))))

(def-tool play_sfx
  {:proto proto/tool-base
   :description "Play a sound effect"
   :inputSchema {:type "object" :properties {:name {:type "string"}} :required ["name"]}}
  (fn [_ args] (str "[play_sfx] " (get args "name"))))

(def-tool overlay-text
  {:proto proto/tool-base
   :description "DECOY legacy overlay"
   :inputSchema {:type "object" :properties {:msg {:type "string"}} :required ["msg"]}}
  (fn [_ _] "Deprecated overlay"))

(def-tool play_sound_effect_legacy
  {:proto proto/tool-base
   :description "DECOY legacy sfx"
   :inputSchema {:type "object" :properties {:sfx {:type "string"}} :required ["sfx"]}}
  (fn [_ _] "Deprecated sfx"))
