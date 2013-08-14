package au.edu.rmit.indexing;

import java.io.IOException;
import java.util.HashMap;

public interface IndexerModule
{
    public void indexWord(String word, int documentId);
    public void addDocument(int documentId, HashMap<String, Integer> termList);
    public void writeIndex() throws IOException;
}
