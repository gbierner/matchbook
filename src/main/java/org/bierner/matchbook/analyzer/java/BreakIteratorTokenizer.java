/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.java;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.google.common.base.CharMatcher;
import lombok.AllArgsConstructor;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.analyzer.Tokenizer;
import org.bierner.matchbook.analyzer.UnitAnnotations;

/**
 * A wrapper around Java's BreakIterator for tokenization.  Any locale supported
 * by the Java API is therefore supported here.
 * @author gann
 */
@AllArgsConstructor
public class BreakIteratorTokenizer implements SentenceAnnotator, Tokenizer {
    static {
        AnnotationType.registerAnnotator(BreakIteratorTokenizer.class, AnnotationType.TOKEN);
    }

    private Locale locale;
    
    @Override
    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        
        BreakIterator iterator = BreakIterator.getWordInstance(locale);
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String token = text.substring(start,end);
            if (! CharMatcher.whitespace().matchesAllOf(token))
                tokens.add(token.trim());
        }

        return tokens;
        
    }
    
    @Override
    public void annotate(AnnotatableSentence sentence) {
        List<String> tokens = tokenize(sentence.getText());
        sentence.setAnnotations(AnnotationType.TOKEN, new UnitAnnotations<>(tokens));
    }
}
