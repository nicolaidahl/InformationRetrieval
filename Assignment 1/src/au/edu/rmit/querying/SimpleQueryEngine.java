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

import au.edu.rmit.indexing.PostingsList;

public class SimpleQueryEngine implements QueryEngine
{
    private static final String LEXICON_DELIM = "|";

    File lexiconFile;
    File invlistFile;
    File mapFile;
    HashMap<String, LexiconTerm> lexiconList;
    String[] mapArray;
    
    public SimpleQueryEngine(File lexicon, File invlist, File map) throws IOException
    {
        this.lexiconFile = lexicon;
        this.invlistFile = invlist;
        lexiconList = new HashMap<String, LexiconTerm>();
        readIndex();

        this.mapFile = map;
        mapArray = readMap();
    }
    
    public PostingsList getPostings(String term) throws IOException
    {
        if (!lexiconList.containsKey(term))
            return null;

        int documentFreq = lexiconList.get(term).documentFreq;
        int filePosition = (int) lexiconList.get(term).filePosition;
        PostingsList termPosting = new PostingsList();
        
        SeekableByteChannel sbc = Files.newByteChannel(invlistFile.toPath());
        ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE / 8 * 2 * documentFreq);
        buf.position(filePosition);

        sbc.read(buf);
        
        buf.rewind();
        
        while (buf.remaining() >= 4)
        {
            termPosting.addPosting(buf.getInt(), buf.getInt());
        }
        sbc.close();

        return termPosting;
    }
    
    private void readIndex() throws IOException
    {
        BufferedReader br;
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
    }
    
    private String[] readMap() throws IOException
    {
        ArrayList<String> mapArrayList = new ArrayList<String>();

        BufferedReader br;
        br = new BufferedReader(new FileReader(mapFile));

        String line;

    	while ((line = br.readLine()) != null) {
    	    mapArrayList.add(line);
    	}

        br.close();        

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