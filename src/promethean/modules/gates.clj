(ns promethean.modules.gates
  (:require [promethean.modules.core :as mods]
            [clojure.string :as str]))

(defn gate->module-id [gate-k] (keyword (str/replace (name gate-k) "/" ".")))

(defn apply-gates! [gates]
  (doseq [[gate-k on?] gates]
    (when-let [m (mods/get-module (gate->module-id gate-k))]
      (if on? (mods/enable! m) (mods/disable! m))))
  true)
