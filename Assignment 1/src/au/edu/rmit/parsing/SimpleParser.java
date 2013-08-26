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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import au.edu.rmit.indexing.IndexerModule;
import au.edu.rmit.stopping.StopperModule;



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
    private HashSet<String> commonPrefixes;


    public SimpleParser(StopperModule stopperModule, 
    					IndexerModule indexerModule, 
    					DocIdHandler documentHandler, 
    					boolean shouldPrintTerms)
    {
        this.stopperModule = stopperModule;
        this.indexerModule = indexerModule;
        this.documentHandler = documentHandler;
        this.shouldPrintTerms = shouldPrintTerms;
        
        commonPrefixes = new HashSet<String>(Arrays.asList("co", "pre", "meta", "multi", "auto", 
        		"circum", "com", "con", "contra", "de", "dis", "en", "ex", "extra", "hetero", "homo", 
        		"hyper", "im", "in", "inter", "intra", "marco", "micro", "mono", "non", "omni", 
        		"post", "pre", "pro", "sub", "syn", "trans", "tri", "un", "uni"));
    }

    

    private String consumeToken(Reader reader, TokenTerminatorDeterminer terminator) throws IOException
    {
        StringBuilder sb = new StringBuilder();

        int r;
        while ((r = reader.read()) != -1) {
            char ch = (char) r;

            if(terminator.shouldTerminateToken(ch))
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
                        consumeToken(reader, new TagTerminator());
                    }
                    else
                    {
                        String tagName = consumeToken(reader, new TagTerminator());
                        if(terminatorTagName != null && tagName.equals(terminatorTagName))
                            break;
                    }
                }
            }
            else if(Character.isLetter(ch))
            {
            	LetterContentTerminator terminator = new LetterContentTerminator();
            	
                String aWord = ch + consumeToken(reader, terminator);
                String lowerCaseTerm = aWord.toLowerCase();
                ArrayList<String> tokens;
                
                if(terminator.doesContainHypens())
                	tokens = handleHyphenatedToken(lowerCaseTerm);
                else
                {
                	tokens = new ArrayList<String>();
                	tokens.add(lowerCaseTerm);
                }
                	
                for (String token : tokens)
				{
                    if(!stopperModule.isStopWord(token))
                    {
                    	if(shouldPrintTerms)
                    		System.out.println(token);
                    	
                    	termHandler.handleTerm(token);
                    }

				}
            }
            else if(Character.isDigit(ch))
            {
            	NumberContentTerminator terminator = new NumberContentTerminator();
            	String aWord = ch + consumeToken(reader, terminator);
            	String lowerCaseTerm = aWord.toLowerCase();
            	
            	//If a comma or dot was at the end of the token, remove them
            	if(terminator.wasLastCharWasDotOrComma())
            	{
            		int charsToTrim = 0;
            		for (int i = lowerCaseTerm.length() - 1; i >= 0; i--)
					{
						if(lowerCaseTerm.charAt(i) == ',' || lowerCaseTerm.charAt(i) == '.')
							charsToTrim++;
						else
							break;
					}
            		lowerCaseTerm = lowerCaseTerm.substring(0, lowerCaseTerm.length() - charsToTrim);
            	}
            	
                if(!stopperModule.isStopWord(lowerCaseTerm))
                {
                	if(shouldPrintTerms)
                		System.out.println(lowerCaseTerm);
                	
                	termHandler.handleTerm(lowerCaseTerm);
                }
            }
        }
    }

    
    
    private ArrayList<String> handleHyphenatedToken(String hyphenatedToken)
	{
		String[] parts = hyphenatedToken.split("-");

		if(parts.length == 2)
		{
			if(parts[1].matches("^.{2,}ed$"))
			{
				return new ArrayList<String>(Arrays.asList(hyphenatedToken));
			}
			else if(commonPrefixes.contains(parts[0]))
			{
				ArrayList<String> list = new ArrayList<String>();
				list.add(parts[0] + parts[1]);
				return list;
			} 
		}
		
		
		return new ArrayList<String>(Arrays.asList(parts));
		
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
                        String tagName = nextChar + consumeToken(reader, new TagTerminator());

                        if(tagName.equals("DOC"))
                        {
                            inDocument = true;
                        }
                        else if(tagName.equals("DOCNO"))
                        {
                            rawDocId = consumeToken(reader, new WordTerminator());
                            consumeToken(reader, new TagTerminator());

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
									if(term.length() > 2)
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
                        String tagName = consumeToken(reader, new TagTerminator());

                        if(tagName.equals("DOC"))
                        {
                            inDocument = false;
                        	// Add current document to term list
                        	indexerModule.addDocument(currentDocId, rawDocId, docTermList);
                        }

                        if(tagName.equals("DOCNO"))
                        {
                            rawDocId = "";
                        }


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

interface TokenTerminatorDeterminer
{
	public boolean shouldTerminateToken(char ch);
}

class LetterContentTerminator implements TokenTerminatorDeterminer
{
	private boolean containHypens = false;
	

	@Override
	public boolean shouldTerminateToken(char ch)
	{
		if(ch == '-')
		{
			containHypens = true;
			return false;
		}
		
		return !Character.isLetterOrDigit(ch);
	}

	public boolean doesContainHypens()
	{
		return containHypens;
	}
}

class NumberContentTerminator implements TokenTerminatorDeterminer
{
	private boolean lastCharWasDotOrComma = false;
	
	@Override
	public boolean shouldTerminateToken(char ch)
	{
		if(Character.isLetterOrDigit(ch))
		{
			lastCharWasDotOrComma = false;
			return false;
		}
		else if(ch == '.' || ch == ',')
		{
			lastCharWasDotOrComma = true;
			return false;
		}
		
		return true;
	}

	public boolean wasLastCharWasDotOrComma()
	{
		return lastCharWasDotOrComma;
	}
}

class TagTerminator implements TokenTerminatorDeterminer
{
	@Override
	public boolean shouldTerminateToken(char ch)
	{
		return ch == '>';
	}
}

class WordTerminator implements TokenTerminatorDeterminer
{
	@Override
	public boolean shouldTerminateToken(char ch)
	{
		return ch == '<';
	}
}