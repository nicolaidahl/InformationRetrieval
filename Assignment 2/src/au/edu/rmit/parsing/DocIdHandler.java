package au.edu.rmit.parsing;

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

import au.edu.rmit.querying.BM25RankedQueryEngine;

public class DocIdHandler {
    private ArrayList<String> docIdMap;
    private HashMap<String, Integer> docIdHash;
    private ArrayList<Integer> documentLengths;
    private ArrayList<Double> documentWeights;
    private double averageDocumentLength;

    /**
     * Create new DocIdHandler with empty document Id map
     */
    public DocIdHandler()
    {
        docIdMap = new ArrayList<String>();
        docIdHash = new HashMap<String, Integer>();
        documentLengths = new ArrayList<Integer>();
        documentWeights = new ArrayList<Double>();
    }

    /**
     * Create new DocIdHandler with pre-existing document Id map read in from file.
     */
    public DocIdHandler(File mapFile)
    {
        this();
        this.readMap(mapFile);
    }

    public int getDocumentId(String rawDocumentId)
    {
        if (docIdHash.containsKey(rawDocumentId))
        {
            return docIdHash.get(rawDocumentId);
        }
        else
        {
            docIdMap.add(rawDocumentId);
            documentLengths.add(0);

            int docId = (docIdMap.size() - 1);
            docIdHash.put(rawDocumentId, docId);
            
            return docId;
        }
    }
    
    public void setDocumentLength(int documentId, int docLength)
    {
    	documentLengths.set(documentId, docLength);
    }

    /**
     * Get the raw document ID for the given internal document ID
     * @param documentId The internal numeric document Id to retrieve
     * @return The plain text raw document Id as read in from the document collection
     * @throws InvalidDocumentIdException If internal document Id not found
     */
    public String getRawDocumentId(int documentId)
            throws InvalidDocumentIdException
    {
        String rawDocId;
        try
        {
            rawDocId = docIdMap.get(documentId);
        }
        catch(IndexOutOfBoundsException e)
        {
            throw new InvalidDocumentIdException(
                    "Document Id" + documentId + " does not exist");
        }

        return rawDocId;
    }
    

    /**
     * Get the Okapi BM25 document weight for the given internal document ID
     * @param documentId The internal numeric document Id to retrieve
     * @return The Okapi BM25 document weight as calculated while indexing
     * @throws InvalidDocumentIdException If internal document Id not found
     */
    public double getBM25DocumentWeight(int documentId)
            throws InvalidDocumentIdException
    {
        double docWeight;
        try
        {
            docWeight = documentWeights.get(documentId);
        }
        catch(IndexOutOfBoundsException e)
        {
            throw new InvalidDocumentIdException(
                    "Document Id" + documentId + " does not exist");
        }

        return docWeight;
    }   

    
    public int getNumberOfDocuments()
    {
        return docIdMap.size();
    }

    /**
     * Write the document Id map to disk
     * @param mapFile Output file
     * @throws FileNotFoundException If file not found
     */
    public void writeMap(File mapFile) throws FileNotFoundException
    {
        PrintWriter mapWriter = new PrintWriter(mapFile);

        // Calculate average document length
        Integer sumDocLength = 0;
        for (Integer docLength : documentLengths)
        {
            sumDocLength += docLength;
        }
        averageDocumentLength = sumDocLength.doubleValue() / documentLengths.size();
        
        // Write document ID and document weight to map file
        Double documentWeight = 0.0;
        for (int i = 0; i < docIdMap.size(); i++)
        {
            // calculate Okapi BM25 document weight
            documentWeight = BM25RankedQueryEngine.getDocumentWeight(documentLengths.get(i), averageDocumentLength);

            mapWriter.println(docIdMap.get(i) + " " + documentWeight);
        }

        mapWriter.close();
    }

    /**
     * Read in a document Id map to a newly created DocIdHandler.
     * This is only used when instantiating a new DocIdHandler.
     * @param mapFile Input map file
     */
    private void readMap(File mapFile)
    {
        try (InputStream in = new FileInputStream(mapFile);
             Reader reader = new InputStreamReader(in, Charset.defaultCharset());
             BufferedReader mapReader = new BufferedReader(reader))
        {
            String rawDocIdAndLength;

            // Map file writes out docIds sequentially, so line 1 = 0, line 2 = 1 etc.
            while ((rawDocIdAndLength = mapReader.readLine()) != null)
            {
            	String[] splitted = rawDocIdAndLength.split(" ");
            	String rawDocId = splitted[0];
            	double docWeight = Double.parseDouble(splitted[1]);
            	
                // Make sure it wasn't a blank index
                if (!rawDocId.isEmpty())
                {
                	docIdMap.add(rawDocId);
                	docIdHash.put(rawDocId, (docIdMap.size() - 1));
                	documentWeights.add(docWeight);
                }
            }  
        }
        catch (IOException e)
        {
            System.err.println("Error while reading map file " + mapFile);
            e.printStackTrace();
        }
    }

    /**
     * Gets the eagerly initialized average document length. The length will be 0
     * if it has not yet been computed.
     * @return
     */
	public double getAverageDocumentLength()
	{
		return averageDocumentLength;
	}
}
