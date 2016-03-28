package org.elasticsearch.plugin.analysis.deletions;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class LevenshteinDeletionAnalyzer extends Analyzer {

    private final int distance;

    public LevenshteinDeletionAnalyzer(int distance) {
        super();
        this.distance = distance;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        TokenStream standarded = new StandardFilter(source);
        TokenStream lowered = new LowerCaseFilter(standarded);
        TokenStream filter = new LevenshteinDeletionTokenFilter(lowered, distance);
        return new TokenStreamComponents(source, filter);
    }
}
