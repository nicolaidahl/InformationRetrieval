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

    public DocIdHandler()
    {
        docIdMap = new ArrayList<String>();
    }

    // If instantiated with a file name load index in straight away
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

    public void writeMap(File mapFile) throws FileNotFoundException
    {
        PrintWriter mapWriter = new PrintWriter(mapFile);

        for (int i = 0; i < docIdMap.size(); i++)
        {
            mapWriter.println(docIdMap.get(i));
        }

        mapWriter.close();
    }

    public void readMap(File mapFile)
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
