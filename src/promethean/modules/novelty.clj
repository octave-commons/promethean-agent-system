(ns promethean.modules.novelty
  (:require [promethean.modules.core :refer [def-module]]
            [promethean.modules.protos :as protos]))

(defn- sig [world]
  {:app (get-in world [:screen :frame :app])
   :hash (get-in world [:screen :frame :hash])
   :stt (get-in world [:audio :stt :text])
   :mode (get-in world [:router :mode])})

(def-module novelty/detector
  {:proto proto/module-passive :enabled true}
  (fn [_ctx world module]
    (let [st* (:module/state module)
          prev (get @st* :prev)
          s (sig world)
          fire? (not= prev s)]
      (swap! st* assoc :prev s)
      (assoc-in world [:novelty :last] {:fire fire? :score (if fire? 0.65 0.0)}))))
