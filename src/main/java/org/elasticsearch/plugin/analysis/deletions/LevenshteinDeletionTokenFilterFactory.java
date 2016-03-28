package org.elasticsearch.plugin.analysis.deletions;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;

public class LevenshteinDeletionTokenFilterFactory extends AbstractTokenFilterFactory {

    public static final int DEFAULT_DISTANCE = 1;

    private int distance;

    @Inject
    public LevenshteinDeletionTokenFilterFactory(Index index, IndexSettingsService indexSettingsService, @Assisted String name, @Assisted Settings settings) {
        this(index, indexSettingsService.getSettings(), name, settings);
    }

    public LevenshteinDeletionTokenFilterFactory(Index index, Settings indexSettings, String name, Settings settings) {
        super(index, indexSettings, name, settings);
        this.distance = settings.getAsInt("distance", DEFAULT_DISTANCE);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new LevenshteinDeletionTokenFilter(tokenStream, distance);
    }
}
