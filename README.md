# cypher-gen
Experiments with Cypher generation under different operational constraints.

Large graphs, dense graphs, schema-based subgraphs...

When searching to any appreciable depth in a graph using Cypher, performance problems may arise due to naive traversals like `()-[*0..4]-()`.

These queries are easy to write, but they can return multiple copies of the same path, nodes and edges that the user is not interested in, and get stuck when traversing via nodes of very high degree (supernodes). In terms of performance, a simple query like this sucks.

Often, a user is able to define the nodes and relationships that they're interested in, by means of a `schema` - a graph meta-model.

Cypher-gen lets you create schemas very easily and then run different query strategies over them to see the Cypher that would be generated.

For example:

    // create a schema
    Schema schema = new Schema();
    
    schema.addEdge("Movie", "IN_GENRE", "Genre");
    schema.addEdge("Actor", "ACTED_IN", "Movie");
    schema.addEdge("User", "RATED", "Movie");
    schema.addEdge("User", "LIKES", "Genre");
    
    // select a query strategy
    Matcher matcher = new TripleMatcher();
    
    // generate a search for all movies, to depth 3
    Statements statements = matcher.match(schema, "Movie", 3);

    // Print the cypher
    System.out.println(statements.cypher());
    
    -->
    MATCH ($0:Movie)
    OPTIONAL MATCH $p1 = ($0)-[$r1:IN_GENRE]->($2:Genre)
    OPTIONAL MATCH $p2 = ($0)<-[$r3:RATED]-($4:User)
    OPTIONAL MATCH $p3 = ($0)<-[$r5:ACTED_IN]-($6:Actor)
    OPTIONAL MATCH $p4 = ($6)-[$r7:ACTED_IN]->($8:Movie) WHERE $r5<>$r7
    OPTIONAL MATCH $p5 = ($2)<-[$r9:IN_GENRE]-($10:Movie) WHERE $r1<>$r9
    OPTIONAL MATCH $p6 = ($2)<-[$r11:LIKES]-($12:User)
    OPTIONAL MATCH $p7 = ($4)-[$r13:RATED]->($14:Movie) WHERE $r3<>$r13
    OPTIONAL MATCH $p8 = ($4)-[$r15:LIKES]->($16:Genre) WHERE $r11<>$r15
    WITH
    $p1,$p2,$p3,$p4,$p5,$p6,$p7,$p8,
    $r1,$r3,$r5,$r7,$r9,$r11,$r13,$r15,
    $12,$16,
    COLLECT ([$10,$14,$8]) AS $cc1
    UNWIND $cc1 AS $c1
    UNWIND $c1 AS $i1
    WITH DISTINCT
    $i1 AS $17,
    $12,$16,
    $p1,$p2,$p3,$p4,$p5,$p6,$p7,$p8,
    $r1,$r3,$r5,$r7,$r9,$r11,$r13,$r15
    OPTIONAL MATCH $p9 = ($16)<-[$r18:IN_GENRE]-($19:Movie) WHERE $r1<>$r18 AND $r9<>$r18
    OPTIONAL MATCH $p10 = ($16)<-[$r20:LIKES]-($21:User) WHERE $r11<>$r20 AND $r15<>$r20
    OPTIONAL MATCH $p11 = ($17)-[$r22:IN_GENRE]->($23:Genre) WHERE $r1<>$r22 AND $r9<>$r22 AND $r18<>$r22
    OPTIONAL MATCH $p12 = ($17)<-[$r24:RATED]-($25:User) WHERE $r3<>$r24 AND $r13<>$r24
    OPTIONAL MATCH $p13 = ($17)<-[$r26:ACTED_IN]-($27:Actor) WHERE $r5<>$r26 AND $r7<>$r26
    OPTIONAL MATCH $p14 = ($12)-[$r28:RATED]->($29:Movie) WHERE $r3<>$r28 AND $r13<>$r28 AND $r24<>$r28
    OPTIONAL MATCH $p15 = ($12)-[$r30:LIKES]->($31:Genre) WHERE $r11<>$r30 AND $r15<>$r30 AND $r20<>$r30
    RETURN $p1,$p2,$p3,$p4,$p5,$p6,$p7,$p8,$p9,$p10,$p11,$p12,$p13,$p14,$p15



