package au.edu.rmit;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

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
        
        StopperModule stopper = new SimpleStopperModule(stopList);
        IndexerModule indexer = new SimpleIndexerModule();
        DocIdHandler documentHandler = new DocIdHandler();

        SimpleParser p = new SimpleParser(stopper, indexer, documentHandler);
        p.parseFile(inputFileSmall);

        File lexicon = new File("lexicon");
        File invlist = new File("invlist");

        File mapFile = new File("map");
        
        // Write index and map to disk
        try {
            indexer.writeIndex(lexicon, invlist);

            documentHandler.writeMap(mapFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
