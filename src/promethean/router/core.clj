(ns promethean.router.core
  (:require [promethean.agent.runtime :as rt]))

(defonce ^:private *routers (atom {}))
(defn register-router! [r] (swap! *routers assoc (:router/id r) r) r)
(defn get-router [id] (get @*routers id))

(defmacro def-router [id {:keys [cooldown-ms initial-gates] :as meta} decide-fn]
  `(do
     (def ~id
       (register-router!
        (merge {:router/id ~(keyword (name id))
                :router/cooldown-ms ~(or cooldown-ms 1000)
                :router/last-change-ms (atom 0)
                :router/state (atom {:mode nil :gates ~(or initial-gates {})})
                :router/decide ~decide-fn}
               ~meta)))
     ~id))

(defn- stable? [router now]
  (>= (- now @(:router/last-change-ms router)) (:router/cooldown-ms router)))

(defn apply-router! [{:keys [bus] :as ctx} router world]
  (let [now (System/currentTimeMillis)
        {:keys [mode gates]} ((:router/decide router) ctx world)
        prev @(:router/state router)
        next {:mode mode :gates gates}]
    (if (or (= prev next) (not (stable? router now)))
      (assoc world :router prev)
      (do
        (reset! (:router/last-change-ms router) now)
        (reset! (:router/state router) next)
        (when bus (rt/emit! bus {:event :router/change :router (:router/id router) :mode mode :gates gates}))
        (assoc world :router next)))))
