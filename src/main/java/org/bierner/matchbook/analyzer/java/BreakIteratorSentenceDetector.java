/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.java;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import org.bierner.matchbook.analyzer.SentenceDetector;

/**
 * A wrapper around Java's BreakIterator for sentence detection.  Any locale supported
 * by the Java API is therefore supported here.
 * 
 * @author gann
 */
@AllArgsConstructor
public class BreakIteratorSentenceDetector implements SentenceDetector {
    
    private Locale locale;

    @Override
    public List<String> getSentences(String text) {
        List<String> sentences = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(locale);
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            sentences.add(text.substring(start,end));
        }
        return sentences;
    }
    
}
