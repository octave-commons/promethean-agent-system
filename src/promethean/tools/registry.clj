(ns promethean.tools.registry)

(defn make-registry [] {:tools/by-name (atom {})})

(defn register-tool! [reg tool]
  (swap! (:tools/by-name reg) assoc (:tool/name tool) tool)
  reg)

(defn register-tools! [reg tools] (reduce register-tool! reg tools))
(defn get-tool [reg name] (get @(:tools/by-name reg) name))
(defn list-tools [reg] (vals @(:tools/by-name reg)))
