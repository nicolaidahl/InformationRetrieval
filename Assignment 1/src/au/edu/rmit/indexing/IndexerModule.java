package au.edu.rmit.indexing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public interface IndexerModule
{
    public void indexWord(String word, int documentId);
    public void addDocument(int documentId, String rawDocumentId, HashMap<String, Integer> termList);
    public void writeIndex(File lexicon, File invlist) throws IOException;
}
