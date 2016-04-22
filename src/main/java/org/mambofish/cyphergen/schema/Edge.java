package org.mambofish.cyphergen.schema;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents an Edge archetype in the Schema
 *
 * @author vince
 */
public class Edge {

    static AtomicLong counter = new AtomicLong(0L);

    private Long id;
    private String start;
    private String end;
    private String type;
    private String head;
    private String tail;

    public Edge(String start, String end, String type) {
        this.start = start;
        this.end = end;
        this.type = type;
        this.head = "-";
        this.tail = "->";
        this.id = counter.getAndIncrement();
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Path {" +
                "start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", type='" + type + '\'' +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        if (!start.equals(edge.start)) return false;
        if (!end.equals(edge.end)) return false;
        return type.equals(edge.type);

    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    public String cypher() {

        StringBuilder sb = new StringBuilder();

        sb.append ("(");
        sb.append (start);
        sb.append (")");
        sb.append (head);
        sb.append("[");
        sb.append (":");
        sb.append (type);
        sb.append ("]");
        sb.append (tail);
        sb.append ("(");
        sb.append (end);
        sb.append (")");

        return sb.toString();
    }

    public Edge reverse() {

        Edge edge = new Edge(start, end, type);

        edge.start = edge.end;
        edge.end = this.start;

        if (this.head.equals("-")) {
            edge.head = "<-";
            edge.tail = "-";
        }
        else {
            edge.head = "-";
            edge.tail = "->";
        }

        return edge;
    }

    public String lhsText() {
        return head;
    }

    public String rhsText() {
        return tail;
    }

    public Long id() {
        return id;
    }
}
