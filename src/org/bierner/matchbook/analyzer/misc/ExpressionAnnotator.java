package org.bierner.matchbook.analyzer.misc;

import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.RangeAnnotations;
import org.bierner.matchbook.analyzer.Sentence;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.matcher.realtime.MatchAnnotation;
import org.bierner.matchbook.matcher.realtime.RealtimeExpressionFactory;
import org.bierner.matchbook.matcher.realtime.RealtimeMatcherFactory;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher.Match;
import org.bierner.matchbook.matcher.realtime.expr.Expression;

/**
 * Creates an annotation based on a match expression
 * @author gann
 */
@AllArgsConstructor
public abstract class ExpressionAnnotator<T> implements SentenceAnnotator {
    protected final String id;
    private final String expr;

    protected abstract T getValue(Sentence sentence, Match match);
    protected abstract AnnotationType<T> getAnnotationType();

    @Override
    public void annotate(AnnotatableSentence sentence) {
        Expression e = new RealtimeExpressionFactory(sentence.getAnalyzer()).parse(expr);
        RealtimeSentenceMatcher matcher = RealtimeMatcherFactory.newIndexingMatcher(e);
        RealtimeSentenceMatcher.Matches matches = matcher.match(sentence);
        sentence.setAnnotations(getAnnotationType(), new RangeAnnotations(
                matches.stream().map(m->new MatchAnnotation<T>(m, id, getValue(sentence, m))).collect(Collectors.toList())));
    }

    public static class SimpleExpressionAnnotator extends ExpressionAnnotator<String> {
        @Getter
        AnnotationType<String> annotationType;

        public SimpleExpressionAnnotator(String id, String expr) {
            super(id, expr);
            annotationType = new AnnotationType<>(id, String.class);
            AnnotationType.registerAnnotator(getClass(), annotationType);
        }

        @Override
        protected String getValue(Sentence sentence, Match match) {
            return id;
        }

    }

}
