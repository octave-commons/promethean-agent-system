(ns promethean.modules.vision
  (:require [promethean.modules.core :refer [def-module]]
            [promethean.modules.protos :as protos]))

(def-module vision/objects
  {:proto proto/module-passive :enabled false}
  (fn [ctx world _]
    (if-let [detect (:vision/detect ctx)]
      (assoc-in world [:vision :objects] (or (detect (get-in world [:screen :frame])) []))
      world)))

(def-module vision/ocr
  {:proto proto/module-passive :enabled false}
  (fn [ctx world _]
    (if-let [ocr (:vision/ocr ctx)]
      (assoc-in world [:vision :text] (or (ocr (get-in world [:screen :frame])) []))
      world)))
