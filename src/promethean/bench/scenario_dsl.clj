(ns promethean.bench.scenario-dsl)
(defmacro expect [type & kvs] `(merge {:expect/type ~type} ~(apply hash-map kvs)))
(defmacro step [id & {:as m}] `(merge {:step/id ~id} ~m))
(defmacro def-scenario [id & steps] `(def ~id {:scenario/id ~(name id) :scenario/steps ~(vec steps)}))
