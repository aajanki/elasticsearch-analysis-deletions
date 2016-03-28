package org.elasticsearch.plugin.analysis.deletions;

import org.elasticsearch.index.analysis.AnalysisModule;

public class LevenshteinDeletionAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processAnalyzers(AnalyzersBindings analyzersBindings) {
        analyzersBindings.processAnalyzer("deletions", LevenshteinDeleteionAnalyzerProvider.class);
    }

    @Override
    public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {
        tokenFiltersBindings.processTokenFilter("deletions", LevenshteinDeletionTokenFilterFactory.class);
    }
}
