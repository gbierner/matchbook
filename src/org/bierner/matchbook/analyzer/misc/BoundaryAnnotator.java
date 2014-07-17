/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.misc;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.Annotations;
import org.bierner.matchbook.analyzer.RangeAnnotations;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;

/**
 *
 * @author gann
 */
@ExtensionMethod(SentenceUtilities.class)
public class BoundaryAnnotator implements SentenceAnnotator {
    static {
        AnnotationType.registerAnnotator(BoundaryAnnotator.class, AnnotationType.BOUNDARY);
    }
    
    @AllArgsConstructor
    private static class BoundaryAnnotation implements Annotation<String> {
        private String value;        
        private int pos;        
        @Override public String getValue() { return value; }
        @Override public String getId()    { return value; }
        @Override public int getStart()    { return pos; }
        @Override public int getEnd()      { return pos; }                
    }

    @Override
    public void annotate(AnnotatableSentence sentence) {
        Annotations<String> annotations = new RangeAnnotations<>(Lists.newArrayList(
                new BoundaryAnnotation(Annotation.BOUNDARY_START, 0),
                new BoundaryAnnotation(Annotation.BOUNDARY_END, sentence.getAnnotations(AnnotationType.TOKEN).size())                
                ));
        sentence.setAnnotations(AnnotationType.BOUNDARY, annotations);
    }

}
