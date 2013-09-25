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
    
    //Buffered terms
    HashMap<String, Integer> docTermList;
    int documentLength;
    
    //Indicating if each term found should be printed
    boolean shouldPrintTerms;

    //Document indicators and identification
    boolean inDocument = false;
    String rawDocId = "";
    int currentDocId;
    
    //The list of common prefixes when analyzing hyphenized tokens
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
        this.documentLength = 0;
        
        commonPrefixes = new HashSet<String>(Arrays.asList("co", "pre", "meta", "multi", "auto", 
        		"circum", "com", "con", "contra", "de", "dis", "en", "ex", "extra", "hetero", "homo", 
        		"hyper", "im", "in", "inter", "intra", "marco", "micro", "mono", "non", "omni", 
        		"post", "pre", "pro", "sub", "syn", "trans", "tri", "un", "uni"));
    }

    
    /**
     * Consumes characters from the given reader until the token terminator
     * tells it to terminate
     * @param reader Any type of input reader
     * @param terminator Some instance that can determine when the token should be
     * terminated
     * @return The parsed token
     * @throws IOException If reading from input fails
     */
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
    
    
    /**
     * Will parse input until the given terminator tag is parsed. Note that this method
     * works on a tag level as opposed to consumeToken
     * @param reader Any type of input reader
     * @param terminatorTagName The tag name that will terminate the method
     * @param tokenHandler A handler of any token that has been parsed
     * @throws IOException If reading from input fails
     */
    private void consumeContentUntil(Reader reader, String terminatorTagName, TokenHandler tokenHandler) 
    		throws IOException
    {
        int r;
        while ((r = reader.read()) != -1)
        {
            char ch = (char) r;

            //A tag was encountered
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
                    	//End this method call if we've reached the tag we were looking for
                        String tagName = consumeToken(reader, new TagTerminator());
                        if(terminatorTagName != null && tagName.equals(terminatorTagName))
                            break;
                    }
                }
            }
            //Beginning of a word token
            else if(Character.isLetter(ch))
            {
            	LetterContentTerminator terminator = new LetterContentTerminator();
            	
                String aWord = ch + consumeToken(reader, terminator);
                String lowerCaseTerm = aWord.toLowerCase();
                ArrayList<String> tokens;
                
                //Handle the case where the token is hyphenated
                if(terminator.doesContainHypens())
                	tokens = handleHyphenatedToken(lowerCaseTerm);
                else
                {
                	tokens = new ArrayList<String>();
                	tokens.add(lowerCaseTerm);
                }
                	
                //Add all the tokens if the stopper module permits
                for (String token : tokens)
				{
                    if(!stopperModule.isStopWord(token))
                    {
                    	if(shouldPrintTerms)
                    		System.out.println(token);
                    	
                    	tokenHandler.handleTerm(token);
                    }

				}
            }
            //Beginning of a number token
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
            	
            	//Add all the tokens if the stopper module permits
                if(!stopperModule.isStopWord(lowerCaseTerm))
                {
                	if(shouldPrintTerms)
                		System.out.println(lowerCaseTerm);
                	
                	tokenHandler.handleTerm(lowerCaseTerm);
                }
            }
        }
    }

    
    /**
     * Will handle a hyphenated token according to the rule set described
     * in the report
     * @param hyphenatedToken The token
     * @return A list of handled tokens
     */
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


    /**
     * Will skip the reader to a given string is encountered
     * @param s The string to encounter
     * @param reader A given input reader
     * @throws IOException If the input reading fails
     */
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

	/**
	 * The specific-purpose parsing method that will look for the HEADLINE
	 * TEXT and DOCNO tags to determine how certain parts of the document need
	 * to be parsed in order to retrieve the content and id of documents
	 * @param reader Any given input reader
	 * @throws IOException If the reader fails to read the input
	 */
    private void consumeSomething(Reader reader) throws IOException
    {
        int r;
        while ((r = reader.read()) != -1) {
            char ch = (char) r;

            // We encountered a tag
            if(ch == '<')
            {
                r = reader.read();
                if (r != -1)
                {
                    char nextChar = (char) r;
                    //It was not an end tag
                    if(nextChar != '/')
                    {
                        String tagName = nextChar + consumeToken(reader, new TagTerminator());

                        //It was the beginning of a document
                        if(tagName.equals("DOC"))
                        {
                            inDocument = true;
                            
                        }
                        //It was the beginning of the document number
                        else if(tagName.equals("DOCNO"))
                        {
                            rawDocId = consumeToken(reader, new WordTerminator());
                            consumeToken(reader, new TagTerminator());

                            // Get new docId and initialise term list
                            currentDocId = documentHandler.getDocumentId(rawDocId.trim());
                            docTermList = new HashMap<String, Integer>();
                            documentLength = 0;
                            
                        }
                        //It was the beginning of content
                        else if(tagName.equals("HEADLINE") || tagName.equals("TEXT"))
                        {
                        	//Consume content until we reach the end tag
                            consumeContentUntil(reader, tagName, new TokenHandler()
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
										documentLength += term.getBytes().length;
									}
									
								}
							});
                        }
                        //Otherwise just skip forward
                        else
                        {
                            skipUntil("</" + tagName + ">", reader);
                        }
                    }
                    //Otherwise it was an end tag
                    else
                    {
                        String tagName = consumeToken(reader, new TagTerminator());

                        if(tagName.equals("DOC"))
                        {
                            inDocument = false;
                        	// Add current document to term list
                        	indexerModule.addDocument(currentDocId, docTermList);
                        	documentHandler.setDocumentLength(currentDocId, documentLength);
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

    /**
     * The public interface of the SimpleParser to allow parsing of a file
     * @param file The file to be parsed
     */
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
    
    /**
     * The public interface of the SimpleParser to allow parsing of a query term
     * @param queryString The query string to be parsed
     * @return A list of tokens
     */
    public ArrayList<String> parseQueryString(String queryString)
    {
    	final ArrayList<String> outputTokens = new ArrayList<String>();
    	
    	try
		{
    		InputStream is = new ByteArrayInputStream(queryString.getBytes(Charset.defaultCharset()));
        	Reader reader = new InputStreamReader(is, Charset.defaultCharset());
            // buffer for efficiency
            Reader bufferedReader = new BufferedReader(reader);
        	
			consumeContentUntil(bufferedReader, null, new TokenHandler()
			{
				@Override
				public void handleTerm(String term)
				{
					outputTokens.add(term);
					
				}
			});
		} catch (IOException e)
		{
			System.err.println("Unable to parse query string");
			e.printStackTrace();
		}
    	
    	return outputTokens;
    }

}

interface TokenHandler
{
	public void handleTerm(String term);
}

