package org.bierner.matchbook;

import java.util.Locale;
import lombok.experimental.ExtensionMethod;
import org.bierner.matchbook.analyzer.Sentence;
import org.bierner.matchbook.analyzer.SimpleAnalyzer;
import org.bierner.matchbook.analyzer.java.BreakIteratorSentenceDetector;
import org.bierner.matchbook.analyzer.misc.SpaceAnnotator;
import org.bierner.matchbook.analyzer.opennlp.PorterStemmer;
import org.bierner.matchbook.analyzer.opennlp.ThreadSafeOpenNLPChunker;
import org.bierner.matchbook.analyzer.opennlp.ThreadSafeOpenNLPPosTagger;
import org.bierner.matchbook.analyzer.opennlp.ThreadSafeOpenNLPTokenizer;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;
import org.bierner.matchbook.matcher.realtime.RealtimeExpressionFactory;
import org.bierner.matchbook.matcher.realtime.RealtimeMatcherFactory;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher.Match;
import org.bierner.matchbook.matcher.realtime.expr.Expression;

/**
 *
 * @author gann
 */
@ExtensionMethod(SentenceUtilities.class)
public class Main {
    public static void main(String[] args) throws Exception {
        SimpleAnalyzer analyzer = SimpleAnalyzer.builder().
                locale(Locale.ENGLISH).
                sentenceDetector(new BreakIteratorSentenceDetector(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPTokenizer(Locale.ENGLISH)).
                annotator(new SpaceAnnotator()).
                annotator(new ThreadSafeOpenNLPPosTagger(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPChunker(Locale.ENGLISH)).
                annotator(new PorterStemmer()).build();
        final Sentence sentence = analyzer.getSentence(args[0]);
        sentence.prettyPrint();

        Expression expr = new RealtimeExpressionFactory(analyzer).parse(args[1]);
        RealtimeSentenceMatcher matcher = RealtimeMatcherFactory.newIndexingMatcher(expr);
        RealtimeSentenceMatcher.Matches matches = matcher.match(sentence);

        if (matches == null) {
            System.out.println("No matches");
            return;
        }

        for (Match match: matches) {
            System.out.println();
            System.out.format("%s (%d-%d)\n", sentence.subSentence(match.getStart(), match.getEnd()), match.getStart(), match.getEnd());
            RealtimeSentenceMatcher.CaptureGroups cg = match.getCaptureGroups();
            if (cg != null)
                for (String id: cg.getIds())
                    System.out.format("  %s: %s (%d-%d)\n", id, sentence.subSentence(cg.getStart(id), cg.getEnd(id)), cg.getStart(id), cg.getEnd(id));
        }
    }
}
