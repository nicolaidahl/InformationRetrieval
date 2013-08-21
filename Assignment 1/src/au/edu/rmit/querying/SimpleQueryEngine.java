package au.edu.rmit.querying;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import au.edu.rmit.indexing.Posting;
import au.edu.rmit.indexing.PostingsList;

public class SimpleQueryEngine implements QueryEngine
{
    private static final String LEXICON_DELIM = "\\|";

    File lexiconFile;
    File invlistFile;
    File mapFile;
    HashMap<String, LexiconTerm> lexiconList;
    String[] mapArray;
    
    public SimpleQueryEngine(File lexicon, File invlist, File map)
    {
        this.lexiconFile = lexicon;
        this.invlistFile = invlist;
        lexiconList = new HashMap<String, LexiconTerm>();
        readIndex();

        this.mapFile = map;
        mapArray = readMap();
    }
    
    public SearchResult getSearchResult(String term)
    {
        if (!lexiconList.containsKey(term))
            return new SearchResult(term, new ArrayList<Posting>(), 0);

        int documentFreq = lexiconList.get(term).documentFreq;
        int filePosition = (int) lexiconList.get(term).filePosition;
        PostingsList termPosting = new PostingsList();
        
        SeekableByteChannel sbc;
		try
		{
			sbc = Files.newByteChannel(invlistFile.toPath());
			
			ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE / 8 * 2 * documentFreq);

	        sbc.position(filePosition);
	        sbc.read(buf);
	        
	        buf.rewind();
	        while (buf.remaining() >= 4)
	        {
	            termPosting.addPosting(buf.getInt(), buf.getInt());
	        }
	        sbc.close();
		}
		catch (IOException e)
		{
			System.err.println("Unable to retrieve search results for term " + term);
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
	    	                            Long.parseLong(tokens[2])));
	    	}

	        br.close();
		} catch (Exception e)
		{
			System.err.println("Unable to load lexicon file");
			e.printStackTrace();
			System.exit(-1);
		} 

        
    }
    
    private String[] readMap()
    {
        ArrayList<String> mapArrayList = new ArrayList<String>();

        BufferedReader br;
        try
		{
			br = new BufferedReader(new FileReader(mapFile));
			
			String line;

	    	while ((line = br.readLine()) != null) {
	    	    mapArrayList.add(line);
	    	}

	        br.close(); 
	        
	        
		} catch (FileNotFoundException e)
		{
			System.err.println("Unable to load map file");
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e)
		{
			System.err.println("Unable to load map file");
			e.printStackTrace();
			System.exit(-1);
		}

               
        return mapArrayList.toArray(new String[0]);
        
    }
    
    private static class LexiconTerm
    {
        public int documentFreq;
        public long filePosition;
        
        public LexiconTerm(int documentFreq, long filePosition)
        {
            this.documentFreq = documentFreq;
            this.filePosition = filePosition;
        }
    }

}