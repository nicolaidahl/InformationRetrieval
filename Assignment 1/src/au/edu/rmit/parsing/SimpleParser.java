package au.edu.rmit.parsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import au.edu.rmit.indexing.IndexerModule;
import au.edu.rmit.misc.TokenType;
import au.edu.rmit.stopping.StopperModule;




public class SimpleParser 
{

    ArrayList<String> tokenList;
    StopperModule stopperModule;
    IndexerModule indexerModule;
    DocIdHandler documentHandler;
    HashMap<String, Integer> docTermList;


    boolean inDocument = false;
    String rawDocId = "";
    int currentDocId;


    public SimpleParser(StopperModule stopperModule, IndexerModule indexerModule, DocIdHandler documentHandler)
    {
        tokenList = new ArrayList<String>();
        this.stopperModule = stopperModule;
        this.indexerModule = indexerModule;
        this.documentHandler = documentHandler;
    }

    private char getTerminatorChar(TokenType token)
    {
        switch (token)
        {
        case STAG:
            return '>';
        case ETAG:
            return '>';
        case WORD:
            return '<';
        case CONTENT:
            return ' ';
        default:
            return ' ';
        }
    }



    private String consumeToken(TokenType tt, Reader reader) throws IOException
    {
        StringBuilder sb = new StringBuilder();

        char c = getTerminatorChar(tt);

        int r;
        while ((r = reader.read()) != -1) {
            char ch = (char) r;

            if(c == ch)
                break;

            sb.append(ch);
        }

        return sb.toString();
    }

    private void consumeContentUntil(Reader reader, String terminatorTagName) throws IOException
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
                        if(tagName.equals(terminatorTagName))
                            break;
                    }
                }
            }
            else if(ch != '\n')
            {
                String aWord = ch + consumeToken(TokenType.CONTENT, reader);
                String lowerCaseWord = aWord.toLowerCase();
                if(!stopperModule.isStopWord(lowerCaseWord))
                {
                    //indexerModule.indexWord(lowerCaseWord, currentDocId);
                	if (docTermList.containsKey(lowerCaseWord))
                	{
	                	docTermList.put(lowerCaseWord, new Integer(docTermList.get(lowerCaseWord).intValue() + 1));
                	}
                	else
                	{
	                	docTermList.put(lowerCaseWord, new Integer(1));
                	}
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
                            consumeContentUntil(reader, tagName);
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

        }
    }

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