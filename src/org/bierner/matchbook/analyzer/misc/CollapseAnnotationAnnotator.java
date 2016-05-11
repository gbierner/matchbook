package org.bierner.matchbook.analyzer.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.Annotations;
import org.bierner.matchbook.analyzer.RangeAnnotations;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;
import static org.bierner.matchbook.analyzer.util.SentenceUtilities.getTokens;

/**
 * A simple annotator that combines sequential or overlapping identical annotations together into a single new annotation (with possible
 * ignored tokens in between).
 *
 * @author gann
 * @param <T>
 */
public abstract class CollapseAnnotationAnnotator<T> implements SentenceAnnotator {
    private final AnnotationType<T> combineType;
    private final T combineValue;
    private final Set<String> skipTokens;

    @Getter
    private final AnnotationType<String> annotationType;


    public CollapseAnnotationAnnotator(String id, AnnotationType<T> combineType, T value, Set<String> skipTokens) {
        this.combineType = combineType;
        this.combineValue = value;
        this.skipTokens = skipTokens;
        annotationType = new AnnotationType<>(id, String.class);
        AnnotationType.registerAnnotator(getClass(), annotationType);
    }

    @Override
    public void annotate(AnnotatableSentence sentence) {
        List<String> tokens = getTokens(sentence);
        Annotations annotations = sentence.getAnnotations(combineType);
        List<Annotation> newAnnotations = new ArrayList<>();

        int start = -1;
        int end = -1;
        for (int i = 0; i < annotations.size(); i++) {
            Annotation a = annotations.get(i);
            if (! a.getValue().equals(combineValue))
                continue;

            if (start == -1) {
                start = a.getStart();
                end = a.getEnd();
            } else if (a.getStart() <= end) {
                if (a.getEnd() > end)
                    end = a.getEnd();
            } else {
                boolean allSkips = true;
                for (int s = end; s < a.getStart(); s++)
                    if (! skipTokens.contains(tokens.get(s))) {
                        allSkips = false;
                        break;
                    }
                if (allSkips)
                    end = a.getEnd();
                else {
                    String txt = SentenceUtilities.subSentence(sentence, start, end);
                    newAnnotations.add(new Annotation.SimpleAnnotation(txt, txt, start, end));
                    start = a.getStart();
                    end = a.getEnd();
                }
            }
        }
        if (start >= 0) {
            String txt = SentenceUtilities.subSentence(sentence, start, end);
            newAnnotations.add(new Annotation.SimpleAnnotation(txt, txt, start, end));
        }

        sentence.setAnnotations(getAnnotationType(), new RangeAnnotations(newAnnotations));
    }
}
