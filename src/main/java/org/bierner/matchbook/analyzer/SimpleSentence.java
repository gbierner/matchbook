/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lombok.*;

/**
 * An implementation of sentence which lazily gets annotations from its analyzer upon request.
 * @author gann
 */
@RequiredArgsConstructor @Getter
public class SimpleSentence implements Sentence {

    @NonNull private final String   text;
    @NonNull private final Locale   locale;
    @NonNull private final Analyzer analyzer;

    private SimpleAnnotatable annotatable = getAnnotatable();

    protected SimpleAnnotatable getAnnotatable() {
        return new SimpleAnnotatable(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Annotations<T> getAnnotations(AnnotationType<T> type) {
        if (! annotatable.annotationCache.containsKey(type))
            analyzer.applyAnnotations(annotatable, type);

        return (Annotations<T>) annotatable.annotationCache.get(type);
    }

    @Override
    public <T> boolean hasAnnotation(AnnotationType<T> type) {
        return analyzer.provides(annotatable, type);
    }

    @RequiredArgsConstructor
    protected static class SimpleAnnotatable implements AnnotatableSentence {
        @NonNull @Delegate
        SimpleSentence delegate;

        private Map<AnnotationType<?>, Annotations<?>> annotationCache = new HashMap<>();

        @Override
        public <T> void setAnnotations(AnnotationType<T> type, Annotations<T> annotations) {
            annotationCache.put(type, annotations);
        }
    }

}
