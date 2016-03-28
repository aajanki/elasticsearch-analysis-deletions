package org.elasticsearch.plugin.analysis.deletions;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public final class LevenshteinDeletionTokenFilter extends TokenFilter {

    private int distance;
    private Set<String> variants;
    private AttributeSource.State state;
    private final CharTermAttribute termAtt;
    private final PositionIncrementAttribute positionIncrementAtt;

    public LevenshteinDeletionTokenFilter(TokenStream input, int distance) {
        super(input);
        this.distance = distance;
        this.state = null;
        this.variants = new HashSet<>();
        this.termAtt = addAttribute(CharTermAttribute.class);
        this.positionIncrementAtt = addAttribute(PositionIncrementAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!variants.isEmpty()) {
            return popVariant();
        }

        boolean hasToken = input.incrementToken();
        if (!hasToken) {
            return false;
        }

        // Add deletion variant to the stack
        storeAllVariants();

        // Return the original token
        state = captureState();
        return true;
    }

    private void storeAllVariants() {
        assert variants.isEmpty();

        String token = termAtt.toString();
        generateVariants(variants, token, distance);
    }

    private void generateVariants(final Set<String> stack, final String token, final int n) {
        if (token.length() > 1) {
            for (int i=0; i < token.length(); i++) {
                String tokenMinusOneChar = token.substring(0, i) + token.substring(i + 1);
                stack.add(tokenMinusOneChar);
                if (n > 1) {
                    generateVariants(stack, tokenMinusOneChar, n-1);
                }
            }
        }
    }

    private boolean popVariant() {
        assert !variants.isEmpty();

        String variant = variants.iterator().next();
        variants.remove(variant);
        restoreState(state);
        termAtt.copyBuffer(variant.toCharArray(), 0, variant.length());
        positionIncrementAtt.setPositionIncrement(0);
        return true;
    }

    @Override
    public void reset() throws IOException {
        state = null;
        variants.clear();
        super.reset();
    }
}
