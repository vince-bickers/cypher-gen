package org.mambofish.cyphergen.schema;

import java.util.HashSet;
import java.util.Set;

/**
 * The Schema maintains a graph meta-model, consisting of Edge archetypes, and simple String values
 * corresponding to Node archetypes.
 *
 * @author vince
 */
public class Schema {

    private Set<Edge> edges = new HashSet<>();
    private Set<String> nodes = new HashSet<>();

    public Set<Edge> edges() {
        return edges;
    }

    public void addEdge(String start, String type, String end) {

        Edge edge = new Edge(start, end, type);
        edges.add(edge);
        nodes.add(start);
        nodes.add(end);
    }

    public Set<Edge> edges(String nodeType) {

        Set<Edge> incidentEdges = new HashSet();
        for (Edge edge : edges) {
            if (edge.getStart().equals(nodeType) || edge.getEnd().equals(nodeType)) {
                incidentEdges.add(edge);
            }
        }
        return incidentEdges;
    }

    public Set<String> nodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "Schema { " +
                "nodes= " + nodes +
                ", edges= " + edges +
                '}';
    }

    public void cypher() {

        for (Edge edge : edges) {

            System.out.println("CREATE " + edge.cypher());
        }
    }

}
