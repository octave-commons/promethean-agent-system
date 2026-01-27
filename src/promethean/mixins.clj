(ns promethean.mixins
  (:require [promethean.proto :as proto]))

(proto/def-proto proto/mixin-trace
  {:defaults {:trace/enabled true}
   :hooks {:around
           [(fn [next]
              (fn [& call-args]
                (let [t0 (System/nanoTime)]
                  (try
                    (let [res (apply next call-args)
                          dt (/ (- (System/nanoTime) t0) 1e6)
                          ctx (first call-args)
                          emit (:trace/emit ctx)]
                      (when emit (emit {:event :trace/done :ms dt}))
                      res)
                    (catch Throwable t
                      (let [ctx (first call-args)
                            emit (:trace/emit ctx)]
                        (when emit (emit {:event :trace/error :err (.getMessage t)})))
                      (throw t))))))]}})

(proto/def-proto proto/mixin-denylist
  {:hooks {:before
           [(fn [ctx args]
              (let [deny (set (:deny/tools ctx))
                    tool (:tool/name ctx)]
                (if (contains? deny tool)
                  {:return {:content [{:type "text" :text "Denied by policy"}]}}
                  {:ctx ctx :args args}))) ]}})
