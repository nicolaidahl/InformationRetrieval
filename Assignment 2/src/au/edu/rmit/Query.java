package au.edu.rmit;

import java.io.File;
import java.util.ArrayList;

import au.edu.rmit.indexing.Posting;
import au.edu.rmit.misc.SimilarityFunction;
import au.edu.rmit.parsing.DocIdHandler;
import au.edu.rmit.parsing.InvalidDocumentIdException;
import au.edu.rmit.parsing.SimpleParser;
import au.edu.rmit.querying.QueryEngine;
import au.edu.rmit.querying.SearchResult;
import au.edu.rmit.querying.SimpleQueryEngine;
import au.edu.rmit.stopping.DummyStopperModule;
import au.edu.rmit.stopping.SimpleStopperModule;
import au.edu.rmit.stopping.StopperModule;

public class Query {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
    	if(args.length < 9)
    	{
    		printErrorMessageWithUsage("Too few arguments");
    		System.exit(-1);
    	}
    	
    	//Files
    	File lexiconFile = null, invlistFile = null, mapFile = null;
    	boolean didTryToSetStopFile = false;
    	String stopFilePath = "";
    	
    	//Query
    	StringBuilder searchTerms = new StringBuilder();

    	//Misc
    	String queryLabel = "";
    	int numberOfResults = 0;
    	SimilarityFunction simFunc = SimilarityFunction.BM25;
    	boolean didSpecifyFiles = false;
    	
    	
    	// Process command line options
    	for (int i = 0; i < args.length; i++)
		{
    		String arg = args[i];
    		
    		if (arg.equals("-BM25")) {
    			simFunc = SimilarityFunction.BM25;
    		}
    		else if(arg.equals("-q"))
			{
				i++;
				if(i == args.length)
					break;
				else
					queryLabel = args[i];
			}
			else if(arg.equals("-n"))
			{
				i++;
				if(i == args.length)
					break;
				else
					try{
						numberOfResults = Integer.parseInt(args[i]);
					}
					catch(NumberFormatException e){
						printErrorMessageWithUsage("Number of results must be a natural number");
					}
			}
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
			{
				if(didSpecifyFiles)
				{					
					// Turn search terms from command line into a single string for parsing.
			        // This ensures the same as rules are applied as indexing.
			    	String delimiter = "";
			    	
			    	for(int j = 0; i + j < args.length; j++)
			    	{
			    	    searchTerms.append(delimiter);
			    		searchTerms.append(args[i+j]);
			    		delimiter = " ";
			    	}
				}
				else
				{
					if(args.length - 3 > i)
					{
						// Read in filenames from command line and make sure they exist
				    	lexiconFile = new File(args[i]);
				    	validateFile(lexiconFile, "lexicon");

				    	i++;
				    	invlistFile = new File(args[i]);
				    	validateFile(invlistFile, "inverted list");

				    	i++;
				    	mapFile = new File(args[i]);
				    	validateFile(mapFile, "map");
				    	
				    	didSpecifyFiles = true;
					}
					else
					{
						printErrorMessageWithUsage("Not enough files given as input. Please input a lexicon, inverted list and a map");
			    		System.exit(-1);
					}
				}
				
			}
				
		}
    	
    	// Read in stop list file name, make sure it exists.
    	StopperModule stopperModule = new DummyStopperModule();
    	
    	if(didTryToSetStopFile)
    	{
    		File stopList = new File(stopFilePath);
        	
        	if(!stopList.exists())
        	{
        		printErrorMessageWithUsage("Please specify a valid stoplist path. " + 
        										stopFilePath + " is invalid.");
        		System.exit(-1);
        	}
        	else
        	{
        		stopperModule = new SimpleStopperModule(stopList);
        	}
    	}
        

    	SimpleParser parser = new SimpleParser(stopperModule, null, null, false);
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
    	System.err.println("USAGE: search -BM25 -q <query-label> -n <num-results> <lexicon> <invlists>" +
    							"<map> [-s <stoplist>] <queryterm-1> [<queryterm-2> ...  <queryterm-N>]");
    }

}
