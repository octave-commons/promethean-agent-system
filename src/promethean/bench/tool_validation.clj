(ns promethean.bench.tool-validation
  (:require [promethean.tools.schema :as schema]))

(defn missing-required [tool args]
  (let [req (schema/required tool)]
    (->> req (remove #(contains? args %)) vec)))

(defn validate-tool-call [tool args]
  (let [{:keys [args repairs]} (schema/repair-args tool args)
        missing (missing-required tool args)]
    {:ok? (empty? missing)
     :missing missing
     :repairs repairs
     :repaired-args args}))
