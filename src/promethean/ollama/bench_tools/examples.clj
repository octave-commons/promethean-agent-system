(ns promethean.ollama.bench-tools.examples
  "Example tools demonstrating the bench-tools registry system."
  (:require
    [promethean.ollama.bench-tools :as tools]))

(defn register-example-tools!
  "Register a collection of example tools for testing."
  []
  (tools/register-tool!
    {:name "math/add"
     :description "Add two integers"
     :domain :math
     :tags #{:arithmetic :deterministic}
     :parameters {:a {:type "integer" :required true}
                  :b {:type "integer" :required true}}
     :impl (fn [{:keys [a b]}] (+ a b))})
  (tools/register-tool!
    {:name "math/multiply"
     :description "Multiply two numbers"
     :domain :math
     :tags #{:arithmetic :deterministic}
     :parameters {:a {:type "number" :required true}
                  :b {:type "number" :required true}}
     :impl (fn [{:keys [a b]}] (* a b))})
  (tools/register-tool!
    {:name "math/divide"
     :description "Divide two numbers"
     :domain :math
     :tags #{:arithmetic :deterministic}
     :parameters {:a {:type "number" :required true}
                  :b {:type "number" :required true}}
     :impl (fn [{:keys [a b]}]
             (if (zero? b)
               (throw (ex-info "Division by zero" {:a a :b b}))
               (/ a b)))})
  (tools/register-tool!
    {:name "string/concat"
     :description "Concatenate two strings"
     :domain :text
     :tags #{:string-manipulation :deterministic}
     :parameters {:s1 {:type "string" :required true}
                  :s2 {:type "string" :required true}}
     :impl (fn [{:keys [s1 s2]}] (str s1 s2))})
  (tools/register-tool!
    {:name "validate-email"
     :description "Validate email format"
     :domain :validation
     :tags #{:validation :deterministic}
     :parameters {:email {:type "string" :required true}}
     :impl (fn [{:keys [email]}] (re-find #".+@.+\..+" email))})
  (tools/register-tool!
    {:name "generate-uuid"
     :description "Generate a random UUID"
     :domain :system
     :tags #{:deterministic :side-effect-free}
     :parameters {}
     :impl (fn [] (str (random-uuid)))}))

(defn lookup-example!
  "Demonstrate tool lookup functionality."
  []
  (println "Looking up math/add tool:")
  (let [add-tool (tools/tool-by-name "math/add")]
    (if add-tool
      (do
        (println "Found:" add-tool)
        (println "  Parameters:" (:parameters add-tool))
        (println "  Description:" (:description add-tool)))
      (println "Tool not found")))
  (println "\nAll tools:" (tools/tools))
  (println "Tools by tag :arithmetic:" (tools/tools-by-tag :arithmetic))
  (println "Tool count:" (count (tools/tools))))

(defn tools-by-tag-example!
  "Demonstrate tag-based tool filtering."
  [tag]
  (let [tagged-tools (tools/tools-by-tag tag)]
    (println "\nTools with tag" tag ":")
    (doseq [tool tagged-tools]
      (println "  -" (:name tool)))))

(defn tool-count-example!
  "Demonstrate tool count tracking."
  []
  (println "Total tools registered:" (count (tools/tools)))
  (println "Active tools count:" (count (tools/tools))))
