package org.bierner.matchbook.analyzer.misc;

import java.util.Collections;
import java.util.Locale;
import org.bierner.matchbook.analyzer.Analyzer;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.Annotations;
import org.bierner.matchbook.analyzer.Sentence;
import org.bierner.matchbook.analyzer.SimpleAnalyzer;
import org.bierner.matchbook.analyzer.java.BreakIteratorSentenceDetector;
import org.bierner.matchbook.analyzer.opennlp.ThreadSafeOpenNLPPosTagger;
import org.bierner.matchbook.analyzer.opennlp.ThreadSafeOpenNLPTokenizer;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author gann
 */
public class CollapseAnnotationAnnotatorTest {

    static CollapseAnnotationAnnotator annotator = new CollapseAnnotationAnnotator("NUMBERS", AnnotationType.POS, "CD", Collections.EMPTY_SET) {};
    static CollapseAnnotationAnnotator annotatorSkip = new CollapseAnnotationAnnotator("NUMBERS2", AnnotationType.POS, "CD", Collections.singleton(",")) {};

    @Test
    public void test() throws Exception {

        Analyzer analyzer = SimpleAnalyzer.builder().
                locale(Locale.ENGLISH).
                sentenceDetector(new BreakIteratorSentenceDetector(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPTokenizer(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPPosTagger(Locale.ENGLISH)).
                annotator(annotator).
                build();

        Sentence sentence = analyzer.getSentence("a 10 20 b");
        Annotations<?> annotations = sentence.getAnnotations(annotator.getAnnotationType());
        Assert.assertEquals(1, annotations.size());
        Assert.assertEquals(1, annotations.get(0).getStart());
        Assert.assertEquals(3, annotations.get(0).getEnd());
    }

    @Test
    public void test2() throws Exception {
        Analyzer analyzer = SimpleAnalyzer.builder().
                locale(Locale.ENGLISH).
                sentenceDetector(new BreakIteratorSentenceDetector(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPTokenizer(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPPosTagger(Locale.ENGLISH)).
                annotator(annotator).
                build();

        Sentence sentence = analyzer.getSentence("a 10 20 b 30 c");
        Annotations<?> annotations = sentence.getAnnotations(annotator.getAnnotationType());
        Assert.assertEquals(2, annotations.size());
        Assert.assertEquals(1, annotations.get(0).getStart());
        Assert.assertEquals(3, annotations.get(0).getEnd());
        Assert.assertEquals(4, annotations.get(1).getStart());
        Assert.assertEquals(5, annotations.get(1).getEnd());
    }

    @Test
    public void testCombine() throws Exception {
        Analyzer analyzer = SimpleAnalyzer.builder().
                locale(Locale.ENGLISH).
                sentenceDetector(new BreakIteratorSentenceDetector(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPTokenizer(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPPosTagger(Locale.ENGLISH)).
                annotator(annotatorSkip).
                build();

        Sentence sentence = analyzer.getSentence("a 10 20 , 30 c");
        Annotations<?> annotations = sentence.getAnnotations(annotatorSkip.getAnnotationType());
        Assert.assertEquals(1, annotations.size());
        Assert.assertEquals(1, annotations.get(0).getStart());
        Assert.assertEquals(5, annotations.get(0).getEnd());
    }

}
