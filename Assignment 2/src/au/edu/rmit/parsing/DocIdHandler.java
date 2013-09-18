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

public class DocIdHandler {
    ArrayList<String> docIdMap;

    /**
     * Create new DocIdHandler with empty document Id map
     */
    public DocIdHandler()
    {
        docIdMap = new ArrayList<String>();
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
        docIdMap.add(rawDocumentId);
        return (docIdMap.size() - 1);
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
     * Write the document Id map to disk
     * @param mapFile Output file
     * @throws FileNotFoundException If file not found
     */
    public void writeMap(File mapFile) throws FileNotFoundException
    {
        PrintWriter mapWriter = new PrintWriter(mapFile);

        for (int i = 0; i < docIdMap.size(); i++)
        {
            mapWriter.println(docIdMap.get(i));
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
            String rawDocId;

            // Map file writes out docIds sequentially, so line 1 = 0, line 2 = 1 etc.
            while ((rawDocId = mapReader.readLine()) != null)
            {
                // Make sure it wasn't a blank index
                if (!rawDocId.isEmpty())
                    docIdMap.add(rawDocId);
            }          
        }
        catch (IOException e)
        {
            System.err.println("Error while reading map file " + mapFile);
            e.printStackTrace();
        }
    }
}
