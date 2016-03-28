package org.elasticsearch.plugin.analysis.deletions;

import org.elasticsearch.common.inject.AbstractModule;

public class LevenshteinDeletionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(LevenshteinDeletionAnalysis.class).asEagerSingleton();
    }
}
