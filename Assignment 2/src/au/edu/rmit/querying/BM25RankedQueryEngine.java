package au.edu.rmit.querying;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import au.edu.rmit.indexing.Posting;
import au.edu.rmit.misc.MinHeap;
import au.edu.rmit.parsing.InvalidDocumentIdException;

public class BM25RankedQueryEngine extends QueryEngine {
    // Okapi BM25 constants
    private static final double k1 = 1.2;
    private static final double b = 0.75;
    

    public BM25RankedQueryEngine(File lexicon, File invlist, File mapFile)
    {
        super(lexicon, invlist, mapFile);
    }

    /**
     * Calculate the BM25 document weight for a given document given its length and the average document length of the collection
     * k1 * ((1 - b) + ((b * Ld) / AL))
     * where:
     * Ld is the length of the document
     * AL is the average document length for the collection
     * k1 & b are predefined constants
     * @param documentLength length of the document
     * @param averageDocumentLength average document length of the collection
     * @return the BM25 document weight
     */
    public static double getDocumentWeight(int documentLength, double averageDocumentLength)
    {
        return k1 * ((1.0 - b) + ((b * Double.valueOf(documentLength)) / averageDocumentLength));
    }
    
    /**
     * Run a query against the inverted index
     * @param queryTerms a list of terms to query
     * @param numResults the number of results to return
     * @return a list of QueryResults containing the document ID and ranking score for the top numResults documents
     */
    public QueryResult[] getResults(String[] queryTerms, int numResults)
    {
        HashMap<Integer, Double> accumulatorHash = new HashMap<Integer, Double>();
        MinHeap minHeap = new MinHeap(numResults);
        int numDocuments = docIdHandler.getNumberOfDocuments();

        for (String term : queryTerms)
        {
            SearchResult result = super.getSearchResult(term);

            // Get number of documents containing term
            int termFreq = result.getFrequency();

            for (Posting posting : result.getPostings())
            {
                int docId = posting.getDocumentId();
                double docWeight = 0.0;
                try {
                    // Retrieve document weight from document map
                    docWeight = docIdHandler.getBM25DocumentWeight(docId);
                } catch (InvalidDocumentIdException e) {
                    System.err.println("FATAL ERROR: Invalid document ID " +
                                       docId + " found in search results!\n" +
                                       "The index is corrupt. Please reindex before querying again.");
                    System.exit(-1);
                }

                // Robertson-Sparck Jones weight
                double rsjWeight = Math.log((numDocuments - termFreq + 0.5) / (termFreq + 0.5));

                int docTermFreq = posting.getFrequency();

                // Calculate BM25 similarity score
                double bm25Rank = rsjWeight * (((k1 + 1) * docTermFreq) / (docWeight + docTermFreq));

                // If document is already in accumulator add similarity score value
                // Otherwise add new accumulator initialised with similarity score
                if (accumulatorHash.containsKey(docId))
                {
                    accumulatorHash.put(docId, accumulatorHash.get(docId) + bm25Rank);
                }
                else
                {
                    accumulatorHash.put(docId, bm25Rank);
                }
            }
        }
        
        // Get the top numResults accumulators using a MinHeap
        for (Map.Entry<Integer, Double> finalScore : accumulatorHash.entrySet())
        {
            // Only add to MinHeap if it is greater than the lowest MinHeap value
            if (finalScore.getValue() > minHeap.getLowestScore())
            {
                try {
                    minHeap.addItem(new QueryResult(docIdHandler.getRawDocumentId(finalScore.getKey()), finalScore.getValue()));
                } catch (InvalidDocumentIdException e) {
                    // No need to catch, taken care of when getting weight
                }
            }
        }
        
        return minHeap.getSortedHeap();
    }
}
