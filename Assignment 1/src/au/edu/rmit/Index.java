package au.edu.rmit;

import java.io.File;
import java.io.IOException;

import au.edu.rmit.indexing.IndexerModule;
import au.edu.rmit.indexing.SimpleIndexerModule;
import au.edu.rmit.parsing.DocIdHandler;
import au.edu.rmit.parsing.SimpleParser;
import au.edu.rmit.stopping.DummyStopperModule;
import au.edu.rmit.stopping.SimpleStopperModule;
import au.edu.rmit.stopping.StopperModule;

public class Index {

    /**
     * @param args
     */
    public static void main(String[] args) {
    	
    	String inputFormatHelp = "USAGE: index [-s <stoplist>] [-p] <sourcefile>"; 
    	
    	boolean shouldPrintTerms = false;
    	String dataFilePath = "";
    	boolean didTryToSetStopFile = false;
    	String stopFilePath = "";
    	
    	
    	for (int i = 0; i < args.length; i++)
		{
    		String arg = args[i];
    		
			if(arg.equals("-p"))
				shouldPrintTerms = true;
			else if(arg.equals("-s"))
			{
				didTryToSetStopFile = true;
				i++;
				if(i == args.length)
					break;
				else
					stopFilePath = args[i];
			}
			else
				dataFilePath = arg;
		}

    	File inputFile = new File(dataFilePath);
    	if(!inputFile.exists())
    	{
    		System.out.println("Please specify a valid source file path. " + 
    							(dataFilePath.equals("") ? "<empty>" : dataFilePath) + " is invalid.");
    		System.out.println(inputFormatHelp);
    		System.exit(-1);
    	}
    	
    	File stopList = null;
    	if(didTryToSetStopFile)
    	{
        	stopList = new File(stopFilePath);
        	
        	if(!stopList.exists())
        	{
        		System.out.println("Please specify a valid stoplist path. " + stopFilePath + " is invalid.");
        		System.out.println(inputFormatHelp);
        		System.exit(-1);
        	}
    	}
    	
    	//This ensures the parser never has to check if a stopper is present
    	StopperModule stopper;
    	if(stopList != null)
    		stopper = new SimpleStopperModule(stopList);
    	else
    		stopper = new DummyStopperModule();
    	
        IndexerModule indexer = new SimpleIndexerModule();
        DocIdHandler documentHandler = new DocIdHandler();

        SimpleParser p = new SimpleParser(stopper, indexer, documentHandler, shouldPrintTerms);
        p.parseFile(inputFile);
        
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
