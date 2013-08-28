Assignment 1
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

Run:     java -cp bin au.edu.rmit.Query <lexicon> <invlist> <map> <queryterm 1> [... <queryterm N>]
e.g.     java -cp bin au.edu.rmit.Query lexicon invlist map spam spam spam baked beans spam

