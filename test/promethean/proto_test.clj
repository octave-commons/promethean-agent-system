(ns promethean.proto-test
  (:require [clojure.test :refer [deftest testing is]]
            [promethean.proto :as proto]))

(deftest test-proto-registration
  (testing "Proto registration and retrieval"
    (let [proto-id :test.proto
          proto-def {:description "Test proto"}]
      
      ;; Register proto
      (is (= proto-id (proto/register-proto! proto-id proto-def)))
      
      ;; Retrieve proto
      (let [retrieved (proto/get-proto proto-id)]
        (is (not (nil? retrieved)))
        (is (= "Test proto" (:description retrieved))))
      
      ;; Clean up
      (swap! proto/*protos dissoc proto-id))))

(deftest test-def-proto-macro
  (testing "def-proto macro functionality"
    (let [proto-id :test.macro.proto]
      
      ;; Use def-proto macro
      (proto/def-proto proto-id 
        {:description "Macro test proto"})
      
      ;; Verify registration
      (let [retrieved (proto/get-proto proto-id)]
        (is (not (nil? retrieved)))
        (is (= "Macro test proto" (:description retrieved)))
        (is (= proto-id (:proto/id retrieved))))
      
      ;; Clean up
      (swap! proto/*protos dissoc proto-id))))

(deftest test-unknown-proto-handling
  (testing "Handling of unknown protos"
    (is (nil? (proto/get-proto :nonexistent.proto)))
    
    (is (thrown-with-msg?
          clojure.lang.ExceptionInfo
          #"Unknown proto"
          (proto/materialize :nonexistent.proto)))))