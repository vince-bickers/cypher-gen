package org.mambofish.cyphergen.statement;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.mambofish.cyphergen.matcher.Binding;
import org.mambofish.cyphergen.matcher.Variables;

/**
 * The Collect class represents a Collect cypher statement, and is responsible
 * for collecting the results of one iteration (where required) and passing
 * the unique items in the collection to the next iteration. This is required to
 * prevent the same traversal being matched multiple times due to duplicate
 * start nodes.
 *
 * @author vince
 */
public class Collect implements Statement {

    private final Map<String, List<Match>> map = new HashMap<>();
    private final Set<Match> matches;
    private boolean collected = false;
    private Map<String, Integer> mergedVariable = new HashMap<>();

    private static final AtomicInteger counter = new AtomicInteger(0);

    public Collect(Set<Match> matches) {
        this.matches = matches;
    }

    public Set<Binding> merge(Variables variables) {

        for (Match match : matches) {
            String key = match.edge().getEnd();

            List<Match> matchSet = map.get(key);
            if (matchSet == null) {
                map.put(key, matchSet = new ArrayList<>());
            }
            matchSet.add(match);
        }

        Set<Binding> merged = new HashSet<>();

        for (String key : map.keySet()) {
            List<Match> matchList = map.get(key);
            Match firstMatch = matchList.get(0);
            if (matchList.size() == 1) {
                merged.add(firstMatch.rhsBinding());
            } else {
                collected = true;
                String type = firstMatch.edge().getEnd();
                Integer var = variables.add(type);
                mergedVariable.put(type, var);
                merged.add(new Binding(var, type));
            }
        }

        return merged;

    }

    @Override
    public String cypher() {

        StringBuilder cypher = new StringBuilder();
        StringBuilder collector = new StringBuilder();
        StringBuilder unwinder  = new StringBuilder();
        StringBuilder passthru  = new StringBuilder();
        StringBuilder distinct  = new StringBuilder();

        Integer firstCollectionId = counter.get() + 1;
        Integer collectionId = 0;

        Map<Integer, String> collectionIdMappedKey = new HashMap<>();

        if (collected) {

            // collect or passthru
            for (String key : map.keySet()) {
                List<Match> matchList = map.get(key);
                if (matchList.size() > 1) {
                    collector.append("\nCOLLECT ([");
                    for (Match match : matchList) {
                        collector.append("$");
                        collector.append(match.rhsBinding().id());
                        collector.append(",");
                    }
                    collectionId = counter.incrementAndGet();
                    collectionIdMappedKey.put(collectionId, key);
                    collector.deleteCharAt(collector.length()-1);
                    collector.append("]) AS $cc");
                    collector.append(collectionId);
                    collector.append(",");
                } else {
                    passthru.append("$");
                    passthru.append(matchList.get(0).rhsBinding().id());
                    passthru.append(",");
                }
            }

            // unwind
            for (int i = firstCollectionId; i <= collectionId; i++) {
                unwinder.append("\nUNWIND $cc");
                unwinder.append(i);
                unwinder.append(" AS $c");
                unwinder.append(i);
                unwinder.append("\nUNWIND $c");
                unwinder.append(i);
                unwinder.append(" AS $i");
                unwinder.append(i);
            }

            // distinct
            for (int i = firstCollectionId; i <= collectionId; i++) {
                distinct.append("$i");
                distinct.append(i);
                distinct.append(" AS $");
                distinct.append(mergedVariable.get(collectionIdMappedKey.get(i)));
                distinct.append(",");
            }

            cypher.append("\n");
            cypher.append(passthru);
            cypher.append(collector);
            cypher.deleteCharAt(cypher.length()-1);
            cypher.append(unwinder);
            cypher.append("\nWITH DISTINCT\n");
            cypher.append(distinct);
            cypher.append("\n");
            cypher.append(passthru);
            cypher.append("\n");

        }

        return cypher.toString();
    }


}
