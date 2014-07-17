/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.matcher.realtime.indexing;

import java.util.Locale;
import org.bierner.matchbook.analyzer.Analyzer;
import org.bierner.matchbook.analyzer.SimpleAnalyzer;
import org.bierner.matchbook.analyzer.java.BreakIteratorSentenceDetector;
import org.bierner.matchbook.analyzer.java.BreakIteratorTokenizer;
import org.bierner.matchbook.analyzer.misc.BoundaryAnnotator;
import org.bierner.matchbook.analyzer.ontology.AncestorAnnotator;
import org.bierner.matchbook.analyzer.ontology.Ontologies;
import org.bierner.matchbook.analyzer.ontology.SimpleConceptAnnotator;
import org.bierner.matchbook.analyzer.opennlp.PorterStemmer;
import org.bierner.matchbook.analyzer.opennlp.ThreadSafeOpenNLPChunker;
import org.bierner.matchbook.analyzer.opennlp.ThreadSafeOpenNLPPosTagger;
import org.bierner.matchbook.matcher.realtime.RealtimeExpressionFactory;
import org.bierner.matchbook.matcher.realtime.RealtimeMatcherFactory;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher;
import org.bierner.matchbook.matcher.realtime.expr.Expression;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gann
 */
public class IndexingRealtimeSentenceMatcherTest {
    private static Analyzer analyzer;
    
    static {
        try {
            Ontologies.setLoader(new Ontologies.ResourceLoadStrategy("org/bierner/matchbook/analyzer/ontology"));
            analyzer = SimpleAnalyzer.builder().
                    locale(Locale.ENGLISH).
                    sentenceDetector(new BreakIteratorSentenceDetector(Locale.ENGLISH)).
                    annotator(new BoundaryAnnotator()).
                    annotator(new ThreadSafeOpenNLPPosTagger(Locale.ENGLISH)).
                    annotator(new ThreadSafeOpenNLPChunker(Locale.ENGLISH)).                
                    annotator(new BreakIteratorTokenizer(Locale.ENGLISH)).
                    annotator(new SimpleConceptAnnotator("animal.ont")).
                    annotator(new AncestorAnnotator()).                    
                    annotator(new PorterStemmer()).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
        
    @Test
    public void testConcepts() {
        System.out.println("Test concepts");
        check(matcher("<dog>"), "my dog is great", 1,2);
        check(matcher("<dog>"), "my poodle is great", 1,2);        
        check(matcher("<<dog>>"), "my dog is great", 1,2);
        check(matcher("<<dog>>"), "my poodle is great");        
    }
    
    @Test
    public void testOr() {
        System.out.println("Test Or");                                        
        RealtimeSentenceMatcher matcher = matcher("a OR b");
        check(matcher, "a", 0, 1);
        check(matcher, "b", 0, 1);
        check(matcher, "a b", 0, 1,  1, 2);
        check(matcher, "b a", 0, 1,  1, 2);        
        check(matcher, "a x b", 0, 1,  2, 3);                
    }
    
    @Test
    public void testIsnt() {
        System.out.println("Test Isnt");                                
        RealtimeSentenceMatcher matcher = matcher("{DT} ISNT a");
        check(matcher, "a");
        check(matcher, "the", 0, 1);        
    }
    
    @Test
    public void testIs() {
        System.out.println("Test Is");                        
        RealtimeSentenceMatcher matcher = matcher("rat IS {VB}");
        check(matcher, "A rat likes to rat people out.", 4, 5);
    }
    
    @Test
    public void testSequence() {
        System.out.println("Test Sequence");                
        RealtimeSentenceMatcher matcher = matcher("a b");
        check(matcher, "x a b y", 1, 3);
        check(matcher, "a b a b", 0, 2,  2, 4);        
    }
    
    @Test 
    public void testAnnotations() {
        System.out.println("Test Annotations");        
        // Stem
        check(matcher("dog"), "dogs", 0, 1);
        check(matcher("dog"), "dog", 0, 1);        
        check(matcher("'dog'"), "dogs", 0, 1);
        check(matcher("''dog''"), "dogs", 0, 1); 
        
        // Token
        check(matcher("\"dog\""), "dogs");
        check(matcher("\"dog\""), "dog", 0, 1);
        
        // POS
        check(matcher("{NN}"), "dog", 0, 1);
        
        // Chunk
        check(matcher("[NP]"), "dog", 0, 1);        
    }
    
    @Test
    public void testRepeat() {
        System.out.println("Test Repeat");
        RealtimeSentenceMatcher matcher = matcher("a[2]");
        check(matcher, "a");
        check(matcher, "a b a");        
        check(matcher, "a a", 0, 2);        
        check(matcher, "x a a", 1, 3);                
        
        matcher = matcher("a[2:3]");
        check(matcher, "a");
        check(matcher, "a a", 0, 2);        
        check(matcher, "a a a", 0, 2,  0, 3,  1, 3);                
        check(matcher, "a a a a", 0, 2,  0, 3,  1, 3,  1, 4,  2, 4);                
        
        matcher = matcher("a[0:2] b");
        check(matcher, "b", 0, 1);        
        check(matcher, "a b", 0, 2,  1, 2);                
        check(matcher, "a a b", 0, 3,  1, 3,  2, 3);                        
        check(matcher, "a a a b", 1, 4,  2, 4,  3, 4);                                
        
        matcher = matcher("x a? y");
        check(matcher, "x y", 0, 2);        
        check(matcher, "x a y", 0, 3);                
        check(matcher, "x a a y");                        
    }
    
    @Test
    public void testWith() {
        System.out.println("Test With");
        check(matcher("CHUNK WITH \"really\""), "I really love my really big dog.", 1, 2,  3, 7);
        check(matcher("CHUNK:NP WITH \"really\""), "I really love my really big dog.", 3, 7);        
        check(matcher("CHUNK:NP WITH (\"really\", my)"), "I really love my really big dog.", 3, 7);                
        check(matcher("CHUNK WITH (\"really\", my)"), "I really love my really big dog.", 3, 7);                        
    }
    
    @Test
    public void testBoundaries() {
        System.out.println("Test Boundaries");
        check(matcher("START b"), "a a b b");
        check(matcher("START a"), "a a b b", 0, 1);        
        check(matcher("b END"), "a a b b", 3, 4);    
        check(matcher("a END"), "a a b b");
    }
    
    @Test
    public void testCapture() {
        System.out.println("Test Capture");
        RealtimeSentenceMatcher.Matches match = matcher("capture=a").match(analyzer.getSentence("a b a"));
        assertEquals(0, match.get(0).getCaptureGroups().getStart("capture"));
        assertEquals(1, match.get(0).getCaptureGroups().getEnd("capture"));        
        assertEquals(2, match.get(1).getCaptureGroups().getStart("capture"));
        assertEquals(3, match.get(1).getCaptureGroups().getEnd("capture"));

        match = matcher("(capture=a OR b) ISNT c").match(analyzer.getSentence("x a y"));
        assertEquals(1, match.size());
        assertEquals(1, match.get(0).getCaptureGroups().getStart("capture"));
        assertEquals(2, match.get(0).getCaptureGroups().getEnd("capture"));        
        
        match = matcher("(capture=a OR b) ISNT c").match(analyzer.getSentence("x a y b"));
        assertEquals(2, match.size());
        assertEquals(1, match.get(0).getCaptureGroups().getStart("capture"));
        assertEquals(2, match.get(0).getCaptureGroups().getEnd("capture"));        
        assertNull(match.get(1).getCaptureGroups());
        
        match = matcher("capture=(a OR b) ISNT c").match(analyzer.getSentence("x a y"));
        assertEquals(1, match.size());
        assertEquals(1, match.get(0).getCaptureGroups().getStart("capture"));
        assertEquals(2, match.get(0).getCaptureGroups().getEnd("capture"));       
        
        match = matcher("capture=(a OR b) ISNT c").match(analyzer.getSentence("x a y b"));
        assertEquals(2, match.size());
        assertEquals(1, match.get(0).getCaptureGroups().getStart("capture"));
        assertEquals(2, match.get(0).getCaptureGroups().getEnd("capture"));      
        assertEquals(3, match.get(1).getCaptureGroups().getStart("capture"));
        assertEquals(4, match.get(1).getCaptureGroups().getEnd("capture"));      
        
        match = matcher("(capture=a OR b) c").match(analyzer.getSentence("x a c b c y"));
        assertEquals(2, match.size());
        assertEquals(1, match.get(0).getCaptureGroups().getStart("capture"));
        assertEquals(2, match.get(0).getCaptureGroups().getEnd("capture"));        
        assertNull(match.get(1).getCaptureGroups());      
        
        match = matcher("a=CHUNK WITH (b=\"really\", c=my)").match(analyzer.getSentence("I really love my really big dog"));
        assertEquals(1, match.size());
        assertEquals(3, match.get(0).getCaptureGroups().getStart("a"));
        assertEquals(7, match.get(0).getCaptureGroups().getEnd("a"));        
        assertEquals(4, match.get(0).getCaptureGroups().getStart("b"));
        assertEquals(5, match.get(0).getCaptureGroups().getEnd("b"));        
        assertEquals(3, match.get(0).getCaptureGroups().getStart("c"));
        assertEquals(4, match.get(0).getCaptureGroups().getEnd("c"));        
    }
    
    private void check(RealtimeSentenceMatcher matcher, String sentence, int ... vals) {
        RealtimeSentenceMatcher.Matches matches = matcher.match(analyzer.getSentence(sentence));
        //System.out.println(matches);
        if (vals.length == 0)
            assertEquals(0, matches.size());
        else {
            assertEquals(vals.length / 2, matches.size());

            for (int i = 0; i < vals.length - 1; i += 2) {
                assertEquals(vals[i], matches.get(i/2).getStart());
                assertEquals(vals[i + 1], matches.get(i/2).getEnd()); 
            }
        }
    }
    
    private RealtimeSentenceMatcher matcher(String expr) {
        Expression expression = new RealtimeExpressionFactory(analyzer).parse(expr);
        return RealtimeMatcherFactory.newIndexingMatcher(expression);
    }
}