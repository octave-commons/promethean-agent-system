(ns promethean.actions.core)
(defn enqueue [world action] (update world :actions/queue (fnil conj []) action))
(defn drain [world] [(or (:actions/queue world) []) (assoc world :actions/queue [])])
