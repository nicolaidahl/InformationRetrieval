Assignment 2
============
Nicolai Dahl Blicher-Petersen s3441163
Daniel Jonathan Smith s3361789

=== Running details ===

Indexing
--------

Compile: javac -sourcepath src -d bin src/au/edu/rmit/Index.java

Run:     java -cp bin au.edu.rmit.Index [-s <stoplist>] [-p] <sourcefile>
e.g.     java -cp bin au.edu.rmit.Index -s /share/a2/stoplist /share/a2/collection/latimes


Querying
--------

Compile: javac -sourcepath src -d bin src/au/edu/rmit/Query.java

Okapi BM25 Ranked Retrieval

Run:     java -cp bin au.edu.rmit.Query -BM25 -q <query-label> -n <num-results> <lexicon> <invlists> <map> [-s <stoplist>] <queryterm-1> [<queryterm-2> ... <queryterm-N>]
e.g.     java -cp bin au.edu.rmit.Query -BM25 -q 401 -n 10 lexicon invlist map -s /share/a2/stoplist foreign minorities germany

Okapi BM25 Ranked Retrieval with Query Expansion

IMPORTANT: The Query Expansion lexicon files must be specified immediately after the -QEBM25 flag 

Run:     java -cp bin au.edu.rmit.Query -QEBM25 <term lexicon> <term index> <term map> -r <R> -e <E> -q <query-label> -n <num-results> <lexicon> <invlists> <map> [-s <stoplist>] <queryterm-1> [<queryterm-2> ... <queryterm-N>]
e.g.     java -cp bin au.edu.rmit.Query -QEBM25 termLex termIndex termMap -r 10 -e 10 -q 401 -n 10 lexicon invlist map -s /share/a2/stoplist foreign minorities germany

Where:
    R = the number of top-ranked documents that are assumed to be relevant
    E = the number of terms that should be appended to the original query
