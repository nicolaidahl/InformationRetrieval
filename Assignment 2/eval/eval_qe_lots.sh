function qe {
    cd ..
    java -cp bin au.edu.rmit.Query -QEBM25 termLex termIndex termMap -r $1 -e $2 -q 401 -n 20 lexicon invlist map -s ./test_data/stoplist foreign minorities germany > eval/eval_output_QE_R$1_E$2
    java -cp bin au.edu.rmit.Query -QEBM25 termLex termIndex termMap -r $1 -e $2 -q 402 -n 20 lexicon invlist map -s ./test_data/stoplist behavioral genetics >> eval/eval_output_QE_R$1_E$2
    java -cp bin au.edu.rmit.Query -QEBM25 termLex termIndex termMap -r $1 -e $2 -q 403 -n 20 lexicon invlist map -s ./test_data/stoplist osteoporosis >> eval/eval_output_QE_R$1_E$2
    java -cp bin au.edu.rmit.Query -QEBM25 termLex termIndex termMap -r $1 -e $2 -q 405 -n 20 lexicon invlist map -s ./test_data/stoplist cosmic events >> eval/eval_output_QE_R$1_E$2
    java -cp bin au.edu.rmit.Query -QEBM25 termLex termIndex termMap -r $1 -e $2 -q 408 -n 20 lexicon invlist map -s ./test_data/stoplist tropical storms >> eval/eval_output_QE_R$1_E$2
    cd eval
    python eval_process.py eval_output_QE_R$1_E$2
}

qe 5 5
qe 10 5
qe 15 5
qe 20 5
qe 25 5
qe 30 5

qe 5 10
qe 10 10
qe 15 10
qe 20 10
qe 25 10
qe 30 10

qe 5 15
qe 10 15
qe 15 15
qe 20 15
qe 25 15
qe 30 15

qe 5 20
qe 10 20
qe 15 20
qe 20 20
qe 25 20
qe 30 20

qe 5 25
qe 10 25
qe 15 25
qe 20 25
qe 25 25
qe 30 25

qe 5 30
qe 10 30
qe 15 30
qe 20 30
qe 25 30
qe 30 30

