(ns promethean.modules.protos
  (:require [promethean.proto :as proto]))

(proto/def-proto proto/module-passive
  {:defaults {:module/role :passive}
   :hooks {:around []}})
