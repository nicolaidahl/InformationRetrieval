package au.edu.rmit;

import java.io.File;
import java.util.ArrayList;

import au.edu.rmit.indexing.Posting;
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
    	
    	
    	File invlistFile = new File(args[0]);
    	validateFile(invlistFile, "inverted list");
    	File lexiconFile = new File(args[1]);
    	validateFile(lexiconFile, "lexicon");
    	File mapFile = new File(args[2]);
    	validateFile(mapFile, "map");

    	StringBuilder searchTerms = new StringBuilder();
    	for(int i = 3; i < args.length; i++)
    	{
    		searchTerms.append(args[i]);
    	}
    	
    	QueryEngine engine = new SimpleQueryEngine(lexiconFile, invlistFile, mapFile);
    	SimpleParser parser = new SimpleParser(new DummyStopperModule(), null, null, false);
    	
    	ArrayList<String> parsedTerms = parser.parseQueryString(searchTerms.toString());
    	
    	for (String term : parsedTerms)
    	{
    		SearchResult result = engine.getSearchResult(term);
    		
    		System.out.println(result.getSearchTerm());
    		System.out.println(result.getFrequency());
    		for (Posting posting : result.getPostings())
			{
				System.out.println(posting.getDocumentId() + " " + posting.getFrequency());
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
