package au.edu.rmit.querying;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import au.edu.rmit.indexing.Posting;
import au.edu.rmit.indexing.PostingsList;
import au.edu.rmit.misc.VariableByteEncoding;
import au.edu.rmit.parsing.DocIdHandler;

public abstract class QueryEngine {
	
    private static final String LEXICON_DELIM = " ";

    File lexiconFile;
    File invlistFile;
    File mapFile;
    HashMap<String, LexiconTerm> lexiconList;
    DocIdHandler docIdHandler;
    
    public QueryEngine(File lexicon, File invlist, File mapFile)
    {
        this.lexiconFile = lexicon;
        this.invlistFile = invlist;
        this.mapFile = mapFile;
        lexiconList = new HashMap<String, LexiconTerm>();

        // Read index when query engine is created
        readLexicon();
        
        docIdHandler = new DocIdHandler(mapFile);
    }

    public abstract QueryResult[] getResults(String queryTerms[]);
    
    /**
     * Search for a term in the inverted index
     * @param term The string to search for
     * @return a SearchResult containing the result of the search for further processing
     */
    public SearchResult getSearchResult(String term)
    {
        if (!lexiconList.containsKey(term))
            return new SearchResult(term, new ArrayList<Posting>(), 0);

		// Number of documents containing term.
        int documentFreq = lexiconList.get(term).documentFreq;
		// Byte offset of postings list from lexicon.
        int filePosition = (int) lexiconList.get(term).filePosition;
		// Byte size of postings list from lexicon.
        int invlistLength = (int) lexiconList.get(term).invlistLength;

        PostingsList termPosting = new PostingsList();
        
        SeekableByteChannel sbc;
		try
		{
			sbc = Files.newByteChannel(invlistFile.toPath());
			
            // Create byte buffer of the same size as the postings list.
			ByteBuffer buf = ByteBuffer.allocate(invlistLength);

	        sbc.position(filePosition);
	        sbc.read(buf);
	        
	        buf.rewind();

            // Read bytes from buffer into Byte array.
	        Byte[] byteArray = new Byte[invlistLength];
	        while (buf.hasRemaining())
	        {
	            byteArray[buf.position()] = buf.get();
	        }
	        sbc.close();
	        
            // Decode variable byte encoded postings list into integer array.
	        Integer[] valueArray = VariableByteEncoding.decode(byteArray);

            // Read postings list from integer array.
	        int prevDocId = 0;
	        for (int i = 0; i < (documentFreq * 2); i += 2)
	        {
	            int curDocId = prevDocId + valueArray[i].intValue();
	            termPosting.addPosting(curDocId, valueArray[i+1].intValue());
	            prevDocId = curDocId;
	        }
		}
		catch (IOException e)
		{
			System.err.println("Error reading file while retrieving search results for term " + term);
			e.printStackTrace();
		}
		catch (Exception e)
		{
			System.err.println("Something went wrong retrieving the search results for " + term);
			e.printStackTrace();
		}

        return new SearchResult(term, termPosting.getPostingsAsArrayList(), documentFreq);
    }

    /**
     * Read the inverted index lexicon from disk.
     * Uses the lexiconFile stored in the object by the constructor.
     */
    private void readLexicon() 
    {
        BufferedReader br;

        try
		{
        	
			br = new BufferedReader(new FileReader(lexiconFile));
			
			String line;
	        String[] tokens;

	    	while ((line = br.readLine()) != null) {
	    	    tokens = line.split(LEXICON_DELIM);

	    	    lexiconList.put(tokens[0],
	    	            new LexiconTerm(Integer.parseInt(tokens[1]),
	    	                            Long.parseLong(tokens[2]),
	    	                            Long.parseLong(tokens[3])));
	    	}

	        br.close();
		}
        catch (Exception e)
		{
			System.err.println("Unable to load lexicon file");
			e.printStackTrace();
			System.exit(-1);
		} 
        
    }

    /**
     * Class to hold term information read in from lexicon.
     * Only used internally.
     */
    private static class LexiconTerm
    {
        public int documentFreq;
        public long filePosition;
        public long invlistLength;
        
        public LexiconTerm(int documentFreq, long filePosition, long invlistLength)
        {
            this.documentFreq = documentFreq;
            this.filePosition = filePosition;
            this.invlistLength = invlistLength;
        }
    }
    
    protected int documentFrequencyForTerm(String term)
    {
    	LexiconTerm obj = lexiconList.get(term);
    	if(obj != null)
    		return obj.documentFreq;
    	else
    		return 0;
    }
}
