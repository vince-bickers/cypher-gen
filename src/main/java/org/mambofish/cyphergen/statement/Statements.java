package org.mambofish.cyphergen.statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds an ordered list of statements to be executed to satisfy a search request
 *
 * @author vince
 */
public class Statements {

    private List<Statement> statements = new ArrayList();
    private String startType;

    public void add(Statement statement) {
        statements.add(statement);
    }

    /*
     * Determines whether the specified Match statement has been seen yet in the
     * the list of Match statements already generated
    */
    public boolean has(Match statement) {

        String cypher = statement.cypher();
        String reverseCypher = statement.reverseCypher();

        for (Statement existing : matches()) {
            String existingCypher = existing.cypher();
            if (existingCypher.equals(cypher) || existingCypher.equals(reverseCypher)) {
                return true;
            }
        }
        return false;

    }

    public List<Match> matches () {
        List<Match> matchStatements = new ArrayList<>();
        for (Statement statement : statements) {
            if (statement instanceof Match) {
                matchStatements.add((Match) statement);
            }
        }
        return matchStatements;
    }

    public String cypher() {

        StringBuilder sb = new StringBuilder();

        if (statements.size() > 0) {

            sb.append("MATCH ($0:");
            sb.append(startType);
            sb.append(")\n");

            StringBuilder stmtIds = new StringBuilder();
            StringBuilder edgeIds = new StringBuilder();
            for (Statement statement : statements) {

                String text = statement.cypher();

                if (statement instanceof Match) {
                    stmtIds.append(((Match) statement).id());
                    stmtIds.append(",");
                    edgeIds.append("$r"); // TODO fix this
                    edgeIds.append(((Match) statement).relBinding().id());
                    edgeIds.append(",");
                    sb.append(text);
                    sb.append("\n");
                } else if (statement instanceof Collect) {
                    if (text.length() > 0) {
                        sb.append("WITH\n");
                        sb.append(stmtIds);
                        sb.append("\n");
                        sb.append(edgeIds);
                        sb.append(text);
                        sb.append(stmtIds);
                        sb.append("\n");
                        sb.append(edgeIds);
                        sb.deleteCharAt(sb.length()-1);
                        sb.append("\n");
                    }
                }
            }
            stmtIds.deleteCharAt(stmtIds.length() - 1);
            sb.append("RETURN ");
            sb.append(stmtIds);
        } else {
            sb.append("MATCH ($0:");
            sb.append(startType);
            sb.append(")\nRETURN $0");
        }

        return sb.toString();
    }

    public void initialise(String startType) {
        this.startType = startType;
    }


}
