package org.elasticsearch.plugin.analysis.deletions;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettingsService;

public class LevenshteinDeleteionAnalyzerProvider extends AbstractIndexAnalyzerProvider<LevenshteinDeletionAnalyzer> {

    private final LevenshteinDeletionAnalyzer analyzer;

    @Inject
    public LevenshteinDeleteionAnalyzerProvider(Index index, IndexSettingsService indexSettingsService, Environment env, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettingsService.getSettings(), name, settings);
        int distance = settings.getAsInt("distance", LevenshteinDeletionTokenFilterFactory.DEFAULT_DISTANCE);
        analyzer = new LevenshteinDeletionAnalyzer(distance);
    }

    @Override
    public LevenshteinDeletionAnalyzer get() {
        return this.analyzer;
    }
}
