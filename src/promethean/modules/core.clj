(ns promethean.modules.core
  (:require [promethean.proto :as proto]
            [promethean.agent.runtime :as rt]))

(defonce ^:private *modules (atom {}))
(defn register-module! [m] (swap! *modules assoc (:module/id m) m) m)
(defn get-module [id] (get @*modules id))
(defn list-modules [] (vals @*modules))
(defn enable! [m] (reset! (:module/enabled? m) true) m)
(defn disable! [m] (reset! (:module/enabled? m) false) m)
(defn module-enabled? [m] (true? @(:module/enabled? m)))

(defn materialize-module [m]
  (let [p (proto/materialize (:module/proto m))
        defaults (:proto/defaults p)]
    (assoc m
           :module/defaults defaults
           :module/tick* (fn [ctx world module] ((:module/tick m) (merge defaults ctx) world module)))))

(defmacro def-module [id {:keys [proto enabled] :as meta} tick-fn]
  `(do
     (def ~id
       (register-module!
         (materialize-module
          (merge {:module/id ~(keyword (name id))
                  :module/proto ~proto
                  :module/enabled? (atom ~(if (contains? meta :enabled) enabled true))
                  :module/state (atom {})}
                 ~meta
                 {:module/tick ~tick-fn}))))
     ~id))

(defn tick-modules! [{:keys [bus] :as ctx} world]
  (reduce
    (fn [w m]
      (if (module-enabled? m)
        (do (when bus (rt/emit! bus {:event :module/tick :module (:module/id m)}))
            ((:module/tick* m) ctx w m))
        w))
    world
    (list-modules)))
