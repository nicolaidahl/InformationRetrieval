package au.edu.rmit.querying;

import java.io.IOException;

import au.edu.rmit.indexing.PostingsList;

public interface QueryEngine {
    public PostingsList getPostings(String term) throws IOException;
}
