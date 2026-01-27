(ns promethean.llm.parse
  (:require [clojure.string :as str]
            [cheshire.core :as json]
            [clojure.edn :as edn]))

(defn- try-json [s] (try (json/parse-string s true) (catch Throwable _ nil)))
(defn- try-edn [s] (try (edn/read-string s) (catch Throwable _ nil)))

(defn- strip-fences [s]
  (-> s
      (str/replace #"(?s)```(?:json|tool|yaml|yml|edn)?\s*" "")
      (str/replace #"(?s)```" "")))

(defn- find-json-object [s]
  (let [s (strip-fences s)
        start (.indexOf s "{")]
    (when (>= start 0)
      (loop [i start depth 0]
        (when (< i (count s))
          (let [ch (.charAt s i)
                depth' (cond (= ch \{) (inc depth) (= ch \}) (dec depth) :else depth)]
            (cond
              (and (= depth' 0) (> depth 0)) (subs s start (inc i))
              :else (recur (inc i) depth'))))))))

(defn- parse-call-line [line]
  (when-let [[_ name args] (re-matches #"(?i)\s*CALL\s+([a-zA-Z0-9_\-\.]+)\s+(.*)" line)]
    {:tool_calls [{:name name :arguments (or (try-json args) {})}]}))

(defn parse-soft [s]
  (let [s0 (str/trim s)]
    (or
      (some parse-call-line (str/split-lines s0))
      (when-let [obj-str (find-json-object s0)]
        (or (try-json obj-str) (try-edn obj-str)))
      (or (try-json s0) (try-edn s0)))))

(defn ->step [text]
  (let [parsed (parse-soft text)]
    (if (and (map? parsed) (seq (:tool_calls parsed)))
      {:type :tool_calls
       :tool_calls (mapv (fn [idx {:keys [name arguments]}]
                           {:id (str "call_" (inc idx))
                            :name (str name)
                            :arguments (if (map? arguments) arguments {})})
                         (range) (:tool_calls parsed))}
      {:type :final :content [{:type "text" :text text}]})))
