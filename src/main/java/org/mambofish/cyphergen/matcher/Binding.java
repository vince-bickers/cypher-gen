package org.mambofish.cyphergen.matcher;

/**
 * Represents an association of a variable to a set of nodes of a specific type
 * that form the start or end nodes of a Matched edge in the graph
 *
 * @author vince
 */
public class Binding {

    private final String type;
    private final Integer id;

    public Binding(Integer id, String type) {

        this.id = id;
        this.type = type;

    }

    public String type() {
        return type;
    }

    public int id() {
        return id;
    }
}
