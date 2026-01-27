(ns promethean.modules.screen
  (:require [promethean.modules.core :refer [def-module]]
            [promethean.modules.protos :as protos]))

(def-module screen/capture
  {:proto proto/module-passive :enabled true}
  (fn [ctx world _module]
    (if-let [get-frame (:screen/get-frame ctx)]
      (assoc-in world [:screen :frame] (get-frame))
      world)))
