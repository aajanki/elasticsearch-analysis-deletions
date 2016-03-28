package org.elasticsearch.plugin.analysis.deletions;

import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.PreBuiltAnalyzerProviderFactory;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;

public class LevenshteinDeletionAnalysis extends AbstractComponent {

    @Inject
    public LevenshteinDeletionAnalysis(Settings settings, IndicesAnalysisService indicesAnalysisService) {
        super(settings);

        int distance = settings.getAsInt("distance", LevenshteinDeletionTokenFilterFactory.DEFAULT_DISTANCE);
        indicesAnalysisService.analyzerProviderFactories().put(
                "deletions",
                new PreBuiltAnalyzerProviderFactory("deletions",
                        AnalyzerScope.INDICES,
                        new LevenshteinDeletionAnalyzer(distance)));
    }
}
