package org.bierner.matchbook.analyzer.misc;

import java.util.Collections;
import java.util.List;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.RangeAnnotations;
import org.bierner.matchbook.analyzer.SentenceAnnotator;

/**
 * Annotates the entire span of a sentence.  This way you can easily test for multiple matches
 * within a sentence using SENTENCE WITH (a, b, c).
 * @author gann
 */
public class SentenceSpanAnnotator implements SentenceAnnotator {
    static {
        AnnotationType.registerAnnotator(SentenceSpanAnnotator.class, AnnotationType.SENTENCE);
    }

    @Override
    public void annotate(AnnotatableSentence sentence) {
        List<Annotation<String>> annotations =
                Collections.singletonList(new Annotation.SimpleAnnotation("", "", 0, sentence.getAnnotations(AnnotationType.TOKEN).size()));
        sentence.setAnnotations(AnnotationType.SENTENCE, new RangeAnnotations<String>(annotations));
    }
}
