(ns promethean.llm.loop
  (:require [promethean.tools.runtime-repair :as tools]
            [promethean.agent.runtime :as rt]))

(defn append-message [state msg] (update state :messages conj msg))

(defn tool-result-message [{:keys [tool_call_id name result]}]
  {:role "tool" :tool_call_id tool_call_id :name name :content (:content result)})

(defn run-tool-calls! [ctx tool_calls]
  (mapv (fn [{:keys [id name arguments]}]
          {:id id :name name :result (tools/call-tool! ctx name arguments)})
        tool_calls))

(defn tool-loop! [{:keys [bus llm agent] :as ctx} {:keys [system messages tools max-steps model]}]
  (let [max-steps (or max-steps 12)]
    (loop [state {:system system :messages (vec messages) :step 0}]
      (if (>= (:step state) max-steps)
        {:type :final :content [{:type "text" :text "Max steps reached."}] :messages (:messages state)}
        (let [resp ((:llm/call llm) {:system (:system state) :messages (:messages state) :tools tools :model model :ctx ctx})]
          (case (:type resp)
            :final (do (rt/emit! bus {:event :llm/final :agent (:agent/id agent)})
                       (assoc resp :messages (:messages state)))
            :tool_calls
            (let [results (run-tool-calls! ctx (:tool_calls resp))
                  state (reduce (fn [st {:keys [id name result]}]
                                  (append-message st (tool-result-message {:tool_call_id id :name name :result result})))
                                state
                                results)]
              (recur (update state :step inc)))
            {:type :final :content [{:type "text" :text "Unknown model response type."}] :messages (:messages state)}))))))
