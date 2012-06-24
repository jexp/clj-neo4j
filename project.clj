(defproject clj-neo4j "1.0.0-SNAPSHOT"
  :description "Simple clojure binding for Neo4j"
  :dependencies [[org.clojure/clojure "1.2.1"] 
				 [org.clojure/clojure-contrib "1.2.0"] 
                 [org.neo4j/neo4j "1.8.M04"]
				 [org.neo4j/neo4j-kernel "1.8.M04" :type "test-jar" :scope "test"]
				])