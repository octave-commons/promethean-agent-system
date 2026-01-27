(ns promethean.tools.schema
  (:require [clojure.string :as str]))

(defn props [tool] (get-in tool [:tool/inputSchema :properties] {}))
(defn required [tool] (set (get-in tool [:tool/inputSchema :required] [])))

(defn- normalize-k [k]
  (-> (name k) str/lower-case (str/replace "-" "_") (str/replace " " "_")
      (str/replace #"[^\w_\.]" "")))

(defn property-names [tool] (->> (props tool) keys (map name) set))

(defn best-key-match [tool incoming-k]
  (let [incoming (normalize-k incoming-k)
        candidates (keys (props tool))
        normalized->orig (into {} (map (fn [k] [(normalize-k k) (name k)])) candidates)
        synonyms {"msg" "text" "message" "text" "caption" "text" "prompt" "text"
                  "file" "path" "filename" "path"}]
    (or (get normalized->orig incoming)
        (when-let [syn (get synonyms incoming)]
          (when (contains? (property-names tool) syn) syn)))))

(defn coerce-value [schema v]
  (let [t (:type schema)]
    (cond
      (nil? v) nil
      (nil? t) v
      (= t "string") (if (string? v) v (str v))
      (= t "integer") (cond (integer? v) v (number? v) (long v) :else v)
      (= t "number") (cond (number? v) v :else v)
      (= t "boolean") (cond (boolean? v) v (string? v) (contains? #{"true" "1" "yes" "y"} (str/lower-case v)) :else v)
      (= t "array") (cond (vector? v) v (seq? v) (vec v) :else [v])
      (= t "object") (if (map? v) v v)
      :else v)))

(defn repair-args [tool args]
  (let [args (if (map? args) args {})
        schema-props (props tool)
        schema-keys (property-names tool)
        repairs (atom [])
        renamed (reduce-kv
                 (fn [m k v]
                   (let [kname (name k)
                         target (best-key-match tool k)]
                     (cond
                       (contains? schema-keys kname) (assoc m kname v)
                       target (assoc m target v)
                       :else (assoc m (str "_extra." kname) v))))
                 {}
                 args)
        coerced (reduce-kv
                 (fn [m k v]
                   (if-let [ps (get schema-props (keyword k))]
                     (let [v2 (coerce-value ps v)]
                       (when (not= v v2)
                         (swap! repairs conj {:op :coerce :key k :from v :to v2}))
                       (assoc m k v2))
                     (assoc m k v)))
                 {}
                 renamed)]
    {:args coerced :repairs @repairs}))
