/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.analyzer;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bierner.matchbook.analyzer.java.BreakIteratorSentenceDetector;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author gann
 */
@ExtensionMethod(SentenceUtilities.class)
public class MultiLingualAnalyzerTest {
    public static final AnnotationType<String> MULTI_TYPE = new AnnotationType<>("MultiTest", String.class);
    
    static {
        AnnotationType.registerAnnotator(GenericTestAnnotator.class, MULTI_TYPE);
        AnnotationType.registerAnnotator(LocalizableTestAnnotator.class, AnalyzerTest.TYPE);
    }

    @AllArgsConstructor
    public static class GenericTestAnnotator implements SentenceAnnotator {
        private String text;
        @Override
        public void annotate(AnnotatableSentence sentence) {
             sentence.setAnnotations(MULTI_TYPE, new UnitAnnotations<>(Lists.newArrayList(text)));
        }
    }
    
    public static class TestLanguageDetector implements LanguageDetector {
        @Override
        public Locale getLanguage(String text) {
            return Locale.forLanguageTag(text.substring(0, text.indexOf(":")));
        }
    }
    
    @AllArgsConstructor
    public static class TestLocalizedFactory<T> implements MultiLingualAnalyzer.LocalizedFactory<T> {
        private T t;
        @Override
        public T newInstance(Locale locale) {
            return t;
        }
    }
    
    public static class NullSentenceDetector implements SentenceDetector {
        @Override
        public List<String> getSentences(String text) {
            return Lists.newArrayList(text);
        }
    }

    private static String ENGLISH = "GENERAL ENGLISH";
    private static String US      = "US ENGLISH";    
    private static String DEFAULT = "DEFAULT";        
    
    public MultiLingualAnalyzer newAnalyzer() {
        return MultiLingualAnalyzer.builder().
                languageDetector(new TestLanguageDetector()).
                sentenceDetector(BreakIteratorSentenceDetector.class).
                sentenceDetector(Locale.ENGLISH, new TestLocalizedFactory<SentenceDetector>(new NullSentenceDetector())).
                annotator(Collections.singleton(MULTI_TYPE), new TestLocalizedFactory<SentenceAnnotator>(new GenericTestAnnotator(DEFAULT))).
                annotator(Collections.singleton(MULTI_TYPE), Locale.ENGLISH, new TestLocalizedFactory<SentenceAnnotator>(new GenericTestAnnotator(ENGLISH))).
                annotator(Collections.singleton(MULTI_TYPE), Locale.forLanguageTag("en-US"), new TestLocalizedFactory<SentenceAnnotator>(new GenericTestAnnotator(US))).                
                build();
        
    }
    
    @Test
    public void testSentenceDectorLanguage() {
        assertEquals(1, newAnalyzer().getSentences("en: First sentence.  Second sentence.").size());
    }
    
    @Test
    public void testSentenceDetectorDefault() {
        assertEquals(2, newAnalyzer().getSentences("de: First sentence.  Second sentence.").size());        
    }
    
    @Test
    public void testAnnotatorLanguageCountry() {
        Sentence sentence = newAnalyzer().getSentence("en-US: A b c");
        assertEquals(US, sentence.getAnnotationValues(MULTI_TYPE).get(0));
    }
    
    @Test
    public void testAnnotatorLanguage() {
        Sentence sentence = newAnalyzer().getSentence("en: A b c");
        assertEquals(ENGLISH, sentence.getAnnotationValues(MULTI_TYPE).get(0));
    }
    
    @Test
    public void testAnnotatorDefault() {
        Sentence sentence = newAnalyzer().getSentence("de: A b c");
        assertEquals(DEFAULT, sentence.getAnnotationValues(MULTI_TYPE).get(0));
    }
}