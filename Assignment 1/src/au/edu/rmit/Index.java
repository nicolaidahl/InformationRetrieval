package au.edu.rmit;

import java.io.File;

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
		
		StopperModule stopper = new SimpleStopperModule(new File(""));
		IndexerModule indexer = new SimpleIndexerModule();
		
		SimpleParser p = new SimpleParser(stopper, indexer);
		p.parseFile(file, stopper);
	}

}
