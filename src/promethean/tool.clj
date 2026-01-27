(ns promethean.tool
  (:require [promethean.proto :as proto]))

(defmacro def-tool [id {:keys [proto description inputSchema hooks] :as meta} impl-fn]
  `(def ~id
     (merge
       {:tool/name ~(name id)
        :tool/description ~description
        :tool/inputSchema ~inputSchema
        :tool/proto ~proto
        :tool/hooks ~hooks
        :tool/impl-raw ~impl-fn}
       (dissoc ~meta :proto :description :inputSchema :hooks))))

(defn materialize-tool [tool]
  (let [p (when-let [pid (:tool/proto tool)] (proto/materialize pid))
        defaults (:proto/defaults p)
        ph (:proto/hooks p)
        lh (or (:tool/hooks tool) {})
        hooks {:before (into (vec (:before ph)) (get lh :before []))
               :after  (into (vec (:after ph))  (get lh :after []))
               :around (into (vec (:around ph)) (get lh :around []))}
        base (fn [ctx args] ((:tool/impl-raw tool) ctx args))
        wrapped (proto/wrap-around (:around hooks)
                 (fn [ctx args]
                   (let [ctx (merge defaults ctx)
                         pre (proto/run-before (:before hooks)
                                              (assoc ctx :tool/name (:tool/name tool))
                                              args)]
                     (if (contains? pre :return)
                       (:return pre)
                       (let [res (base (:ctx pre) (:args pre))]
                         (proto/run-after (:after hooks) (:ctx pre) (:args pre) res))))))]
    (assoc tool :tool/impl wrapped)))
