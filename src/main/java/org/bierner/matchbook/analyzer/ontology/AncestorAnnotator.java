/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.ontology;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.ExtensionMethod;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.RangeAnnotations;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.analyzer.ontology.Ontology.Concept;

/**
 *
 * @author gann
 */
@ExtensionMethod(OntologyUtilities.class)
public class AncestorAnnotator implements SentenceAnnotator {

    static {
        AnnotationType.registerAnnotator(AncestorAnnotator.class, AnnotationType.ANCESTOR_CONCEPT);
    }
    
    @Override
    public void annotate(AnnotatableSentence sentence) {
        List<Annotation<Concept>> annotations = new ArrayList<>();
        for (Annotation<Concept> a : sentence.getAnnotations(AnnotationType.CONCEPT)) {
            for (Concept ancestor : a.getValue().isa()) {
                annotations.add(new Annotation.SimpleAnnotation<>(ancestor.getId(), ancestor, a.getStart(), a.getEnd()));
            }
        }

        sentence.setAnnotations(AnnotationType.ANCESTOR_CONCEPT, new RangeAnnotations<>(annotations));
    }

}
