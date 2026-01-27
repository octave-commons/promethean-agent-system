(ns promethean.actions.exec
  (:require [promethean.actions.core :as a]
            [promethean.tools.runtime-repair :as tools]))
(defn exec-actions! [ctx world]
  (let [[actions world] (a/drain world)]
    (reduce (fn [w {:keys [action/type tool args] :as act}]
              (if (= type :tool/call)
                (do (tools/call-tool! ctx tool (or args {}))
                    (update w :actions/history (fnil conj []) act))
                (update w :actions/ignored (fnil conj []) act)))
            world actions)))
