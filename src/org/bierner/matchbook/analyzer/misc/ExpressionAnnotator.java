package org.bierner.matchbook.analyzer.misc;

import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.RangeAnnotations;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.matcher.realtime.MatchAnnotation;
import org.bierner.matchbook.matcher.realtime.RealtimeExpressionFactory;
import org.bierner.matchbook.matcher.realtime.RealtimeMatcherFactory;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher;
import org.bierner.matchbook.matcher.realtime.expr.Expression;

/**
 * Creates an annotation based on a match expression
 * @author gann
 */
@AllArgsConstructor
public class ExpressionAnnotator implements SentenceAnnotator {
      static {
        AnnotationType.registerAnnotator(ExpressionAnnotator.class, AnnotationType.MATCH);
    }

    private final String name;
    private final String expr;

    @Override
    public void annotate(AnnotatableSentence sentence) {
        Expression e = new RealtimeExpressionFactory(sentence.getAnalyzer()).parse(expr);
        RealtimeSentenceMatcher matcher = RealtimeMatcherFactory.newIndexingMatcher(e);
        RealtimeSentenceMatcher.Matches matches = matcher.match(sentence);

        sentence.setAnnotations(AnnotationType.MATCH, new RangeAnnotations(
                matches.stream().map(m->new MatchAnnotation<String>(m, name, name)).collect(Collectors.toList())));
    }

}
