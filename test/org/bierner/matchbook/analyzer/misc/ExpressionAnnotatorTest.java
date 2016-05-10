/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bierner.matchbook.analyzer.misc;

import java.util.Locale;
import lombok.Getter;
import org.bierner.matchbook.analyzer.Analyzer;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.Annotations;
import org.bierner.matchbook.analyzer.Sentence;
import org.bierner.matchbook.analyzer.SimpleAnalyzer;
import org.bierner.matchbook.analyzer.java.BreakIteratorSentenceDetector;
import org.bierner.matchbook.analyzer.misc.ExpressionAnnotator.SimpleExpressionAnnotator;
import org.bierner.matchbook.analyzer.opennlp.ThreadSafeOpenNLPPosTagger;
import org.bierner.matchbook.analyzer.opennlp.ThreadSafeOpenNLPTokenizer;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author gann
 */
public class ExpressionAnnotatorTest {

    public ExpressionAnnotatorTest() {
    }

    @Test
    public void testSimple() throws Exception {
        class Test extends SimpleExpressionAnnotator {
            public Test() {
                super("test", "\"test\"");
            }
        }
        Test test = new Test();

        Analyzer analyzer = SimpleAnalyzer.builder().
                locale(Locale.ENGLISH).
                sentenceDetector(new BreakIteratorSentenceDetector(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPTokenizer(Locale.ENGLISH)).
                annotator(test).
                build();

        Sentence sentence = analyzer.getSentence("this is a test");
        Annotations<?> annotations = sentence.getAnnotations(test.getAnnotationType());
        Assert.assertEquals(1, annotations.size());

        Annotation a = annotations.get(0);
        Assert.assertEquals("test", a.getId());
        Assert.assertEquals("test", a.getValue());
        Assert.assertEquals(3, a.getStart());
        Assert.assertEquals(4, a.getEnd());
    }

    @Test
    public void testComplex() throws Exception {
        class Test extends ExpressionAnnotator<Integer> {
            @Getter
            private final AnnotationType<Integer> annotationType;
            public Test() {
                super("number", "{CD}");
                annotationType = new AnnotationType<>(id, Integer.class);
                AnnotationType.registerAnnotator(getClass(), annotationType);
            }

            @Override
            protected Integer getValue(Sentence sentence, RealtimeSentenceMatcher.Match match) {
                return Integer.parseInt(SentenceUtilities.getTokens(sentence).get(match.getStart()));
            }
        }
        Test test = new Test();

        Analyzer analyzer = SimpleAnalyzer.builder().
                locale(Locale.ENGLISH).
                sentenceDetector(new BreakIteratorSentenceDetector(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPTokenizer(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPPosTagger(Locale.ENGLISH)).
                annotator(test).
                build();

        Sentence sentence = analyzer.getSentence("a 123 b");
        Annotations<Integer> annotations = sentence.getAnnotations(test.getAnnotationType());
        Assert.assertEquals(1, annotations.size());

        Annotation<Integer> a = annotations.get(0);
        Assert.assertEquals("number", a.getId());
        Assert.assertEquals(new Integer(123), a.getValue());
        Assert.assertEquals(1, a.getStart());
        Assert.assertEquals(2, a.getEnd());
    }

}
