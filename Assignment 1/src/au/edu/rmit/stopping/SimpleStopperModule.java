package au.edu.rmit.stopping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class SimpleStopperModule implements StopperModule 
{
    HashSet<String> stoppedWords;

    public SimpleStopperModule(File file)
    {
        stoppedWords = readStopWordsFromFile(file);        
    }
    
    private HashSet<String> readStopWordsFromFile(File file)
    {
    	HashSet<String> words = new HashSet<String>();
    	
    	BufferedReader br;
		try
		{
			br = new BufferedReader(new FileReader(file));
			String line;
	    	while ((line = br.readLine()) != null) {
	    	   words.add(line);
	    	}
	    	br.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			System.err.println("Invalid stoplist format");
			e.printStackTrace();
		}
    	
    	
    	return words;
    }


    public boolean isStopWord(String word)
    {
        if(stoppedWords.contains(word))
            return true;
        else
            return false;
    }
}
