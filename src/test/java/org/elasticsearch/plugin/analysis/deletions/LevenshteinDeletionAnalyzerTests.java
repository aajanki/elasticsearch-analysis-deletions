package org.elasticsearch.plugin.analysis.deletions;

import com.google.common.collect.Lists;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.elasticsearch.test.ESTestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

public class LevenshteinDeletionAnalyzerTests extends ESTestCase {

    @Test
    public void testBasicAnalysis() throws Exception {
        Collection<String> tokens = analyze("red", 1);

        String[] expectedTerms = {"red", "re", "ed", "rd"};
        assertThat(tokens, containsInAnyOrder(expectedTerms));
    }

    @Test
    public void testSingleLetterTokenShouldNotGenerateEmptyTokens() throws Exception {
        Collection<String> tokens = analyze("a", 1);

        String[] expectedTerms = {"a"};
        assertThat(tokens, containsInAnyOrder(expectedTerms));
    }

    @Test
    public void testEmpty() throws Exception {
        Collection<String> tokens = analyze("", 1);

        assertThat(tokens, is(empty()));
    }

    @Test
    public void testIgnoreDuplicateTokens() throws Exception {
        Collection<String> tokens = analyze("fuzzy", 1);

        String[] expectedTerms = {"fuzzy", "uzzy", "fzzy", "fuzy", "fuzz"};
        assertThat(tokens, containsInAnyOrder(expectedTerms));
    }

    @Test
    public void testTwoDeletionsOneLetter() throws Exception {
        Collection<String> tokens = analyze("a", 2);

        String[] expectedTerms = {"a"};
        assertThat(tokens, containsInAnyOrder(expectedTerms));
    }

    @Test
    public void testTwoDeletionsTwoLetters() throws Exception {
        Collection<String> tokens = analyze("ab", 2);

        String[] expectedTerms = {"ab", "a", "b"};
        assertThat(tokens, containsInAnyOrder(expectedTerms));
    }

    @Test
    public void testTwoDeletionsThreeLetters() throws Exception {
        Collection<String> tokens = analyze("abc", 2);

        String[] expectedTerms = {"abc", "ab", "ac", "bc", "a", "b", "c"};
        assertThat(tokens, containsInAnyOrder(expectedTerms));
    }

    @Test
    public void testTwoDeletionsIgnoreDuplicateTokens() throws Exception {
        Collection<String> tokens = analyze("java", 2);

        String[] expectedTerms = {"java", "jav", "jaa", "jva", "ava", "ja", "jv", "av", "aa", "va"};
        assertThat(tokens, containsInAnyOrder(expectedTerms));
    }

    @Test
    public void testFourDeletions() throws Exception {
        Collection<String> tokens = analyze("banana", 4);

        String[] expectedTerms = {"ba", "bn", "an", "aa", "nn", "na", "ban",
                "baa", "bnn", "bna", "ana", "ann", "aan", "aaa", "nan", "naa",
                "nna", "bana", "bann", "baan", "baaa", "bnan", "bnaa", "bnna",
                "anan", "anaa", "anna", "aana", "nana", "banan", "banaa",
                "banna", "baana", "bnana", "anana", "banana"};
        assertThat(tokens, containsInAnyOrder(expectedTerms));
    }

    private Collection<String> analyze(final String source, int maxDeletions) throws IOException {
        Analyzer analyzer = createAnalyzer(maxDeletions);
        TokenStream stream = analyzer.tokenStream("test", source);
        return Lists.newArrayList(tokenStreamToIterator(stream));
    }

    private Analyzer createAnalyzer(int maxDeletions) {
        Settings settings = Settings.settingsBuilder()
                .put("path.home", createTempDir())
                .put("distance", maxDeletions)
                .put("index.analysis.analyzer.testanalyzer.type", "deletions")
                .put("index.analysis.analyzer.testanalyzer.distance", maxDeletions)
                .build();

        AnalysisModule.AnalysisBinderProcessor analysisBinderProcessor = new LevenshteinDeletionAnalysisBinderProcessor();
        AnalysisService analysisService = createAnalysisService(analysisBinderProcessor, settings);

        return analysisService.analyzer("testanalyzer").analyzer();
    }

    private Iterator<String> tokenStreamToIterator(final TokenStream stream) throws IOException {
        final CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
        stream.reset();

        return new Iterator<String>() {
            public boolean hasNext() {
                try {
                    boolean next = stream.incrementToken();
                    if (!next) {
                        stream.end();
                    }
                    return next;
                } catch (IOException e) {
                    return false;
                }
            }

            public String next() {
                return term.toString();
            }

            public void remove() {}
        };
    }

    private static AnalysisService createAnalysisService(final AnalysisModule.AnalysisBinderProcessor analysisBinderProcessor, final Settings settings) {
        Index index = new Index("test");
        Settings indexSettings = Settings.settingsBuilder()
                .put(settings)
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .build();
        EnvironmentModule environmentModule = new EnvironmentModule(new Environment(settings));
        SettingsModule settingsModule = new SettingsModule(settings);
        Injector parentInjector = new ModulesBuilder().add(settingsModule, environmentModule).createInjector();
        AnalysisModule analysisModule = new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class))
                .addProcessor(analysisBinderProcessor);
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, indexSettings),
                new IndexNameModule(index),
                analysisModule)
                .createChildInjector(parentInjector);
        return injector.getInstance(AnalysisService.class);
    }
}
