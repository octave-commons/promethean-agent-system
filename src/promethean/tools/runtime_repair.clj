(ns promethean.tools.runtime-repair
  (:require [promethean.tools.registry :as reg]
            [promethean.tools.runtime :as base]
            [promethean.bench.tool-validation :as v]
            [promethean.agent.runtime :as rt]))

(defn call-tool! [{:keys [registry bus agent] :as ctx} tool-name args]
  (let [tool (reg/get-tool registry tool-name)]
    (when-not tool (throw (ex-info "Unknown tool" {:tool tool-name})))
    (let [{:keys [ok? missing repairs repaired-args]} (v/validate-tool-call tool args)]
      (when (seq repairs)
        (rt/emit! bus {:event :tool/repair :agent (:agent/id agent) :tool tool-name :repairs repairs}))
      (when-not ok?
        (rt/emit! bus {:event :tool/invalid :agent (:agent/id agent) :tool tool-name :missing missing}))
      (base/call-tool! ctx tool-name repaired-args))))
