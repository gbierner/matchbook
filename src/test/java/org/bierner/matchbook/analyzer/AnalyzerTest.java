/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.analyzer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bierner.matchbook.analyzer.java.BreakIteratorSentenceDetector;
import org.bierner.matchbook.analyzer.tika.TikaLanguageDetector;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author gann
 */
@RunWith(Parameterized.class)
@AllArgsConstructor
@ExtensionMethod(SentenceUtilities.class)
public class AnalyzerTest {
    public static final AnnotationType<Locale> TYPE = new AnnotationType<>("TEST", Locale.class);
    static {
        AnnotationType.registerAnnotator(LocalizableTestAnnotator.class, TYPE);
        AnnotationType.registerAnnotator(TestAnnotator.class, TYPE);        
    }
    
    private Analyzer analyzer;
    
    /**
     * Test of getSentence method, of class SimpleAnalyzer.
     */
    @Test
    public void testGetSentence() {
        Sentence sentence = analyzer.getSentence("A b c. X y z.");
    }

    /**
     * Test of getSentences method, of class SimpleAnalyzer.
     */
    @Test
    public void testGetSentences() {
        List<Sentence> sentence = analyzer.getSentences("A b c. X y z.");
        assertEquals(2, sentence.size());
    }

    /**
     * Test of applyAnnotations method, of class SimpleAnalyzer.
     */
    @Test
    public void testApplyAnnotations() {
        Sentence sentence = analyzer.getSentence("A b c.");
        assertEquals(Locale.ENGLISH, sentence.getAnnotationValues(TYPE).get(0));
    }

    // TODO: Test annotator that doesn't take a Locale
    
    @Parameters
    public static Collection<Object[]> getParameters() throws IOException {
        return Arrays.asList(new Object[][] {
            // Builder constructed 
            { SimpleAnalyzer.builder().
                locale(Locale.ENGLISH).
                sentenceDetector(new BreakIteratorSentenceDetector(Locale.ENGLISH)).
                annotator(new LocalizableTestAnnotator(Locale.ENGLISH)).
                annotator(new ParameterizedAnnotator("a", "b", "c")).
                build() 
            }, 

            // String config constructed with localizable annotator
            {  SimpleAnalyzer.
                from("locale=en;" +
                     "sentenceDetector=org.bierner.matchbook.analyzer.java.BreakIteratorSentenceDetector;" +
                     "annotator=org.bierner.matchbook.analyzer.AnalyzerTest$ParameterizedAnnotator(a,b,c);" +
                     "annotator=org.bierner.matchbook.analyzer.LocalizableTestAnnotator")
            },

            // String config constructed without localizable annotator
            {  SimpleAnalyzer.
                from("locale=en;" +
                     "sentenceDetector=org.bierner.matchbook.analyzer.java.BreakIteratorSentenceDetector;" +
                     "annotator=org.bierner.matchbook.analyzer.TestAnnotator")
            },
            
            // Localized Multi-lingual analyzer with builder
            {  MultiLingualAnalyzer.builder().
                languageDetector(new TikaLanguageDetector()).
                sentenceDetector(BreakIteratorSentenceDetector.class).
                annotator(LocalizableTestAnnotator.class).
                build().
                localize(Locale.ENGLISH)
            },
            
            // Localized Multi-lingual analyzer with string config
            {  MultiLingualAnalyzer.
                from("languageDetector=org.bierner.matchbook.analyzer.tika.TikaLanguageDetector;" +
                     "sentenceDetector=org.bierner.matchbook.analyzer.java.BreakIteratorSentenceDetector;" +
                     "annotator=org.bierner.matchbook.analyzer.LocalizableTestAnnotator").
                localize(Locale.ENGLISH)
            },
            
        });
    }
    
    public static class ParameterizedAnnotator implements SentenceAnnotator {
        static {
            AnnotationType.registerAnnotator(ParameterizedAnnotator.class, AnnotationType.BOUNDARY); // doesn't matter, we're just making sure the from() all gets made correctly
        }
        
        public ParameterizedAnnotator(String... params) {
            assertArrayEquals(new String[] {"a", "b", "c"}, params);
        }
        
        @Override
        public void annotate(AnnotatableSentence sentence) {
        }
        
    }
}