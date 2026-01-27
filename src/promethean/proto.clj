(ns promethean.proto
  (:require [clojure.set :as set]))

(defonce ^:private *protos (atom {}))

(defn register-proto! [id m]
  (swap! *protos assoc id (assoc m :proto/id id))
  id)

(defn get-proto [id]
  (get @*protos id))

(defmacro def-proto
  "Define a prototype with optional :extends and :mixins."
  [id m]
  `(do (register-proto! ~id ~m) ~id))

(defn- merge-hooks [& hs]
  (let [ks [:before :after :around]]
    (reduce
      (fn [acc h]
        (reduce (fn [a k] (update a k (fnil into []) (get h k []))) acc ks))
      {:before [] :after [] :around []}
      hs)))

(defn- materialize* [id seen]
  (when (contains? seen id)
    (throw (ex-info "Prototype cycle" {:proto id})))
  (let [p (or (get-proto id) (throw (ex-info "Unknown proto" {:proto id})))
        parent (:extends p)
        mixins (:mixins p)
        parent* (when parent (materialize* parent (conj seen id)))
        mixins* (mapv #(materialize* % (conj seen id)) (or mixins []))
        defaults (apply merge (map :proto/defaults (concat (when parent* [parent*]) mixins* [p])))
        hooks (apply merge-hooks (map :proto/hooks (concat (when parent* [parent*]) mixins* [p])))]
    {:proto/id id :proto/defaults defaults :proto/hooks hooks}))

(defn materialize [id] (materialize* id #{}))

(defn wrap-around [around base]
  (reduce (fn [f mw] (mw f)) base around))

(defn run-before [befores ctx args]
  (loop [ctx ctx, args args, xs befores]
    (if (empty? xs)
      {:ctx ctx :args args}
      (let [r ((first xs) ctx args)]
        (cond
          (and (map? r) (contains? r :return)) r
          (and (map? r) (contains? r :ctx)) (recur (:ctx r) (:args r) (rest xs))
          :else (recur ctx args (rest xs)))))))

(defn run-after [afters ctx args result]
  (reduce (fn [res f] (f ctx args res)) result afters))
