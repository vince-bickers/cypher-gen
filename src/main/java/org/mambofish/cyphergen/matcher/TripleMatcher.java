

package org.mambofish.cyphergen.matcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mambofish.cyphergen.schema.Edge;
import org.mambofish.cyphergen.schema.Schema;
import org.mambofish.cyphergen.statement.Collect;
import org.mambofish.cyphergen.statement.Match;
import org.mambofish.cyphergen.statement.Statements;

/**
 * Given an arbitrary graph schema, a starting point in the schema and a fetch horizon, the TripleMatcher creates a 
 * Cypher query that fetches all the nodes and edges in any graph or sub-graph conforming to that schema, up to the 
 * specified horizon.
 *
 * Regardless of the complexity of the schema it guarantees that no edge in a conforming graph will be traversed 
 * more than once.
 *
 * The basic operation of the TripleMatcher is to match triples - paths containing a single edge - radiating out from
 * the start node to the horizon. The TripleMatcher proceeds using a breadth-first search strategy.
 *
 * The TripleMatcher is sensitive to schema complexity. It works best when the average degree of the schema is
 * low, the schema contains few cycles and the fetch horizon is modest.
 *
 * @author vince
 */
public class TripleMatcher implements Matcher {

    /*
     * Adds a Match statement to the relevant iterationMatches collection and to the global list of generated matches.
     * Filters are applied to the match statement as appropriate to ensure that edges traversed by an earlier
     * match statement are not re-traversed by this one.
     */
    private void add(Match match, Statements statements, Map<Integer, Set<Match>> iterationMatchesMap) {

        String canonicalForm = match.canonicalForm();

        for (Match existingMatch : statements.matches()) {
            if (existingMatch.canonicalForm().equals(canonicalForm)) {
                match.filter(existingMatch.relBinding().id(), match.relBinding().id());
            }
        }

        addIterationMatch(iterationMatchesMap, match);
        statements.add(match);

    }

    /*
     * Adds a Match to the set of matches that have been generated at the same search iteration
     */
    private void addIterationMatch(Map<Integer, Set<Match>> iterationMatchesMap, Match match) {

        Set<Match> matches = iterationMatchesMap.get(match.iteration());

        if (matches == null) {
            iterationMatchesMap.put(match.iteration(), matches = new HashSet<>());
        }

        matches.add(match);

    }


    /*
     * Starts a new search
     */
    public Statements match(Schema schema, String startType, int horizon) {

        Statements statements = new Statements();

        statements.initialise(startType);

        if (horizon > 0) {

            //set up the iterationMatches
            Map<Integer, Set<Match>> iterationMatchesMap = new HashMap<>();

            // A list of variables that get created during the generation of cypher statements;
            Variables variables = new Variables();

            // Each iteration of the search get a set of variable bindings that it uses as inputs
            // the top iteration bindings are initialised with the variable representing the root of the search.
            Set<Binding> localBindings = new HashSet<>();
            localBindings.add(getBinding(startType, variables));

            // start the recursive search to the specified horizon
            search(schema, variables, statements, iterationMatchesMap, localBindings, 1, horizon);

        }

        return statements;
    }

    /*
     * Conducts a recursive search of the schema, from the current depth up to the specified horizon, generating
     * appropriate match statements as it goes. Each pass through the search space expands the set of edges by one
     * extra hop. The search therefore radiates out from the root breadth-first.
     *
     * Each iteration of the search generates a set of variable bindings that it passes to the next iteration.
     * The first iteration is passed the variable bindings for the root of the search (primary Match statement).
     *
     * The inputBindings therefore represent the end-points from the previous search iteration. For example, a search
     * from the root that had three incident edges would create 3 bindings for each of the node types at the end of
     * those edges. These three bindings would then form the start points for the subsequent iteration of the search.
     */
    private void search(Schema schema, Variables variables, Statements statements, Map<Integer, Set<Match>> iterationMatchesMap, Set<Binding> inputBindings, int depth, int horizon) {

        if (depth <= horizon) {

            for (Binding inputBinding : inputBindings) {

                 // obtain the node type of the binding, e.g. Person, Movie
                 // in order to generate the appropriate Match statement based on the schema.
                String type = inputBinding.type();

                 // now get the set of edges that are incident to this node type in the schema,
                 // and create Match statements for each one
                Set<Edge> edges = schema.edges(type);
                for (Edge edge : edges) {

                    // An edge is always returned by the schema in the outgoing direction x->y. If
                    // we are currently expanding the search from the incoming node, y, we must reverse the edge
                    // to ensure that the that the inputBinding (y) is on the left hand side of the
                    // match statement, i.e.  y<-x
                    if (type.equals(edge.getEnd())) {
                        edge = edge.reverse();
                    }

                    // create new bindings for the discovered edges and nodes
                    Binding relBinding = getBinding(edge.getType(), variables);
                    Binding outputBinding = getBinding(edge.getEnd(), variables);

                    // create a new match statement
                    Match match = new Match(depth, edge, inputBinding, outputBinding, relBinding);
                    if (!statements.has(match)) {
                        add(match, statements, iterationMatchesMap);
                    }
                }
            }

            // all nodes of the same type traversed to via different edges are collected into a unique set
            Collect collect = new Collect(iterationMatchesMap.get(depth));

            // there's no need to generate a collect output statement if we're at the search horizon
            // because there's no additional search to be done.
            if (depth < horizon) {
                statements.add(collect);
            }

            // tail recursive call to expand the search another iteration.
            search(schema, variables, statements, iterationMatchesMap, collect.merge(variables), depth + 1, horizon);
        }

         // if we get here, the search is done, and we unwind
    }

    /*
     * Creates and returns a new Variable binding for the specified type node label or edge type
     */
    private Binding getBinding(String type, Variables variables) {
        return new Binding(variables.add(type), type);
    }

}

