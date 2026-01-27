(ns promethean.ollama.resources
  "Centralized resource management and tracking for the entire framework."
  (:require
    [promethean.ollama.config :as config]
    [promethean.ollama.events :as events]))

;; Resource types
(def resource-types
  #{:file-lock :tool-execution :llm-call :memory-usage :cpu-usage})

;; Resource state tracking
(defonce ^:private !resource-state (atom {:allocations {} :limits {} :peak-counts {}}))

(defn allocate-resource!
  "Allocate a resource of given type with tracking."
  [resource-type resource-id owner options]
  (let [allocation-id (random-uuid)
        timestamp (System/currentTimeMillis)
        allocation {:id allocation-id
                    :type resource-type
                    :resource-id resource-id
                    :owner owner
                    :since timestamp
                    :options options}]
    (swap! !resource-state
           update-in [:allocations resource-type resource-id]
           assoc allocation-id allocation)
    (events/write-event!
      {:type :resource/allocated
       :resource-type resource-type
       :resource-id resource-id
       :allocation-id allocation-id
       :owner owner
       :options options})
    allocation-id))

(defn release-resource!
  "Release a previously allocated resource."
  [allocation-id]
  (let [allocations (:allocations @!resource-state)
        match (first (for [[resource-type resources] allocations
                          [resource-id allocs] resources
                          [id _alloc] allocs
                          :when (= id allocation-id)]
                      {:resource-type resource-type :resource-id resource-id}))]
    (when match
      (swap! !resource-state
             update-in [:allocations (:resource-type match) (:resource-id match)]
             dissoc allocation-id)
      (events/write-event!
        {:type :resource/released
         :resource-type (:resource-type match)
         :resource-id (:resource-id match)
         :allocation-id allocation-id}))))

(defn get-resource-allocations
  "Get all current allocations for a resource type."
  [resource-type]
  (get-in @!resource-state [:allocations resource-type]))

(defn get-allocation-info
  "Get specific allocation information."
  [allocation-id]
  (let [allocations (:allocations @!resource-state)]
    (some (fn [[_resource-type resources]]
            (some (fn [[_resource-id allocs]]
                    (get allocs allocation-id))
                  resources))
          allocations)))

(defn check-resource-limits!
  "Check if allocating would exceed resource limits."
  [resource-type request-owner]
  (let [current-allocations (get-resource-allocations resource-type)
        current-count (reduce + 0 (map count (vals (or current-allocations {}))))
        max-allowed (get-in @!resource-state [:limits resource-type])]
    (when (and (some? max-allowed) (>= current-count max-allowed))
      (throw (ex-info "Resource limit exceeded"
                      {:resource-type resource-type
                       :current current-count
                       :max-allowed max-allowed
                       :requester request-owner}))))

(defn set-resource-limit!
  "Set maximum allowed allocations for a resource type."
  [resource-type max-allocations]
  (swap! !resource-state assoc-in [:limits resource-type] max-allocations)
  (events/write-event!
    {:type :resource/limit-updated
     :resource-type resource-type
     :new-limit max-allocations}))

(defn get-resource-stats
  "Get usage statistics for all resource types."
  []
  (reduce-kv
    (fn [stats resource-type allocations]
      (let [current-count (reduce + 0 (map count (vals allocations)))
            peak-count (get-in @!resource-state [:peak-counts resource-type] 0)]
        (assoc stats resource-type {:current current-count :peak peak-count})))
    {}
    (:allocations @!resource-state)))

;; Resource usage monitoring helpers
(defn track-peak-usage!
  "Track peak usage for resource type."
  [resource-type count]
  (let [current-peak (get-in @!resource-state [:peak-counts resource-type] 0)
        new-peak (max current-peak count)]
    (swap! !resource-state assoc-in [:peak-counts resource-type] new-peak)))

(defn cleanup-expired-resources!
  "Clean up resources that have exceeded their TTL."
  []
  (let [now (System/currentTimeMillis)
        allocations (:allocations @!resource-state)
        expired-allocations
        (for [[resource-type resources] allocations
              [_resource-id allocs] resources
              [_id alloc] allocs
              :let [ttl (get-in alloc [:options :ttl-ms])]
              :when (and ttl (< now (+ (:since alloc) ttl)))]
          alloc)]
    (doseq [alloc expired-allocations]
      (release-resource! (:id alloc)))))

(defn- resource-conflict?
  "Check if two resource allocations conflict."
  [alloc1 alloc2]
  (and (= (:type alloc1) (:type alloc2))
       (or (and (= (:type alloc1) :file-lock)
                (= (:resource-id alloc1) (:resource-id alloc2)))
           (and (= (:type alloc1) :tool-execution)
                (some #(= (:resource-id %) (:resource-id alloc2))
                      (get-in alloc1 [:options :exclusive-resources]))))))

;; Resource conflict detection
(defn detect-resource-conflicts
  "Detect potential conflicts between resources."
  [resource-type]
  (let [allocations (get-resource-allocations resource-type)]
    (filter (fn [[id1 alloc1]]
              (some (fn [[id2 alloc2]]
                      (and (not= id1 id2)
                           (resource-conflict? alloc1 alloc2)))
                    (mapcat seq (vals allocations))))
            (mapcat seq (vals allocations)))))

;; Configuration-based resource limits
(defn init-resource-limits!
  "Initialize resource limits from configuration."
  []
  (let [cfg (config/get-config)]
    (set-resource-limit! :file-lock (:locks/max-concurrent cfg))
    (set-resource-limit! :tool-execution (:tool/max-concurrent cfg))
    (set-resource-limit! :llm-call (:llm/max-concurrent cfg))
    (set-resource-limit! :memory-usage (:memory/max-mb cfg))
    (set-resource-limit! :cpu-usage (:cpu/max-percent cfg))))

;; Periodic cleanup task
(defn start-resource-monitor!
  "Start background resource monitoring and cleanup."
  []
  (future
    (while true
      (Thread/sleep 30000)
      (cleanup-expired-resources!)
      (let [stats (get-resource-stats)]
        (events/write-event!
          {:type :resource/stats-updated
           :stats stats
           :timestamp (System/currentTimeMillis)})))))

)
