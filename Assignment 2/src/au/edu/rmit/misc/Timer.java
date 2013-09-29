package au.edu.rmit.misc;

import java.util.ArrayList;

public class Timer {

    ArrayList<Long> timings;
    ArrayList<String> labels;
    
    public Timer()
    {
        timings = new ArrayList<Long>();
        labels = new ArrayList<String>();
        
        timings.add(System.nanoTime());
        labels.add("Start");
    }
    
    public void stamp(String label)
    {
        timings.add(System.nanoTime());
        labels.add(label);
    }
    
    public String getTimings()
    {
        StringBuilder outputTimings = new StringBuilder();
        
        for (int i = 1; i < timings.size(); i++)
        {
            outputTimings.append(labels.get(i) + ": " + ((timings.get(i) - timings.get(i-1)) / 1000000) + " ms\n");
        }
        
        return outputTimings.toString();
    }
}
