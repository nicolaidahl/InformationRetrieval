package au.edu.rmit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import au.edu.rmit.indexing.IndexerModule;
import au.edu.rmit.indexing.SimpleIndexerModule;
import au.edu.rmit.parsing.DocIdHandler;
import au.edu.rmit.parsing.SimpleParser;
import au.edu.rmit.stopping.SimpleStopperModule;
import au.edu.rmit.stopping.StopperModule;

public class Index {

    /**
     * @param args
     */
    public static void main(String[] args) {

        File inputFileSmall = new File("test_data/latimes_small");
        File stopList = new File("test_data/stoplist");
        
        File lexicon = new File("lexicon");
        File invlist = new File("invlist");

        File mapFile = new File("map");

        StopperModule stopper = new SimpleStopperModule(stopList);
        IndexerModule indexer = new SimpleIndexerModule(lexicon, invlist);
        DocIdHandler documentHandler = new DocIdHandler();

        SimpleParser p = new SimpleParser(stopper, indexer, documentHandler);
        p.parseFile(inputFileSmall);

        // Test SimpleIndexerModule.addDocument()
        /*HashMap<String, Integer> testTerms = new HashMap<String, Integer>();
        int testDocId = documentHandler.getDocumentId("TestDoc1");
        testTerms.put("term", 1);
        testTerms.put("term2", 7);
        testTerms.put("term3", 3);
        indexer.addDocument(testDocId, testTerms);
        testTerms.remove("term");

        testDocId = documentHandler.getDocumentId("TestDoc2");
        testTerms.put("term2", 2);
        testTerms.put("term3", 6);
        testTerms.put("term4", 4);
        indexer.addDocument(testDocId, testTerms);*/


        // Write index and map to disk
        try {
            indexer.writeIndex();
            documentHandler.writeMap(mapFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
