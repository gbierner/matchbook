/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer;

import com.google.common.collect.Lists;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 *
 * @author gann
 */
@AllArgsConstructor
public class LocalizableTestAnnotator implements SentenceAnnotator {
    @NonNull private Locale locale;

    @Override
    public void annotate(AnnotatableSentence sentence) {
        sentence.setAnnotations(AnalyzerTest.TYPE, new UnitAnnotations<>(Lists.newArrayList(locale)));
    }

}
