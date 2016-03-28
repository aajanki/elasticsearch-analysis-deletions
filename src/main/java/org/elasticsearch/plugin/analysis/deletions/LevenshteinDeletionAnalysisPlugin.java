package org.elasticsearch.plugin.analysis.deletions;

import java.util.Collection;
import java.util.Collections;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.Plugin;

public class LevenshteinDeletionAnalysisPlugin extends Plugin {
    @Override
    public String name() {
        return "elasticsearch-analysis-deletions";
    }

    @Override
    public String description() {
        return "An analyzer plugin that indexes Levenshtein deletion variants";
    }

    @Override
    public Collection<Module> nodeModules() {
        return Collections.<Module>singletonList(new LevenshteinDeletionModule());
    }

    public void onModule(AnalysisModule module) {
        module.addProcessor(new LevenshteinDeletionAnalysisBinderProcessor());
    }
}
