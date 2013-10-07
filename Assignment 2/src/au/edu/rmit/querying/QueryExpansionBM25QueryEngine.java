package au.edu.rmit.querying;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import au.edu.rmit.misc.Toolbox;

public class QueryExpansionBM25QueryEngine extends BM25RankedQueryEngine
{
	
    public QueryExpansionBM25QueryEngine(File lexicon, File invlist, File mapFile, int numResults)
    {
        super(lexicon, invlist, mapFile, numResults);
        this.numResults = numResults;
    }

	@Override
	public QueryResult[] getResults(String[] queryTerms)
	{
        QueryResult[] initialResults = super.getResults(queryTerms);
          
        //Calculate TSVs using the initial results
        ArrayList<String> rawDocIDs = new ArrayList<String>();
        for (QueryResult res : initialResults)
			rawDocIDs.add(res.getRawDocId());

        String[] newTerms = findNewTermsFromDocuments(rawDocIDs.toArray(new String[0]), 25);
        
        //Append some terms
        ArrayList<String> expandedQuery = new ArrayList<String>(Arrays.asList(queryTerms));
        expandedQuery.addAll(Arrays.asList(newTerms));
        String[] expandedQueryArray = expandedQuery.toArray(new String[0]);  
        
        //Compute the final results 
        QueryResult[] finalResults = super.getResults(expandedQueryArray);
		return finalResults;
	}

	private String[] findNewTermsFromDocuments(String[] rawDocIDs, int E)
	{
		int R = rawDocIDs.length;
		double N = docIdHandler.getNumberOfDocuments();
		
		String[] candidateTerms = findCandidateTerms();
		PriorityQueue<CandidateTerm> pq = new PriorityQueue<CandidateTerm>(candidateTerms.length, 
				new Comparator<CandidateTerm>()
		{
			@Override
		    public int compare(CandidateTerm x, CandidateTerm y)
		    {
		        if (x.TSV < y.TSV)
		            return -1;
		        if (x.TSV > y.TSV)
		            return 1;
		        return 0;
		    }
		});
		
		for (String candidate : candidateTerms)
		{
			double ft = documentFrequencyForTerm(candidate);
			int rt = 0; //TODO
			
			double tsv = Math.pow(ft/N, rt) * Toolbox.choose(R, rt);
			CandidateTerm t = new CandidateTerm(tsv, candidate);
			
			pq.add(t);
		}
		
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < E; i++)
			result.add(pq.poll().getTerm());
		
		return result.toArray(new String[0]);
	}
	
	private String[] findCandidateTerms()
	{
		//TODO IMPLEMENT
		return new String[0];
	}
	
	
}

class CandidateTerm
{
	double TSV;
	String term;
	public CandidateTerm(double tSV, String term)
	{
		super();
		TSV = tSV;
		this.term = term;
	}
	public double getTSV()
	{
		return TSV;
	}
	public String getTerm()
	{
		return term;
	}
}
