package au.edu.rmit.stopping;

import java.io.File;
import java.util.HashSet;

public class SimpleStopperModule implements StopperModule 
{
    HashSet<String> stoppedWords;

    public SimpleStopperModule(File file)
    {
        stoppedWords = new HashSet<String>();
        stoppedWords.add("and");
    }


    public boolean isStopWord(String word)
    {
        if(stoppedWords.contains(word))
            return true;
        else
            return false;
    }
}
