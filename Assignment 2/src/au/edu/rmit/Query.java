package au.edu.rmit;

import java.io.File;
import java.util.ArrayList;

import au.edu.rmit.misc.SimilarityFunction;
import au.edu.rmit.misc.Timer;
import au.edu.rmit.parsing.SimpleParser;
import au.edu.rmit.querying.BM25RankedQueryEngine;
import au.edu.rmit.querying.QueryEngine;
import au.edu.rmit.querying.QueryExpansionBM25QueryEngine;
import au.edu.rmit.querying.QueryResult;
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
    	int numResults = 0;
    	SimilarityFunction simFunc = SimilarityFunction.NORANK;
    	boolean didSpecifyFiles = false;
    	
    	//Files for Query Expansion
        File termMap = null, termLexicon = null, termIndex = null;
    	
    	// Process command line options
    	for (int i = 0; i < args.length; i++)
		{
    		String arg = args[i];
    		
    		if (arg.equals("-BM25"))
    		{
    			simFunc = SimilarityFunction.BM25;
    		}
    		else if (arg.equals("-QEBM25"))
    		{
    			simFunc = SimilarityFunction.QEBM25;

    			if(args.length - 3 > i)
    			{
    			    // Read in QE filenames from command line and make sure they exist
    			    termLexicon = new File(args[i]);
    			    validateFile(termLexicon, "QE lexicon");

    			    i++;
    			    termIndex = new File(args[i]);
    			    validateFile(termIndex, "QE index");

    			    i++;
    			    termMap = new File(args[i]);
    			    validateFile(termMap, "QE map");
    			}
    			else
    			{
    			    printErrorMessageWithUsage("Not enough files given as input. Please input a lexicon, inverted list and a map");
    			    System.exit(-1);
    			}
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
						numResults = Integer.parseInt(args[i]);
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

			    	for( ; i < args.length; i++)
			    	{
			    	    searchTerms.append(delimiter);
			    		searchTerms.append(args[i]);
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
    	
    	QueryEngine engine;
        // Initialise query engine and document handler
    	if (simFunc == SimilarityFunction.BM25)
    	{
            engine = new BM25RankedQueryEngine(lexiconFile, invlistFile, mapFile, numResults);
            
            Timer timer = new Timer();
            
            QueryResult[] results = engine.getResults(parsedTerms.toArray(new String[0]));
                
            int rank = 1;
            //for (QueryResult result : results)
            for (int i = results.length - 1; i >= 0; i--)
            {
                System.out.println(String.format("%s %s %d %.3f",
                        queryLabel,
                        results[i].getRawDocId(),
                        rank,
                        results[i].getScore()));
                rank++;
            }

            timer.stamp("Running time");
            System.out.println(timer.getTimings());
    	}
    	else if (simFunc == SimilarityFunction.QEBM25) {
            engine = new QueryExpansionBM25QueryEngine(lexiconFile, invlistFile, mapFile,
                    termMap, termLexicon, termIndex,
                    numResults);
    	}
    	else
    	{
            engine = new SimpleQueryEngine(lexiconFile, invlistFile, mapFile);

        	for (String term : parsedTerms)
        	{
        	    System.out.println(term);

        	    String[] termArray = new String[1];
        	    termArray[0] = term;

                QueryResult[] results = engine.getResults(termArray);
                
                for (QueryResult result : results)
                {
                    System.out.println(String.format("%s %s", queryLabel, result.getRawDocId()));
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
