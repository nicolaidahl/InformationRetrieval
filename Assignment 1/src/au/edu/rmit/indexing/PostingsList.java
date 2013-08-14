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

        // Make sure the new documentId is greater than the last.
        // The postings list should be in order, this is just in case.
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

}
