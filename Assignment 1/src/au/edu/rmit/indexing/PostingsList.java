package au.edu.rmit.indexing;

import java.util.ArrayList;

public class PostingsList {
    private ArrayList<Posting> postingsList;

    public PostingsList()
    {
        postingsList = new ArrayList<Posting>();
    }

    public void addPosting(int documentId, int inDocumentFreq)
    {
        Posting newPosting = new Posting(documentId, inDocumentFreq);

        /* Check if the new documentId is greater than the last in the postingsList.
         * If so, add to the end.
         * If not, insert in natural sort order
         *
         * This guarantees the list will be sorted but still have constant-time insertion
         *   when adding documents sequentially.
         */
        if (!(postingsList.isEmpty())
                && postingsList.get(postingsList.size() - 1).getDocumentId() > documentId)
        {
            for (int i = 0; i < postingsList.size(); i++)
            {
                if (postingsList.get(i).getDocumentId() > documentId)
                    postingsList.add(i, newPosting);
            }
        }
        else
        {
            postingsList.add(newPosting);
        }
    }

    // Update an individual posting
    public void updatePosting(int documentId, int inDocumentFreq)
    {
        Posting newPosting = new Posting(documentId, inDocumentFreq);

        int postingIndex = postingsList.indexOf(newPosting);
        if (postingIndex == -1)
        {
            addPosting(documentId, inDocumentFreq);
        }
        else
        {
            postingsList.get(postingIndex).incrementFrequency();
        }
    }

    public void updatePosting(int documentID)
    {
        updatePosting(documentID, 1);
    }

    public int getFrequency()
    {
        return postingsList.size();
    }

    public Posting[] getPostings()
    {
        return postingsList.toArray(new Posting[0]);
    }
    
    public ArrayList<Posting> getPostingsAsArrayList()
    {
    	return postingsList;
    }

}
