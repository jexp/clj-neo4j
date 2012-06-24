(ns clj-neo4j.core
  (:require [clojure.contrib.string :as string]) ; :only [as-str])
  (:import [org.neo4j.graphdb Node Relationship DynamicRelationshipType Direction]
		   [org.neo4j.kernel EmbeddedGraphDatabase]
		   [org.neo4j.tooling GlobalGraphOperations]
		)
)

(def neo)
(when-not (bound? #'neo) (def neo (atom nil)))

(defn shutdown
  ([] 
  	(if (:graph @neo)
	 	(do 
			(.shutdown (:graph @neo))
     		(reset! neo nil)
		)
  	))
  ([g] (.shutdown g))
)

(defn set-graph [gdb]
   (if (not @neo) (.addShutdownHook (Runtime/getRuntime) (Thread. shutdown)))
   (shutdown)
   (reset! neo {:graph gdb
            :cypher (org.neo4j.cypher.javacompat.ExecutionEngine. gdb)
            :index (.index gdb)})
	gdb)

(defn graph 
	([] (:graph @neo))
	([dir] (graph dir {}))
    ([dir opts] (set-graph (EmbeddedGraphDatabase. dir (java.util.LinkedHashMap. opts))))
)

; lookup methods
(defn root [] (.getReferenceNode (graph)))
(defn node [id] (.getNodeById (graph) id))
(defn nodes [& ids] (map node ids))
(defn rel [id] (.getRelationshipById (graph) id))


(defn node? [n] (instance? org.neo4j.graphdb.Node n))
(defn rel? [r] (instance? org.neo4j.graphdb.Relationship r))

; index operations
(defn index-names [] (seq (.nodeIndexNames (:index @neo))))

(defn index-add [node index key value] 
	(let [index (.forNodes (:index @neo) (string/as-str index))]
		(.add index node (string/as-str key) (string/as-str value))
		node
	)
)

(defn lookup [index key value]
	(let [index (.forNodes (:index @neo) (string/as-str index))]
		(.query index (string/as-str key) (string/as-str value))
    )
)

(defn lookup-single [index key value] (.getSingle (lookup index key value)))

(defn prop 
  ([pc name] (.getProperty pc name))
  ([pc name value] 
	(if  (nil? value) 
		(.removeProperty pc (string/as-str name))
		(.setProperty pc (string/as-str name) value))
	pc)
)

(defn set-props [pc props]
  (if (and pc props) (doseq [[p v] props] (prop pc p v)))
  pc
)

(defn prop-names [pc] (.getPropertyKeys pc))

; todo !
(defn props [pc]
  (apply hash-map (flatten (map #(identity [% (prop pc %)]) (prop-names pc))))
)

; transaction macro
(defmacro in-tx [& form] 
	`(let [tx# (.beginTx (graph))] 
		(try 
			(let [r# (do ~@form)] 
				(.success tx#) 
				r#
			)
			(finally (.finish tx#))
		)
	)
)

; todo cache
(defn as-rel-type [type] (DynamicRelationshipType/withName (string/as-str type)))

; mutation
(defn create [props]
   (set-props (.createNode (graph)) props)
)

(defn create-all [& props]
	(let [nodes (if (seq? props) props (list props))]
	   (dorun (map create nodes))
	)
)

(defn relate 
"Creates a relationship between n1 and n2 with type and optional properties map"
   ([n1 n2 type] (relate n1 n2 type {}))
   ([n1 n2 type props] 
	 [n1 (set-props (.createRelationshipTo n1 n2 (as-rel-type type)) props) n2]
   )
)

(defn delete [pc] (.delete pc))

; query operations per element

(defn rel-type [rel] (.. rel getType name))
(defn start-node [rel] (.getStartNode rel))
(defn end-node [rel] (.getEndNode rel))
(defn other-node [rel n] (.getOtherNode rel n))

(defn id [pc] (.getId pc))

; global operations
(defn global [] (GlobalGraphOperations/at (graph)))
(defn all-nodes [] (seq (.getAllNodes (global))))
(defn all-rels [] (seq (.getAllRelationships (global))))

(defn cypher 
  ([query] (cypher query {}))
  ([query params] (.execute (:cypher @neo) query params))
)

; cypher
(defn cypher-result [r] {:columns (vec (.columns r)) :rows (doall (seq r))})
(defn cypher-d [& p] (cypher-result (apply p cypher)))

(defn clean-db []
   (in-tx
   	  (let [r (root)
      	    rels (count (map delete (all-rels)))
      	    nodes (count (map delete (filter #(not= r %) (all-nodes))))]
            (dorun (map #(prop r % nil) (prop-names r)))
      {:nodes nodes :rels rels}
	  )
   )
)

(def rel-dirs {"in" Direction/INCOMING "out" Direction/OUTGOING "both" Direction/BOTH})
(defn rel-dir [dir] (rel-dirs (string/as-str dir) Direction/BOTH))

(defn rels 
  ([n] (seq (.getRelationships n)))
; n dir
; n type
; dirs = map
  ([n dirs] (flatten (map (fn [[type dir]] (rels n type dir)) (seq dirs))))
  ([n type dir] (.getRelationships n (as-rel-type type) (rel-dir dir)))
)

; desc [:type 2nd {}:in :type 3rd]
; destructuring of a path, as it is a seq [a _ b r2 c] p
(defn path [start desc]
  ; traversal description
  ; or lazy for comprehension with binding for path from the right
)

; cypher in clojure, with lazy for comprehensions
; (take,drop,sort,)(for [s1 (node 1) s2 (lookup :index :key :value)] :where :while)

(defn rl [] (use 'clj-neo4j.core :reload-all))
