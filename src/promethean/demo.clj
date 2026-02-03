(ns promethean.demo
  (:require [promethean.agent.runtime :as ar]
            [promethean.tools.registry :as reg]
            [promethean.ollama.tools :as tools]
            [promethean.llm.ollama :as ollama]
            [promethean.llm.loop :as loop]
            [promethean.runtime.step :as step]
            [promethean.routers.stream]
            [promethean.modules.screen]
            [promethean.modules.vision]
            [promethean.modules.novelty]
            [promethean.modules.performer]))

(defn fake-ollama! [{:keys [prompt]}]
  (cond
    (re-find #"(?i)screen|overlay|caption" prompt)
    "{\"tool_calls\":[{\"name\":\"overlay_text\",\"arguments\":{\"msg\":\"hello\"}}]}"
    (re-find #"(?i)sfx|sound|dramatic" prompt)
    "CALL play_sfx {\"name\": \"dramatic_hit\"}"
    :else
    "Hello!"))

(defn run! []
  (let [bus (ar/make-bus)
        registry (-> (reg/make-registry)
                     (reg/register-tools! [tools/overlay_text tools/play_sfx tools/overlay-text tools/play_sound_effect_legacy]))
        llm (ollama/make-ollama-driver {:call-ollama! (fn [{:keys [prompt]}] (fake-ollama! {:prompt prompt}))})
        frames (atom [{:app "Desktop" :hash "a"} {:app "Game" :hash "b"} {:app "VSCode" :hash "c"}])
        ctx {:bus bus
             :registry registry
             :llm llm
             :agent {:agent/id "demo" :agent/model "fake" :agent/prompt "You are a demo agent."}
             :router-id :router.stream
             :screen/get-frame (fn []
                                 (let [f (first @frames)]
                                   (swap! frames #(vec (concat (rest %) [(first %)])))
                                   f))}
        world {}]
    ;; drive a few ticks
    {:world (-> world (step/step! ctx) (step/step! ctx) (step/step! ctx))
     :example-tool-loop
     (loop/tool-loop! ctx {:system "You can call tools."
                           :messages [{:role "user" :content [{:type "text" :text "Put hello on the screen."}]}]
                           :tools (reg/list-tools registry)
                           :model "fake"
                           :max-steps 3})}))
