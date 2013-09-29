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
    
    int numResults;

    public BM25RankedQueryEngine(File lexicon, File invlist, File mapFile, int numResults)
    {
        super(lexicon, invlist, mapFile);
        this.numResults = numResults;
    }

    public static double getDocumentWeight(int documentLength, double averageDocumentLength)
    {
        // k1 * ((1 - b) + ((b * Ld) / AL))
        return BM25RankedQueryEngine.k1 *
               (
                 (1.0 - BM25RankedQueryEngine.b)
                 +
                 (
                   (BM25RankedQueryEngine.b * Double.valueOf(documentLength))
                   /
                   averageDocumentLength
                 )
               );

    }
    
    public QueryResult[] getResults(String[] queryTerms)
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

                double bm25Rank = rsjWeight * (((k1 + 1) * docTermFreq) / (docWeight + docTermFreq));

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
        
        for (Map.Entry<Integer, Double> finalScore : accumulatorHash.entrySet())
        {
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
