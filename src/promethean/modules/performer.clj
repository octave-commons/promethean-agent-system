(ns promethean.modules.performer
  (:require [promethean.modules.core :refer [def-module]]
            [promethean.modules.protos :as protos]
            [promethean.performer.policy :as policy]))

(def-module performer/loop
  {:proto proto/module-passive :enabled true}
  (fn [_ctx world _]
    (policy/propose-actions world)))
