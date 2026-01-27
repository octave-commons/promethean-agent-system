(ns promethean.performer.policy
  (:require [promethean.actions.core :as a]))

(defn pick-line [{:keys [mode]}]
  (case mode :combat "CLUTCH!" :coding "Okay, we refactor." :idle "We chillin." "Huh."))

(defn propose-actions [world]
  (if (get-in world [:novelty :last :fire])
    (a/enqueue world {:action/type :tool/call
                      :tool "overlay_text"
                      :args {"text" (pick-line {:mode (get-in world [:context/fused :mode])})}})
    world))
