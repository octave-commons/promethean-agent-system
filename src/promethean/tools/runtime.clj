(ns promethean.tools.runtime
  (:require [promethean.tools.registry :as reg]
            [promethean.tool :as tool]
            [promethean.agent.runtime :as rt]))

(defn normalize-tool-result [x]
  (cond
    (and (map? x) (:content x)) x
    (string? x) {:content [{:type "text" :text x}]}
    :else {:content [{:type "json" :json x}]}))

(defn call-tool! [{:keys [registry bus agent] :as ctx} tool-name args]
  (let [t (reg/get-tool registry tool-name)]
    (when-not t (throw (ex-info "Unknown tool" {:tool tool-name})))
    (let [t* (tool/materialize-tool t)
          ctx* (assoc ctx :tool/name tool-name)]
      (rt/emit! bus {:event :tool/call :agent (:agent/id agent) :tool tool-name :args args})
      (let [raw ((:tool/impl t*) ctx* (or args {}))
            res (normalize-tool-result raw)]
        (rt/emit! bus {:event :tool/done :agent (:agent/id agent) :tool tool-name})
        res))))
