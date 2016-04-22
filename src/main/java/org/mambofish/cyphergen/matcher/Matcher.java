

package org.mambofish.cyphergen.matcher;

import org.mambofish.cyphergen.schema.Schema;
import org.mambofish.cyphergen.statement.Statements;

/**
 * @author vince
 */
public interface Matcher {

    Statements match(Schema schema, String start, int horizon);

}
