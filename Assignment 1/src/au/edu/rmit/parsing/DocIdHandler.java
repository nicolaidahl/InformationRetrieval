package au.edu.rmit.parsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class DocIdHandler {
    ArrayList<String> docIdMap;

    public DocIdHandler()
    {
        docIdMap = new ArrayList<String>();
    }

    public int getDocumentId(String rawDocumentId)
    {
        docIdMap.add(rawDocumentId);
        return (docIdMap.size() - 1);
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
}
