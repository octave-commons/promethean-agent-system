(ns promethean.llm.ollama
  (:require [clojure.string :as str]
            [promethean.llm.parse :as parse]))

(defn build-system [{:keys [system tools]}]
  (let [tool-lines (->> tools (map (fn [t] (str "- " (:tool/name t) ": " (:tool/description t)))) (str/join "\n"))]
    (str system "\n\n"
         "If you need a tool, respond with JSON like: {\"tool_calls\":[{\"name\":\"tool\",\"arguments\":{...}}]}\n"
         "Or: CALL tool {\"arg\":\"value\"}\n\n"
         "Available tools:\n" tool-lines)))

(defn messages->plain [messages]
  (->> messages
       (map (fn [m]
              (let [role (:role m) content (:content m)]
                (cond
                  (string? content) (str role ": " content)
                  (vector? content) (str role ": " (->> content (map (fn [c] (or (:text c) (pr-str c)))) (str/join "\n")))
                  :else (str role ": " (pr-str content))))))
       (str/join "\n\n")))

(defn make-ollama-driver [{:keys [call-ollama!]}]
  {:llm/call
   (fn [{:keys [system messages tools ctx model]}]
     (let [sys (build-system {:system system :tools tools})
           transcript (messages->plain messages)
           prompt (str sys "\n\n" transcript "\n\nassistant: ")
           text (call-ollama! {:model model :prompt prompt :ctx ctx})]
       (parse/->step text)))})
