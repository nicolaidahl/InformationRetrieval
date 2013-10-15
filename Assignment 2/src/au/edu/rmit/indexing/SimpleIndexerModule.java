package au.edu.rmit.indexing;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import au.edu.rmit.misc.VariableByteEncoding;

public class SimpleIndexerModule implements IndexerModule
{
    private static final String LEXICON_DELIM = " ";

    HashMap<String, PostingsList> index;

    public SimpleIndexerModule()
    {
        this.index = new HashMap<String, PostingsList>();
    }

    /**
     * Index an individual word.
     * Avoid adding word by word if possible, addDocument is _much_ faster
     * @param term Which term to index
     * @param documentId The documentId of the document in which the term occurs
     */
    public void indexWord(String term, int documentId)
    {
        if (!(index.containsKey(term)))
            index.put(term, new PostingsList());

        index.get(term).updatePosting(documentId);
    }

    /**
     * Add a whole document's term frequencies to the index
     * @param documentId The documentId of the document to add to the index
     * @param termList A hash map of terms and their within-document frequencies to add to the index
     */
    public void addDocument(int documentId, HashMap<String, Integer> termList)
    {
        for (String term : termList.keySet())
        {
            int frequency = termList.get(term).intValue();

            // Add new postings list if term not yet in index.
            if (!(index.containsKey(term)))
                index.put(term, new PostingsList());

            // Add document ID and frequency to postings list for term
            index.get(term).addPosting(documentId, frequency);
        }
    }

    /**
     * Write the index out to disk
     * @param lexicon The file to write the lexicon to
     * @param invlist The file to write the inverted list to
     * @throws IOException If writing to either file fails
     */
    public void writeIndex(File lexicon, File invlist) throws IOException
    {
        PrintWriter lexiconWriter = new PrintWriter(lexicon);

        // Use data output stream for invlist so we can write integers.
        FileOutputStream invlistFOS = new FileOutputStream(invlist);
        BufferedOutputStream invlistBOS = new BufferedOutputStream(invlistFOS);
        DataOutputStream invlistDOS = new DataOutputStream(invlistBOS);

        long bytePos;
        long byteLen;
        
        for (String term : index.keySet())
        {
            bytePos = invlistDOS.size();

            int prevDocId = 0;

            for (Posting posting : index.get(term).getPostings())
            {
                // Write variable byte encoded document ID to inverted list file
                // Write gap between previous doc ID and this one to save space
                for (Byte value : VariableByteEncoding.encode(posting.getDocumentId() - prevDocId))
                    invlistDOS.write(value.byteValue());

                // Write variable byte encoded frequency to inverted list file
                for (Byte value : VariableByteEncoding.encode(posting.getFrequency()))
                    invlistDOS.write(value.byteValue());

                prevDocId = posting.getDocumentId();
            }

            // Write delimited term details to lexicon:
            //   term
            //   frequency (number of docs containing term)
            //   inverted list address (byte offset of postings list in inverted list file)
            //   inverted list size (number of bytes occupied by inverted list - for input buffering)
            byteLen = invlistDOS.size() - bytePos;
            lexiconWriter.println(term + LEXICON_DELIM
                    + index.get(term).getFrequency() + LEXICON_DELIM
                    + bytePos + LEXICON_DELIM
                    + byteLen);
        }

        lexiconWriter.close();
        invlistDOS.close();
    }
}
