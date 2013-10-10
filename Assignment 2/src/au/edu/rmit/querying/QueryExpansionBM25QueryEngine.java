package au.edu.rmit.querying;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import au.edu.rmit.indexing.InvalidTermIdException;
import au.edu.rmit.indexing.TermIdHandler;
import au.edu.rmit.misc.Toolbox;
import au.edu.rmit.misc.VariableByteEncoding;

public class QueryExpansionBM25QueryEngine extends BM25RankedQueryEngine
{
    private static final String QE_LEXICON_DELIM = " ";
	
	File termMap;
	File termLexicon;
	File termIndex;
    ArrayList<QELexiconDoc> QELexiconList;
    TermIdHandler termIdHandler;
    int assumedCorrectResults; 
    int appendedQueryTerms;

    public QueryExpansionBM25QueryEngine(File lexicon, File invlist, File mapFile,
            File termMap, File termLexicon, File termIndex, int assumedCorrectResults, int appendedQueryTerms)
    {
        super(lexicon, invlist, mapFile);

        this.termLexicon = termLexicon;
        this.termIndex = termIndex;
        this.termMap = termMap;
        QELexiconList = new ArrayList<QELexiconDoc>();
        this.assumedCorrectResults = assumedCorrectResults;
        this.appendedQueryTerms = appendedQueryTerms;

        // Read index when query engine is created
        readQELexicon();

        termIdHandler = new TermIdHandler(termMap);
    }

	@Override
	public QueryResult[] getResults(String[] queryTerms, int numResults)
	{
        QueryResult[] initialResults = super.getResults(queryTerms, assumedCorrectResults);
          
        //Calculate TSVs using the initial results
        String[] newTerms = findNewTermsFromDocuments(initialResults, appendedQueryTerms);
        
        //Append some terms
        ArrayList<String> expandedQuery = new ArrayList<String>(Arrays.asList(queryTerms));
        expandedQuery.addAll(Arrays.asList(newTerms));
        String[] expandedQueryArray = expandedQuery.toArray(new String[0]);  
        
        //Compute the final results 
        QueryResult[] finalResults = super.getResults(expandedQueryArray, numResults);
		return finalResults;
	}

	private String[] findNewTermsFromDocuments(QueryResult[] queryResults, int E)
	{
		int R = queryResults.length;
		double N = docIdHandler.getNumberOfDocuments();
		
		HashMap<Integer, Integer> candidateTerms = findCandidateTerms(queryResults);
		PriorityQueue<CandidateTerm> pq = new PriorityQueue<CandidateTerm>(candidateTerms.size(), 
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
		
		for (Integer candidateId : candidateTerms.keySet())
		{
			String candidate = "";
			try
			{
				candidate = termIdHandler.getTerm(candidateId);
			} catch (InvalidTermIdException e)
			{
				//Just fail, something is very wrong
				e.printStackTrace();
			}
			
			double ft = documentFrequencyForTerm(candidate);
			//The number of initial documents that have the term
			int rt = candidateTerms.get(candidateId); 
			double power = Math.pow(ft/N, rt);
			long choose = Toolbox.choose(R, rt);
			
			double tsv = power * choose;
			CandidateTerm t = new CandidateTerm(tsv, candidate);
			
			pq.add(t);
		}
		
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < E; i++)
			result.add(pq.poll().getTerm());
		
		return result.toArray(new String[0]);
	}

	private HashMap<Integer, Integer> findCandidateTerms(QueryResult[] queryResults)
	{
		HashMap<Integer, Integer> allCandidates = new HashMap<Integer, Integer>();
		
		for (QueryResult docId : queryResults)
		{
			String rawDocId = docId.getRawDocId();
			ArrayList<Integer> termsInDoc = getTermList(docIdHandler.getDocumentId(rawDocId));
			
			for (Integer termId : termsInDoc)
				if(allCandidates.containsKey(termId))
					allCandidates.put(termId, allCandidates.get(termId) + 1);
				else
					allCandidates.put(termId, 1);
			
		}
		
		return allCandidates;
	}
    
    /**
     * Get the list of term IDs for a given document
     * @param docId the document ID to retrieve
     * @return an array of term IDs contained within the given document
     */
    public ArrayList<Integer> getTermList(int docId)
    {
        if (docId > QELexiconList.size())
            return new ArrayList<Integer>();

        // Byte offset of postings list from lexicon.
        int filePosition = (int) QELexiconList.get(docId).filePosition;
        // Byte size of postings list from lexicon.
        int indexLength = (int) QELexiconList.get(docId).indexLength;

        ArrayList<Integer> termList = new ArrayList<Integer>();
        
        SeekableByteChannel sbc;
        try
        {
            sbc = Files.newByteChannel(termIndex.toPath());
            
            // Create byte buffer of the same size as the postings list.
            ByteBuffer buf = ByteBuffer.allocate(indexLength);

            sbc.position(filePosition);
            sbc.read(buf);
            
            buf.rewind();

            // Read bytes from buffer into Byte array.
            Byte[] byteArray = new Byte[indexLength];
            while (buf.hasRemaining())
            {
                byteArray[buf.position()] = buf.get();
            }
            sbc.close();
            
            // Decode variable byte encoded postings list into integer array.
            Integer[] valueArray = VariableByteEncoding.decode(byteArray);

            // Read postings list from integer array.
            int prevTermId = 0;
            for (int i = 0; i < valueArray.length; i++)
            {
                int curTermId = prevTermId + valueArray[i].intValue();
                termList.add(curTermId);
                prevTermId = curTermId;
            }
        }
        catch (IOException e)
        {
            System.err.println("Error reading file while retrieving term list for document " + docId);
            e.printStackTrace();
        }
        catch (Exception e)
        {
            System.err.println("Something went wrong retrieving the term list for " + docId);
            e.printStackTrace();
        }

        return termList;
    }
	
    /**
     * Read the QE index lexicon from disk.
     * Uses the termLex stored in the object by the constructor.
     */
    private void readQELexicon() 
    {
        BufferedReader br;

        try
		{
        	
			br = new BufferedReader(new FileReader(termLexicon));
			
			String line;
	        String[] tokens;

	    	while ((line = br.readLine()) != null) {
	    	    tokens = line.split(QE_LEXICON_DELIM);

	    	    QELexiconList.add(new QELexiconDoc(Long.parseLong(tokens[0]),
	    	                              Long.parseLong(tokens[1])));
	    	}

	        br.close();
		}
        catch (Exception e)
		{
			System.err.println("Unable to load query expansion lexicon file");
			e.printStackTrace();
			System.exit(-1);
		}
    }

    /**
     * Class to hold doc information read in from lexicon.
     * Only used internally.
     */
    private static class QELexiconDoc
    {
        public long filePosition;
        public long indexLength;
        
        public QELexiconDoc(long filePosition, long indexLength)
        {
            this.filePosition = filePosition;
            this.indexLength = indexLength;
        }
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
