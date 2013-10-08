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

Run:     java -cp bin au.edu.rmit.Query -BM25 -q <query-label> -n <num-results> <lexicon> <invlists> <map> [-s <stoplist>] <queryterm-1> [<queryterm-2> ... <queryterm-N>]
e.g.     java -cp bin au.edu.rmit.Query -BM25 -q 401 -n 10 lexicon invlist map -s /share/a2/stoplist foreign minorities germany

IMPORTANT: The Query Expansion lexicon files must be specified immediately after the -QEBM25 flag 

Run:     java -cp bin au.edu.rmit.Query -QEBM25 <term lexicon> <term index> <term map> -q <query-label> -n <num-results> <lexicon> <invlists> <map> [-s <stoplist>] <queryterm-1> [<queryterm-2> ... <queryterm-N>]
e.g.     java -cp bin au.edu.rmit.Query -QEBM25 termLex termIndex termMap -q 401 -n 10 lexicon invlist map -s /share/a2/stoplist foreign minorities germany
