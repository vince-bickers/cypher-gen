

package org.mambofish.cyphergen.matcher;

import org.junit.Before;
import org.junit.Test;
import org.mambofish.cyphergen.schema.Schema;
import org.mambofish.cyphergen.statement.Statements;

/**
 * @author vince
 */
public class TripleMatcherTest {

    private Schema schema = new Schema();

    @Before
    public void init() {

        schema.addEdge("Movie", "IN_GENRE", "Genre");
        schema.addEdge("Actor", "ACTED_IN", "Movie");
        schema.addEdge("User", "RATED", "Movie");
        schema.addEdge("User", "LIKES", "Genre");

        // TODO : handle loops

        schema.addEdge("bruce:Actor", "ACTED_IN", "diehard:Movie");
        schema.addEdge("milla:Actor", "ACTED_IN", "fifth_element:Movie");
        schema.addEdge("bruce:Actor", "ACTED_IN", "fifth_element:Movie");
        schema.addEdge("fifth_element:Movie", "IN_GENRE", "sci_fi:Genre");
        schema.addEdge("vince:User", "LIKES", "sci_fi:Genre");
        schema.addEdge("vince:User", "RATED", "diehard:Movie");
        schema.addEdge("diehard:Movie", "IN_GENRE", "action:Genre");
        schema.addEdge("vince:User", "LIKES", "action:Genre");
        schema.addEdge("olly:User", "LIKES", "action:Genre");
        schema.addEdge("olly:User", "LIKES", "fantasy:Genre");
        schema.addEdge("harry_potter:Movie", "IN_GENRE", "fantasy:Genre");
        schema.addEdge("emma:Actor", "ACTED_IN", "harry_potter:Movie");
        schema.addEdge("rupert:Actor", "ACTED_IN", "harry_potter:Movie");
        schema.addEdge("daniel:Actor", "ACTED_IN", "harry_potter:Movie");


    }

    @Test
    public void shouldCreateUsingTripleMatcher() {
        TripleMatcher matcher = new TripleMatcher();
        Statements statements = matcher.match(schema, "Movie", 4);

        System.out.println(statements.cypher());
    }

    @Test
    public void shouldEmitGraph() {
        schema.cypher();
    }
}
