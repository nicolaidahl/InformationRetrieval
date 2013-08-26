package au.edu.rmit.indexing;

public class Posting
{
    private int documentId;
    //The raw document id is not guaranteed to be set
    private String rawDoucmentId;
    

	private int frequency;

    public Posting(int documentId, String rawDocumentId)
    {
        this(documentId, rawDocumentId, 1);
    }

    public Posting(int documentId, String rawDocumentId, int frequency)
    {
        this.documentId = documentId;
        this.frequency = frequency;
        this.rawDoucmentId = rawDocumentId;
    }

    public int getDocumentId()
    {
        return documentId;
    }

    public String getRawDoucmentId()
	{
		return rawDoucmentId == null ? "" : rawDoucmentId;
	}

	public void setRawDoucmentId(String rawDoucmentId)
	{
		this.rawDoucmentId = rawDoucmentId;
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
