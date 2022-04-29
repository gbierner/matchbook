/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer;

import com.google.common.collect.Lists;
import java.util.Locale;

/**
 *
 * @author gann
 */
public class TestAnnotator implements SentenceAnnotator {
    @Override
    public void annotate(AnnotatableSentence sentence) {
        sentence.setAnnotations(AnalyzerTest.TYPE, new UnitAnnotations<>(Lists.newArrayList(Locale.ENGLISH)));
    }
}
