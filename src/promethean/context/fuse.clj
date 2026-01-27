(ns promethean.context.fuse)

(defn fuse [world]
  {:mode (get-in world [:router :mode])
   :salient (cond-> []
              (seq (get-in world [:vision :objects])) (conj {:type :vision/objects :top (take 5 (get-in world [:vision :objects]))})
              (seq (get-in world [:vision :text]))    (conj {:type :vision/text :top (take 5 (get-in world [:vision :text]))})
              (seq (get-in world [:audio :stt :text])) (conj {:type :audio/stt :text (get-in world [:audio :stt :text])}))})
