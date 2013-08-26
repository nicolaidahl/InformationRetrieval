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

    public void incrementFrequency()
    {
        frequency++;
    }

    @Override
    public int hashCode()
    {
        return new Integer(this.documentId).hashCode();
    }

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
