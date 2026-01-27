(ns promethean.runtime.tick
  (:require [promethean.modules.core :as mods]
            [promethean.router.core :as router]
            [promethean.modules.gates :as gates]
            [promethean.context.fuse :as fuse]))

(defn tick! [{:keys [router-id] :as ctx} world]
  (let [r (router/get-router router-id)
        world (mods/tick-modules! ctx world)
        world (router/apply-router! ctx r world)
        _ (gates/apply-gates! (get-in world [:router :gates]))
        fused (fuse/fuse world)]
    (assoc world :context/fused fused)))
