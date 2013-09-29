package au.edu.rmit.querying;

import java.io.File;
import java.util.ArrayList;

import au.edu.rmit.indexing.Posting;
import au.edu.rmit.parsing.InvalidDocumentIdException;

public class SimpleQueryEngine extends QueryEngine
{
    public SimpleQueryEngine(File lexicon, File invlist, File mapFile)
    {
        super(lexicon, invlist, mapFile);
    }

    public QueryResult[] getResults(String[] queryTerms) {
        ArrayList<QueryResult> queryResults = new ArrayList<QueryResult>();

    	for (String term : queryTerms)
    	{
            // Retrieve search result from query engine and print results.
    		SearchResult result = super.getSearchResult(term);
    		
            // Get raw document ID for each posting from doc ID handler and print to stdout with frequency
    		for (Posting posting : result.getPostings())
			{
				try {
				    // Add query results - the score is 1 for all simple queries since no ranking is done
                    queryResults.add(new QueryResult(docIdHandler.getRawDocumentId(posting.getDocumentId()), 1.0));
                } catch (InvalidDocumentIdException e) {
                    System.err.println("FATAL ERROR: Invalid document ID " +
                                       posting.getDocumentId() + " found in search results!\n" +
                                       "The index is corrupt. Please reindex before querying again.");
                    System.exit(-1);
                }
			}
    	}

        return queryResults.toArray(new QueryResult[0]);
    }

}
