package au.edu.rmit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import au.edu.rmit.indexing.IndexerModule;
import au.edu.rmit.indexing.SimpleIndexerModule;
import au.edu.rmit.parsing.SimpleParser;
import au.edu.rmit.stopping.SimpleStopperModule;
import au.edu.rmit.stopping.StopperModule;

public class Index {

    /**
     * @param args
     */
    public static void main(String[] args) {

        File file = new File("test_data/latimes_small");

        File lexicon = new File("lexicon");
        File invlist = new File("invlist");

        StopperModule stopper = new SimpleStopperModule(new File(""));
        SimpleIndexerModule indexer = new SimpleIndexerModule(lexicon, invlist);

        SimpleParser p = new SimpleParser(stopper, indexer);
        p.parseFile(file, stopper);

        // Test SimpleIndexerModule.addDocument()
        HashMap<String, Integer> testTerms = new HashMap<String, Integer>();
        testTerms.put("term", 1);
        testTerms.put("term2", 7);
        testTerms.put("term3", 3);
        indexer.addDocument(1, testTerms);

        testTerms.remove("term");
        testTerms.put("term2", 2);
        testTerms.put("term3", 6);
        testTerms.put("term4", 4);
        indexer.addDocument(2, testTerms);

        try {
            indexer.writeIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
