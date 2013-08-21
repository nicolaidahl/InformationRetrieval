package au.edu.rmit.querying;

import java.util.ArrayList;

import au.edu.rmit.indexing.Posting;

public class SearchResult
{
	
	private String searchTerm;
	private ArrayList<Posting> postings;
	private int frequency;
	
	public SearchResult(String searchTerm, ArrayList<Posting> postings,
			int frequency)
	{
		super();
		this.searchTerm = searchTerm;
		this.postings = postings;
		this.frequency = frequency;
	}
	
	public String getSearchTerm()
	{
		return searchTerm;
	}
	public void setSearchTerm(String searchTerm)
	{
		this.searchTerm = searchTerm;
	}
	public ArrayList<Posting> getPostings()
	{
		return postings;
	}
	public void setPostings(ArrayList<Posting> postings)
	{
		this.postings = postings;
	}
	public int getFrequency()
	{
		return frequency;
	}
	public void setFrequency(int frequency)
	{
		this.frequency = frequency;
	}
}
