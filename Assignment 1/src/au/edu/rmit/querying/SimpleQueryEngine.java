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

public class SimpleQueryEngine implements QueryEngine
{
    private static final String LEXICON_DELIM = "\\|";

    File lexiconFile;
    File invlistFile;
    HashMap<String, LexiconTerm> lexiconList;
    
    public SimpleQueryEngine(File lexicon, File invlist)
    {
        this.lexiconFile = lexicon;
        this.invlistFile = invlist;
        lexiconList = new HashMap<String, LexiconTerm>();
        readIndex();
    }

    public SearchResult getSearchResult(String term)
    {
        if (!lexiconList.containsKey(term))
            return new SearchResult(term, new ArrayList<Posting>(), 0);

        int documentFreq = lexiconList.get(term).documentFreq;
        int filePosition = (int) lexiconList.get(term).filePosition;
        int invlistLength = (int) lexiconList.get(term).invlistLength;
        PostingsList termPosting = new PostingsList();
        
        SeekableByteChannel sbc;
		try
		{
			sbc = Files.newByteChannel(invlistFile.toPath());
			
			/* OLD
			 *  Calculate number of bytes to read
             *  Get number of bytes per integer using integer size in bits divided by 8
             *  Multiply by 2 since each posting is made up of two integers
             *  Multiply by term document frequency to get size of full postings list
             *  
            ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE / 8 * 2 * documentFreq);

            sbc.position(filePosition);
            sbc.read(buf);
         
            buf.rewind();
            while (buf.remaining() >= 4)
            {   
                termPosting.addPosting(buf.getInt(), buf.getInt());
            }*/
			
			// Number of bytes to read read in from lexicon
			ByteBuffer buf = ByteBuffer.allocate(invlistLength);

	        sbc.position(filePosition);
	        sbc.read(buf);
	        
	        buf.rewind();

	        Byte[] byteArray = new Byte[invlistLength];
	        while (buf.hasRemaining())
	        {
	            byteArray[buf.position()] = buf.get();
	        }
	        sbc.close();
	        
	        Integer[] valueArray = VariableByteEncoding.decode(byteArray);
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
			System.err.println("Unable to retrieve search results for term " + term);
			e.printStackTrace();
		}
		catch (Exception e)
		{
			System.err.println("Something went wrong retrieving the search results for " + term);
			e.printStackTrace();
		}

        return new SearchResult(term, termPosting.getPostingsAsArrayList(), documentFreq);
    }
    
    private void readIndex() 
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
		} catch (Exception e)
		{
			System.err.println("Unable to load lexicon file");
			e.printStackTrace();
			System.exit(-1);
		} 

        
    }

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

}