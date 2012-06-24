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

(deftest test-root-node 
  (is (= 0 (id (root))) "Root node with id 0"))

(deftest test-root-node-lookup 
  (is (= (root) (node 0)) "Node by id lookup root"))

(deftest test-root-node-props 
  (is (= {} (props (root))) "Root node with no props"))

(deftest test-create-node
  (let [n (in-tx (create {:a 1 "b" "b" :c true}))]
	(assert-predicate (and (= 1 (id n)) (= {"a" 1 "b" "b" "c" true} (props n ))) "node created")
  )
  (is (= 2 (count (all-nodes))) "Created node")
)

(defn test-rel []
  (let [[_ r _] (in-tx (relate (root) (create {:a 1}) :type {:r 1}))] r)
)
(deftest test-create-rel
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

(deftest test-get-rel 
  (let [r (test-rel)]
  	(is (rel? (rel (id r))))
  )
)

(deftest test-set-prop
  (is (= {"key" "value"} (props (in-tx (prop (root) :key "value")))) "property set")
)

(deftest test-set-multiple-prop
  (in-tx (set-props (root) {:a 1 :b "b" :c true}))
  (let [p (props (root))]
    (println (str "PROPS" props))
  	(is (= (list "a" "b" "c") (keys p)) "property set")
  	(is (= {"a" 1 "b" "b" "c" true} p) "property set")
  )
)

(deftest test-index-add
   (in-tx (index-add (root) :index :key :value))
   (is (= (root) (first (lookup :index :key :value)))) ; don't do that, use lookup-single
)

(deftest test-lookup-single
   (in-tx (index-add (root) :index :key :value))
;   (is (= (root) (lookup-single :index :key :value)))
)

(deftest test-predicates
  (test-rel)
  (is (node? (root)))
  (is (rel? (first (all-rels))))
)