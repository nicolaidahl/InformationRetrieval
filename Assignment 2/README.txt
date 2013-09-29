Assignment 2
============
Nicolai Dahl Blicher-Petersen s3441163
Daniel Jonathan Smith s3361789

=== Running details ===

Indexing
--------

Compile: javac -sourcepath src -d bin src/au/edu/rmit/Index.java

Run:     java -cp bin au.edu.rmit.Index [-s <stoplist>] [-p] <sourcefile>
e.g.     java -cp bin au.edu.rmit.Index -s /share/a1/stoplist /share/a1/collection/latimes


Querying
--------

Compile: javac -sourcepath src -d bin src/au/edu/rmit/Query.java

Run:     java -cp bin au.edu.rmit.Query -BM25 -q <query-label> -n <num-results> <lexicon> <invlists> <map> [-s <stoplist>] <queryterm-1> [<queryterm-2> ... <queryterm-N>]
e.g.     java -cp bin au.edu.rmit.Query -BM25 -q 401 -n 10 lexicon invlist map -s test_data/stoplist foreign minorities germany

