package au.edu.rmit.parsing;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import au.edu.rmit.indexing.IndexerModule;
import au.edu.rmit.misc.TokenType;
import au.edu.rmit.stopping.StopperModule;
import static java.util.Arrays.asList;



public class SimpleParser 
{
    StopperModule stopperModule;
    IndexerModule indexerModule;
    DocIdHandler documentHandler;
    HashMap<String, Integer> docTermList;
    boolean shouldPrintTerms;

    boolean inDocument = false;
    String rawDocId = "";
    int currentDocId;


    public SimpleParser(StopperModule stopperModule, 
    					IndexerModule indexerModule, 
    					DocIdHandler documentHandler, 
    					boolean shouldPrintTerms)
    {
        this.stopperModule = stopperModule;
        this.indexerModule = indexerModule;
        this.documentHandler = documentHandler;
        this.shouldPrintTerms = shouldPrintTerms;
    }

    private HashSet<Character> getTerminatorChar(TokenType token)
    {
        switch (token)
        {
        case STAG: {
        	HashSet<Character> ret = new HashSet<Character>();
        	ret.add('>');
            return ret;
        }
        case ETAG: {
        	HashSet<Character> ret = new HashSet<Character>();
        	ret.add('>');
            return ret;
        }
        case WORD: {
        	HashSet<Character> ret = new HashSet<Character>();
        	ret.add('<');
            return ret;
        }
        case CONTENT: {
        	HashSet<Character> ret = new HashSet<Character>();
        	ret.addAll(asList(' ', '.', ',', ';', ':', '\'', '\"', '(', ')', '[', ']', '?', '-', '/', '\\', '!'));
            return ret;
        }
        default: {
        	HashSet<Character> ret = new HashSet<Character>();
        	ret.add(' ');
            return ret;
        }
        }
    }

    

    private String consumeToken(TokenType tt, Reader reader) throws IOException
    {
        StringBuilder sb = new StringBuilder();

        HashSet<Character> chars = getTerminatorChar(tt);

        int r;
        while ((r = reader.read()) != -1) {
            char ch = (char) r;

            if(chars.contains(ch))
                break;

            sb.append(ch);
        }

        return sb.toString();
    }
    
    

    private void consumeContentUntil(Reader reader, String terminatorTagName, TermHandler termHandler) 
    		throws IOException
    {
        int r;
        while ((r = reader.read()) != -1)
        {
            char ch = (char) r;

            if(ch == '<')
            {
                r = reader.read();
                if (r != -1)
                {
                    char nextChar = (char) r;
                    if(nextChar != '/')
                    {
                        consumeToken(TokenType.STAG, reader);
                    }
                    else
                    {
                        String tagName = consumeToken(TokenType.ETAG, reader);
                        if(terminatorTagName != null && tagName.equals(terminatorTagName))
                            break;
                    }
                }
            }
            else if(Character.isLetter(ch))
            {
                String aWord = ch + consumeToken(TokenType.CONTENT, reader);
                String lowerCaseTerm = aWord.toLowerCase();
                if(!stopperModule.isStopWord(lowerCaseTerm))
                {
                	if(shouldPrintTerms)
                		System.out.println(lowerCaseTerm);
                	
                	termHandler.handleTerm(lowerCaseTerm);
                }
            }
        }
    }

    private void skipUntil(String s, Reader reader) throws IOException
    {
        StringBuilder tempString = new StringBuilder();

        if(s.equals(""))
            return;

        int r;
        while ((r = reader.read()) != -1) {
            char ch = (char) r;

            tempString.append(ch);

            String substring = tempString.toString();
            if(tempString.length() > s.length())
                substring = tempString.substring(tempString.length() - s.length());

            if(s.equals(substring))
                break;
        }
    }

    private void consumeSomething(Reader reader) throws IOException
    {
        int r;
        while ((r = reader.read()) != -1) {
            char ch = (char) r;

            if(ch == '<')
            {
                r = reader.read();
                if (r != -1)
                {
                    char nextChar = (char) r;
                    if(nextChar != '/')
                    {
                        String tagName = nextChar + consumeToken(TokenType.STAG, reader);

                        if(tagName.equals("DOC"))
                        {
                            inDocument = true;
                        }
                        else if(tagName.equals("DOCNO"))
                        {
                            rawDocId = consumeToken(TokenType.WORD, reader);
                            consumeToken(TokenType.ETAG, reader);

                            
                            
                            // Get new docId and initialise term list
                            currentDocId = documentHandler.getDocumentId(rawDocId.trim());
                            docTermList = new HashMap<String, Integer>();
                            
                            
                        }
                        else if(tagName.equals("HEADLINE") || tagName.equals("TEXT"))
                        {
                            consumeContentUntil(reader, tagName, new TermHandler()
							{
								@Override
								public void handleTerm(String term)
								{
									if (docTermList.containsKey(term))
				                	{
					                	docTermList.put(term, new Integer(docTermList.get(term).intValue() + 1));
				                	}
				                	else
				                	{
					                	docTermList.put(term, new Integer(1));
				                	}
									
								}
							});
                        }
                        else
                        {
                            skipUntil("</" + tagName + ">", reader);
                        }
                    }
                    else
                    {
                        String tagName = consumeToken(TokenType.ETAG, reader);

                        if(tagName.equals("DOC"))
                        {
                            inDocument = false;
                        	// Add current document to term list
                        	indexerModule.addDocument(currentDocId, docTermList);
                        }

                        if(tagName.equals("DOCNO"))
                        {
                            rawDocId = "";
                        }
                        //else
                            //consumeSomething(reader);


                    }

                }
            }

        }
    }

    public void parseFile(File file) {

        try (InputStream in = new FileInputStream(file);
             Reader reader = new InputStreamReader(in, Charset.defaultCharset());
             // buffer for efficiency
             Reader bufferedReader = new BufferedReader(reader)) {

            consumeSomething(bufferedReader);
        }
        catch (IOException e)
        {
        	e.printStackTrace();
        }
    }
    
    public ArrayList<String> parseQueryString(String queryString)
    {
    	final ArrayList<String> outputTerms = new ArrayList<String>();
    	
    	try
		{
    		InputStream is = new ByteArrayInputStream(queryString.getBytes(Charset.defaultCharset()));
        	Reader reader = new InputStreamReader(is, Charset.defaultCharset());
            // buffer for efficiency
            Reader bufferedReader = new BufferedReader(reader);
        	
			consumeContentUntil(bufferedReader, null, new TermHandler()
			{
				@Override
				public void handleTerm(String term)
				{
					outputTerms.add(term);
					
				}
			});
		} catch (IOException e)
		{
			System.err.println("Unable to parse query string");
			e.printStackTrace();
		}
    	
    	return outputTerms;
    }

}

interface TermHandler
{
	public void handleTerm(String term);
}

/*class DocNoMapping 
{
    public int genId;
    public String docNo;

    public DocNoMapping(int genId, String docNo)
    {
        this.genId = genId;
        this.docNo = docNo;
    }
}*/
