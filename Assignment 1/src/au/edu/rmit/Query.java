package au.edu.rmit;

import java.io.File;
import java.util.ArrayList;

import au.edu.rmit.indexing.Posting;
import au.edu.rmit.parsing.DocIdHandler;
import au.edu.rmit.parsing.InvalidDocumentIdException;
import au.edu.rmit.parsing.SimpleParser;
import au.edu.rmit.querying.QueryEngine;
import au.edu.rmit.querying.SearchResult;
import au.edu.rmit.querying.SimpleQueryEngine;
import au.edu.rmit.stopping.DummyStopperModule;

public class Query {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
    	if(args.length < 4)
    	{
    		printErrorMessageWithUsage("Too few arguments");
    		System.exit(-1);
    	}
    	
    	
        // Read in filenames from command line and make sure they exist
    	File lexiconFile = new File(args[0]);
    	validateFile(lexiconFile, "lexicon");

    	File invlistFile = new File(args[1]);
    	validateFile(invlistFile, "inverted list");

    	File mapFile = new File(args[2]);
    	validateFile(mapFile, "map");

        // Turn search terms from command line into a single string for parsing.
        // This ensures the same as rules are applied as indexing.
    	StringBuilder searchTerms = new StringBuilder();
    	String delimiter = "";
    	for(int i = 3; i < args.length; i++)
    	{
    	    searchTerms.append(delimiter);
    		searchTerms.append(args[i]);
    		delimiter = " ";
    	}

    	SimpleParser parser = new SimpleParser(new DummyStopperModule(), null, null, false);
    	ArrayList<String> parsedTerms = parser.parseQueryString(searchTerms.toString());

        // Initialise query engine and document handler
    	QueryEngine engine = new SimpleQueryEngine(lexiconFile, invlistFile);
    	DocIdHandler docIdHandler = new DocIdHandler(mapFile);
    	
    	for (String term : parsedTerms)
    	{
            // Retrieve search result from query engine and print results.
    		SearchResult result = engine.getSearchResult(term);
    		
    		System.out.println(result.getSearchTerm());
    		System.out.println(result.getFrequency());

            // Get raw document ID for each posting from doc ID handler and print to stdout with frequency
    		for (Posting posting : result.getPostings())
			{
				try {
                    System.out.println(docIdHandler.getRawDocumentId(posting.getDocumentId()) + " " + posting.getFrequency());
                } catch (InvalidDocumentIdException e) {
                    System.out.println("Invalid document ID " + posting.getDocumentId() + " found in search results!");
                }
			}
    	}
    	
    	
    }
    
    private static void validateFile(File file, String fileName)
    {
    	if(!file.exists())
    	{
    		printErrorMessageWithUsage("Please specify a valid " + fileName + " file path.");
    		System.exit(-1);
    	}
    		
    }
    
    private static void printErrorMessageWithUsage(String error)
    {
    	System.err.println(error);
    	System.err.println("USAGE: search <lexicon> <invlists> <map> <queryterm 1> [... <queryterm N>]");
    }

}
