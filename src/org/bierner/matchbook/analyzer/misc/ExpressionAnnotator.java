package org.bierner.matchbook.analyzer.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.RangeAnnotations;
import org.bierner.matchbook.analyzer.Sentence;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;
import org.bierner.matchbook.matcher.realtime.MatchAnnotation;
import org.bierner.matchbook.matcher.realtime.RealtimeExpressionFactory;
import org.bierner.matchbook.matcher.realtime.RealtimeMatcherFactory;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher.Match;
import org.bierner.matchbook.matcher.realtime.expr.Expression;

/**
 * Creates an annotation based on a match expression
 * @author gann
 * @param <T>
 */
public abstract class ExpressionAnnotator<T> implements SentenceAnnotator {
    protected final String id;
    private final String expr;

    @Getter
    private final AnnotationType<T> annotationType;


    public ExpressionAnnotator(String id, String expr, Class<T> clazz) {
        this.id = id;
        this.expr = expr;
        annotationType = new AnnotationType<>(id, clazz);
        AnnotationType.registerAnnotator(getClass(), annotationType);

    }

    protected abstract T getValue(Sentence sentence, Match match);
    protected abstract String getId(Sentence sentence, Match match);

    @Override
    public void annotate(AnnotatableSentence sentence) {
        Expression e = new RealtimeExpressionFactory(sentence.getAnalyzer()).parse(expr);
        RealtimeSentenceMatcher matcher = RealtimeMatcherFactory.newIndexingMatcher(e);
        RealtimeSentenceMatcher.Matches matches = matcher.match(sentence);

        List<Annotation> annotations = new ArrayList<>();
        int[] lastEnd = new int[] {-1};
        matches.stream().sorted((a,b)->
                // in order but when starting in the same place, take bigger ranges first
                a.getStart() != b.getStart()? a.getStart() - b.getStart() : b.getEnd() - a.getEnd()
        ).forEach(m -> {
            if (m.getStart() >= lastEnd[0]) {
                annotations.add(new MatchAnnotation<>(m, getId(sentence, m), getValue(sentence, m)));
                lastEnd[0] = m.getEnd();
            }
        });
        sentence.setAnnotations(getAnnotationType(), new RangeAnnotations(annotations));
    }

    public static class SimpleExpressionAnnotator extends ExpressionAnnotator<String> {
        public SimpleExpressionAnnotator(String id, String expr) {
            super(id, expr, String.class);
        }

        @Override
        protected String getValue(Sentence sentence, Match match) {
            return SentenceUtilities.subSentence(sentence, match.getStart(), match.getEnd());
        }

        @Override
        protected String getId(Sentence sentence, Match match) {
            return getValue(sentence, match);
        }
    }

}
