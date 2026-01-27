(ns promethean.runtime.step
  (:require [promethean.runtime.tick :as base]
            [promethean.actions.exec :as exec]))
(defn step! [ctx world] (exec/exec-actions! ctx (base/tick! ctx world)))
