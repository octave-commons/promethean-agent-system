(ns promethean.benchmarks.tool-choice
  (:require [promethean.bench.dsl :refer [def-benchmark suite case calls abstains]]
            [promethean.ollama.tools :as tools]))

(def-benchmark tool-choice
  (suite "basic"
    (case "overlay caption"
      :prompt "Put the text hello on the screen."
      :expect (calls "overlay_text" {"text" "hello"})
      :decoys [tools/overlay-text])
    (case "play sfx"
      :prompt "Play a dramatic sound effect."
      :expect (calls "play_sfx")
      :decoys [tools/play_sound_effect_legacy])
    (case "abstain"
      :prompt "Say hello politely. Do not use any tools."
      :expect (abstains))))
