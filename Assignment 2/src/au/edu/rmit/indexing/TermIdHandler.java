package au.edu.rmit.indexing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

public class TermIdHandler {
    private ArrayList<String> termIdMap;
    private HashMap<String, Integer> termIdHash;

    /**
     * Create new TermIdHandler with empty term Id map
     */
    public TermIdHandler()
    {
        termIdMap = new ArrayList<String>();
        termIdHash = new HashMap<String, Integer>();
    }

    /**
     * Create new TermIdHandler with pre-existing term Id map read in from file.
     */
    public TermIdHandler(File mapFile)
    {
        this();
        this.readMap(mapFile);
    }

    public int getTermId(String term)
    {
        if (termIdHash.containsKey(term))
        {
            return termIdHash.get(term);
        }
        else
        {
            termIdMap.add(term);

            int termId = (termIdMap.size() - 1);
            termIdHash.put(term, termId);

            return termId;
        }
    }
    
    /**
     * Get the term for the given term ID
     * @param termId The internal numeric term Id to retrieve
     * @return The term as read in from the document collection
     * @throws InvalidTermIdException If term Id not found
     */
    public String getTerm(int termId)
            throws InvalidTermIdException
    {
        String term;
        try
        {
            term = termIdMap.get(termId);
        }
        catch(IndexOutOfBoundsException e)
        {
            throw new InvalidTermIdException(
                    "Term Id" + termId + " does not exist");
        }

        return term;
    }
    

    public int getNumberOfTerms()
    {
        return termIdMap.size();
    }

    /**
     * Write the term Id map to disk
     * @param mapFile Output file
     * @throws FileNotFoundException If file not found
     */
    public void writeMap(File mapFile) throws FileNotFoundException
    {
        PrintWriter mapWriter = new PrintWriter(mapFile);

        // Write term ID to map file
        for (int i = 0; i < termIdMap.size(); i++)
        {
            mapWriter.println(termIdMap.get(i));
        }

        mapWriter.close();
    }

    /**
     * Read in a term Id map to a newly created TermIdHandler.
     * This is only used when instantiating a new TermIdHandler.
     * @param mapFile Input map file
     */
    private void readMap(File mapFile)
    {
        try (InputStream in = new FileInputStream(mapFile);
             Reader reader = new InputStreamReader(in, Charset.defaultCharset());
             BufferedReader mapReader = new BufferedReader(reader))
        {
            String term;

            // Map file writes out termIds sequentially, so line 1 = 0, line 2 = 1 etc.
            while ((term = mapReader.readLine()) != null)
            {
                termIdMap.add(term);
                termIdHash.put(term, (termIdMap.size() - 1));
            }  
        }
        catch (IOException e)
        {
            System.err.println("Error while reading map file " + mapFile);
            e.printStackTrace();
        }
    }

}
