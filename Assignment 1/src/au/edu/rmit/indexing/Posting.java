package au.edu.rmit.indexing;

public class Posting
{
    private int documentId;
	private int frequency;

    public Posting(int documentId)
    {
        this(documentId, 1);
    }

    public Posting(int documentId, int frequency)
    {
        this.documentId = documentId;
        this.frequency = frequency;
    }

    public int getDocumentId()
    {
        return documentId;
    }

    public int getFrequency()
    {
        return frequency;
    }

    public void addFrequency(int frequency)
    {
        this.frequency += frequency;
    }

    public void incrementFrequency()
    {
        frequency++;
    }

    // hashCode for Posting should be based on documentId
    @Override
    public int hashCode()
    {
        return new Integer(this.documentId).hashCode();
    }

    // two Posting objects are considered equal if they have the same documentId
    @Override
    public boolean equals(Object o)
    {
        if (o == null)
            return false;

        if (!(o instanceof Posting))
            return false;

        Posting p = (Posting) o;
        return this.documentId == p.documentId;
    }

}
