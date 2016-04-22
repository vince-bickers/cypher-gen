package org.mambofish.cyphergen.matcher;

import java.util.*;

/**
 * The Variables class looks after the allocation of new variables to nodes and edges in the graph
 * as they are discovered during the search.
 *
 * @author vince
 */
public class Variables {

    // for each type we maintain a set of binding ids (TODO - replace this with the actual Binding)
    private Map<String, Set<Integer>> typeVariables = new HashMap<>();

    // for each binding id we maintain the type it is bound to
    private Map<Integer, String> variables = new HashMap<>();

    // for each type we maintain its highest binding id
    private Map<String, Integer> highWaterMark = new HashMap<>();

    private int index = 0;

    public Integer add(String type) {

        Set<Integer> instances = typeVariables.get(type);
        if (instances == null) {
            typeVariables.put(type, instances = new HashSet<>());
        }
        instances.add(index);
        variables.put(index, type);
        highWaterMark.put(type, index);

        index++;
        return index -1;
    }

    public void list() {
        for (Map.Entry<String, Set<Integer>> entry : typeVariables.entrySet()) {
            System.out.println(entry);
        }
    }

    public String type(Integer key) {
        return variables.get(key);
    }

    public Integer highWaterMark(String type) {
        return highWaterMark.get(type);
    }

    public Set<String> types() {
        return typeVariables.keySet();
    }

    public Set<Integer> bindings(String type) {
        return typeVariables.get(type);
    }
}
