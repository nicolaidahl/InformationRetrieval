cd ..
java -cp bin au.edu.rmit.Query -BM25 -q 401 -n 20 lexicon invlist map -s ./test_data/stoplist foreign minorities germany > eval/eval_output_RR
java -cp bin au.edu.rmit.Query -BM25 -q 402 -n 20 lexicon invlist map -s ./test_data/stoplist behavioral genetics >> eval/eval_output_RR
java -cp bin au.edu.rmit.Query -BM25 -q 403 -n 20 lexicon invlist map -s ./test_data/stoplist osteoporosis >> eval/eval_output_RR
java -cp bin au.edu.rmit.Query -BM25 -q 405 -n 20 lexicon invlist map -s ./test_data/stoplist cosmic events >> eval/eval_output_RR
java -cp bin au.edu.rmit.Query -BM25 -q 408 -n 20 lexicon invlist map -s ./test_data/stoplist tropical storms >> eval/eval_output_RR

java -cp bin au.edu.rmit.Query -QEBM25 termLex termIndex termMap -r 10 -e 25 -q 401 -n 20 lexicon invlist map -s ./test_data/stoplist foreign minorities germany > eval/eval_output_QE
java -cp bin au.edu.rmit.Query -QEBM25 termLex termIndex termMap -r 10 -e 25 -q 402 -n 20 lexicon invlist map -s ./test_data/stoplist behavioral genetics >> eval/eval_output_QE
java -cp bin au.edu.rmit.Query -QEBM25 termLex termIndex termMap -r 10 -e 25 -q 403 -n 20 lexicon invlist map -s ./test_data/stoplist osteoporosis >> eval/eval_output_QE
java -cp bin au.edu.rmit.Query -QEBM25 termLex termIndex termMap -r 10 -e 25 -q 405 -n 20 lexicon invlist map -s ./test_data/stoplist cosmic events >> eval/eval_output_QE
java -cp bin au.edu.rmit.Query -QEBM25 termLex termIndex termMap -r 10 -e 25 -q 408 -n 20 lexicon invlist map -s ./test_data/stoplist tropical storms >> eval/eval_output_QE
cd eval
