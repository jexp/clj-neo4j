(ns clj-neo4j.test.core
  (:use [clj-neo4j.core])
  (:use [clojure.test]))

(defn once-fixture [f]
  (set-graph (org.neo4j.test.ImpermanentGraphDatabase.))
  (f)
  (shutdown)
)

(defn each-fixture [f]
  (clean-db)
  (f)
)
(use-fixtures :once once-fixture)
(use-fixtures :each each-fixture)

(deftest root-node 
  (is (= 0 (id (root))) "Root node with id 0"))

(deftest root-node-lookup 
  (is (= (root) (node 0)) "Node by id lookup root"))

(deftest root-node-props 
  (is (= {} (props (root))) "Root node with no props"))

(deftest create-node
  (let [n (in-tx (create {:a 1 "b" "b" :c true}))]
	(assert-predicate (and (= 1 (id n)) (= {"a" 1 "b" "b" "c" true} (props n ))) "node created")
  )
  (is (= 2 (count (all-nodes))) "Created node")
)

(defn test-rel []
  (in-tx (relate (root) (create {:a 1}) :type {:r 1}))
)
(deftest create-rel
  (let [r (test-rel)]
	(assert-predicate 
	  (and (= 0 (id r)) 
	       (= {"r" 1} (props r)) 
	       (= (root) (start-node r)) 
	       (not= 0 (id (end-node r)))
	       (= "type" (rel-type r))) "relationship created")
  )
  (is (= 1 (count (all-rels))) "Created relationship")
)

(deftest get-rel 
  (test-rel)
  (is (rel? (rel 0)))
)

(deftest set-prop
  (is (= {"key" "value"} (props (in-tx (prop (root) :key "value")))) "property set")
)