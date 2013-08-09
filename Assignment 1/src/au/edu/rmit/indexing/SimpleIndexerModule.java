package au.edu.rmit.indexing;

public class SimpleIndexerModule implements IndexerModule
{

	public void indexWord(String word, String documentId)
	{
		System.out.println(word);
	}
}
