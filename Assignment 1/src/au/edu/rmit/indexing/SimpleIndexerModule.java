package au.edu.rmit.indexing;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class SimpleIndexerModule implements IndexerModule
{
    private static final String LEXICON_DELIM = "|";

    File lexicon;
    File invlist;
    HashMap<String, PostingsList> index = new HashMap<String, PostingsList>();

    public SimpleIndexerModule(File lexicon, File invlist)
    {
        this.lexicon = lexicon;
        this.invlist = invlist;
    }

    public void indexWord(String term, int documentId)
    {
        if (!(index.containsKey(term)))
            index.put(term, new PostingsList());

        index.get(term).updatePosting(documentId);

        //System.out.println(documentId + ": " + term);
    }

    public void addDocument(int documentId, HashMap<String, Integer> termList)
    {
        for (String term : termList.keySet())
        {
            int frequency = termList.get(term).intValue();

            // Add new postings list if term not yet in index.
            if (!(index.containsKey(term)))
                index.put(term, new PostingsList());

            index.get(term).addPosting(documentId, frequency);

        }
        //System.out.println(documentId);
    }

    public void writeIndex() throws IOException
    {
        PrintWriter lexiconWriter = new PrintWriter(lexicon);

        // Use data output stream for invlist so we can write integers.
        FileOutputStream invlistFOS = new FileOutputStream(invlist);
        BufferedOutputStream invlistBOS = new BufferedOutputStream(invlistFOS);
        DataOutputStream invlistDOS = new DataOutputStream(invlistBOS);

        for (String term : index.keySet())
        {
            long bytePos = invlistDOS.size();
            lexiconWriter.println(term + LEXICON_DELIM
                    + index.get(term).getFrequency() + LEXICON_DELIM
                    + bytePos);

            for (Posting posting : index.get(term).getPostings())
            {
                invlistDOS.writeInt(posting.getDocumentId());
                invlistDOS.writeInt(posting.getFrequency());
            }
        }

        lexiconWriter.close();
        invlistDOS.close();
    }
}
