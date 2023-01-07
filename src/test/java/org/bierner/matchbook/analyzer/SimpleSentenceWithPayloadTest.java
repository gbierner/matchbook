package org.bierner.matchbook.analyzer;

import lombok.NonNull;
import org.bierner.matchbook.analyzer.java.BreakIteratorSentenceDetector;
import org.junit.Test;

import java.util.Collections;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class SimpleSentenceWithPayloadTest {
     public static final AnnotationType<Integer> TYPE = new AnnotationType<>("INT", Integer.class);
    static {
        AnnotationType.registerAnnotator(IntAnnotator.class, TYPE);
    }

    @Test
    public void test() {

        SimpleAnalyzer analyzer = SimpleAnalyzer.builder().
                locale(Locale.ENGLISH).
                sentenceDetector(new BreakIteratorSentenceDetector(Locale.ENGLISH)).
                annotator(new IntAnnotator()).
                build();

        analyzer.setSentenceFactory((t, l, a) -> new IntSentence(t, l, a, 42));

        Sentence sentence = analyzer.getSentence("hello");
        assertEquals(((IntSentence) sentence).getPayload().intValue(), 42);
        assertEquals(sentence.getAnnotations(TYPE).get(0).getValue().intValue(), 42);
    }

    public static class IntSentence extends SimpleSentenceWithPayload<Integer> {
        public IntSentence(@NonNull String text, @NonNull Locale locale, @NonNull Analyzer analyzer, Integer payload) {
            super(text, locale, analyzer, payload);
        }
    }

    public static class IntAnnotator implements SentenceAnnotator {
        @Override
        public void annotate(AnnotatableSentence sentence) {
            IntSentence.SimpleAnnotatableWithPayload sent = (IntSentence.SimpleAnnotatableWithPayload) sentence;
            sentence.setAnnotations(TYPE, new UnitAnnotations<>(Collections.singletonList(sent.getPayload())));
        }

    }
}