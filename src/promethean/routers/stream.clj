(ns promethean.routers.stream
  (:require [promethean.router.core :refer [def-router]]
            [clojure.string :as str]))

(def-router router/stream
  {:cooldown-ms 1500
   :initial-gates {:screen/capture true :audio/stt true :vision/objects false :vision/ocr false}}
  (fn [ctx world]
    (let [app (get-in world [:screen :frame :app] "")
          stt (get-in world [:audio :stt :text] "")
          hint-lc (str/lower-case (str app "\n" stt))
          mode (cond
                 (re-find #"code|vscode|clojure|typescript" hint-lc) :coding
                 (re-find #"hp|ammo|enemy|boss|fight" hint-lc) :combat
                 :else :idle)
          fire? (get-in world [:novelty :last :fire] false)
          base (case mode
                 :coding {:screen/capture true :audio/stt true :vision/ocr true  :vision/objects false}
                 :combat {:screen/capture true :audio/stt true :vision/ocr false :vision/objects true}
                 :idle   {:screen/capture true :audio/stt true :vision/ocr false :vision/objects false}
                 {:screen/capture true :audio/stt true})
          gates (cond-> base
                  (and fire? (= mode :coding)) (assoc :vision/ocr true)
                  (and fire? (= mode :combat)) (assoc :vision/objects true))]
      {:mode mode :gates gates})))
