package au.edu.rmit.querying;

public class QueryResult implements Comparable<QueryResult> {

    private final String rawDocId;
    private final double score;
    
    public QueryResult(String rawDocId, double score)
    {
        this.rawDocId = rawDocId;
        this.score = score;
    }

    /**
     * @return the rawDocId
     */
    public String getRawDocId() {
        return rawDocId;
    }

    /**
     * @return the score
     */
    public double getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueryResult))
            return false;
        QueryResult q = (QueryResult) o;
        return q.rawDocId.equals(this.rawDocId);
    }

    @Override
    public int hashCode() {
        return this.rawDocId.hashCode();
    }

    @Override
    public String toString() {
        return this.rawDocId + " " + this.score;
    }

    @Override
    public int compareTo(QueryResult q) {
        return Double.valueOf(this.getScore()).compareTo(Double.valueOf(q.getScore()));
    }
}
