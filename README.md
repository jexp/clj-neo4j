# clj-neo4j

clj-neo4j is a tiny clojure wrapper around the embedded neo4j core-api. Currently set-up against 1.8.M04 but should also work with older versions.

## Usage

### Set up Database ###

With `(graph "dir")` you can create a new db-instance which is stored in a global value. Optionally pass in a map of properties.

`(shutdown)` cleans up, there is an automatic shutdown-hook added once. Set the graph database to an existing value, e.g. for testing to
an in-memory version (set-graph [db]).

### Lookup ###

* (graph) returns the current graph
* (root) returns the reference node
* (node id), (rel id), (nodes id1 id2) can be used to look primitives up by id.
* (all-nodes) and (all-rels) return lazy seq's of each of the types
* predicates (node?) and (rel?) test for the type of the element
* (prop [pc :name]) returns a property of a node or rel, (props [pc]) returns all as a map,  (prop-names [pc]) returns all property names
* relationship information with (start-node), (end-ndoe), (rel-type), (other-node)

### Updates ###

Neo4j is transactiona, so the macro (in-tx [exprs*]) can be used to run other commands transactionally.

* (create [props]) Create a node with the given properties, returns the new node
* (delete [node or rel]) Deletes the node or relationship
* (relate [node1 node2 :type props?]), returns a vector with [node1 rel node2] for easy deconstruction
* (set-props [node or rel] props) sets the map of properties on the element and returns the element
* (prop [pc :name value]) sets a single property, if value is null it will be removed
* (create-all [props*]) creates a node for each map provided

### Cypher ###
* (cypher [query params?]) executes cypher query
* 

### Index Operations ###
* (index-add node :index :key value) adds a node to an index
* (lookup :index :key value) retrieves all nodes indexed against this combination (lookup-single ...) finds a single node (or nil)

### Utils ###
* (clean-db) deletes the content of the current db, useful for testing
* (cypher-d [query]) realizes the whole query result for multiple inspection

## License

Copyright (C) 2012 Michael Hunger

Distributed under the Eclipse Public License, the same as Clojure.
