(ns promethean.agent.runtime
  (:require [clojure.core.async :as async]))

(defn make-bus [] {:events (async/chan 1024)})
(defn emit! [bus evt] (when bus (async/put! (:events bus) evt)))
