package org.mambofish.cyphergen.statement;

import java.util.concurrent.atomic.AtomicLong;

import org.mambofish.cyphergen.matcher.Binding;
import org.mambofish.cyphergen.schema.Edge;

/**
 * The Match class represents a Match cypher statement. It is backed by an Edge instance, to which different
 * variable bindings are applied.
 *
 * @author vince
 */
public class Match implements Statement {

    private final int iteration;
    private final Edge edge;
    private final Binding lhsBinding;
    private final Binding rhsBinding;
    private final String id;
    private final Binding relBinding;

    private StringBuilder filterClause = new StringBuilder();

    private static final AtomicLong counter = new AtomicLong(0L);

    public Match(int level, Edge edge, Binding lhsBinding, Binding rhsBinding, Binding relBinding) {
        this.iteration = level;
        this.edge = edge;
        this.lhsBinding = lhsBinding;
        this.rhsBinding = rhsBinding;
        this.relBinding = relBinding;
        this.id = "$p" + counter.incrementAndGet();
    }

    public int iteration() { return iteration; }

    public Edge edge() { return edge; }

    public Binding rhsBinding() {
        return rhsBinding;
    }

    public Binding lhsBinding() {
        return lhsBinding;
    }

    public String cypher() {
        return cypher(edge, lhsBinding.id(), rhsBinding.id(), relBinding.id(), filterClause);
    }

    public String reverseCypher() {
        return cypher(edge.reverse(), rhsBinding.id(), lhsBinding.id(), relBinding.id(), filterClause);
    }

    public void filter(int x, int y) {
        if (filterClause.length() == 0) {
            filterClause.append(" WHERE ");
        } else {
            filterClause.append( " AND ");
        }
        filterClause.append("$r");
        filterClause.append(x);
        filterClause.append("<>");
        filterClause.append("$r");
        filterClause.append(y);
    }

    private String cypher(Edge edge, Integer lhsBinding, Integer rhsBinding, Integer relBinding, StringBuilder filterClause) {

        StringBuilder sb = new StringBuilder();

        sb.append("OPTIONAL MATCH ");
        sb.append(id);
        sb.append (" = ($");
        sb.append(lhsBinding);
        sb.append (")");
        sb.append (edge.lhsText());
        sb.append ("[$r");
        sb.append (relBinding);
        sb.append (":");
        sb.append (edge.getType());
        sb.append ("]");
        sb.append (edge.rhsText());
        sb.append ("($");
        sb.append (rhsBinding);
        sb.append (":");
        sb.append (edge.getEnd());
        sb.append (")");

        sb.append(filterClause);

        return sb.toString();
    }

    /*
     * returns a Cypher representation of this edge in abstract canonical form, that is:
     * (:S)-[:E]->(:T) where S and T are the source and target label names and E is the edge type
     */
    public String canonicalForm() {

        Edge e = this.edge;
        if (e.lhsText().equals("<-")) {
            e = e.reverse();
        }

        StringBuilder sb = new StringBuilder();

        sb.append ("(");
        sb.append (":");
        sb.append (e.getStart());
        sb.append (")");
        sb.append (e.lhsText());
        sb.append("[");
        sb.append (":");
        sb.append (e.getType());
        sb.append ("]");
        sb.append (e.rhsText());
        sb.append ("(");
        sb.append (":");
        sb.append (e.getEnd());
        sb.append (")");

        return sb.toString();
    }

    public String id() {
        return id;
    }

    public Binding relBinding() {
        return relBinding;
    }
}
