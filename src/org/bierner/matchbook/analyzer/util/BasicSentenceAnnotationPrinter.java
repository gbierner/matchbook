/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.util;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import lombok.experimental.ExtensionMethod;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.analyzer.Annotation.UnitAnnotation;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.Sentence;
import org.bierner.matchbook.analyzer.SimpleAnalyzer;
import org.bierner.matchbook.analyzer.java.BreakIteratorSentenceDetector;
import org.bierner.matchbook.analyzer.opennlp.PorterStemmer;
import org.bierner.matchbook.analyzer.opennlp.ThreadSafeOpenNLPChunker;
import org.bierner.matchbook.analyzer.opennlp.ThreadSafeOpenNLPPosTagger;
import org.bierner.matchbook.analyzer.opennlp.ThreadSafeOpenNLPTokenizer;

/**
 * A simple sentence printer that prints the requested annotation types one per line
 * while indicating the span of the annotation with brackets and dashes.  Unit annotations 
 * (ie those with spans always of length 1) are simplified.  For example,
 * <pre>
 *               1      2      2      4
 * TOKEN:        token1 token2 token3 token4
 * ANNOTATION1:  A      B      C      D
 * ANNOTATION2:  [--------X----------][--Y--]
 * ANNOTATION3:  [-----a-----][------b------]
 * </pre>
 * <p/>
 * or more concretely
 * <p/>
 * <pre>
 *        0        1      2     3  4    5 
 * TOKEN: Everyone should learn to code . 
 * STEM:  Everyon  should learn to code . 
 * POS:   NN       MD     VB    TO VB   . 
 * CHUNK: [--NP---][--------VP---------]
 * </pre>
 * @author gann
 */
@ExtensionMethod(SentenceUtilities.class)
public class BasicSentenceAnnotationPrinter implements SentencePrinter {

    private AnnotationType<?>[] types;

    public BasicSentenceAnnotationPrinter(AnnotationType<?> ... types) {
        this.types = types;
    }
    
    @Override
    public void print(Sentence sentence, PrintStream out) {
        int[] spacing = new int[sentence.tokenCount()];
        int maxTypeLen = 0;
        
        // Calculate how much spacing we need to add between tokens to accommodate the other annotations
        for (AnnotationType<?> type : types) {
            maxTypeLen = Math.max(maxTypeLen, type.getName().length());
            for (Annotation<?> annotation : sentence.getAnnotations(type)) {
                int width = width(spacing, annotation.getStart(), annotation.getEnd());
                int size  = annotation.getId().length() + 1;

                if (! (annotation instanceof UnitAnnotation))
                    size += 1; // use two brackets instead of one space

                if (size > width)
                    spacing[annotation.getEnd() - 1] += size - width;
            }
        }
        
        // Print positions
        out.print(spaces(maxTypeLen + 2));
        for (int i = 0; i < spacing.length; i++)
            out.print(Strings.padEnd(i + "", spacing[i], ' '));
        out.println();
        
        // Print out annotations
        for (AnnotationType<?> type : types) {
            out.print(Strings.padEnd(type.getName() + ":", maxTypeLen + 2, ' '));
            int pos = 0;
            for (Annotation<?> annotation : sentence.getAnnotations(type)) {
                if (annotation.getStart() > pos)
                    out.print(spaces(width(spacing, pos, annotation.getStart())));
                
                int width = width(spacing, annotation.getStart(), annotation.getEnd());
                if (annotation instanceof UnitAnnotation)
                    out.print(Strings.padEnd(annotation.getId(), width, ' '));
                else {
                    int idLen   = annotation.getId().length();
                    int padding = (width - 2) - idLen;
                    String str = Strings.padStart(annotation.getId(), idLen + padding/2, '-');                    
                    str = Strings.padEnd(str, width - 2, '-');
                    out.print("[" + str + "]");
                }
                
                pos = annotation.getEnd();
            }
            
            out.println();
        }
    }
    
    private String spaces(int size) {
        return Strings.repeat(" ", size);
    }
    
    private int width( int[] spacing, int start, int end) {
        int width = 0;
        for (int i = start; i < end; i++)
            width += spacing[i];
        return width;
    }
    
    public static void main(String[] args) throws IOException {
        SimpleAnalyzer analyzer = SimpleAnalyzer.builder().
                locale(Locale.ENGLISH).
                sentenceDetector(new BreakIteratorSentenceDetector(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPTokenizer(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPPosTagger(Locale.ENGLISH)).
                annotator(new ThreadSafeOpenNLPChunker(Locale.ENGLISH)).
                annotator(new PorterStemmer()).build();        
        Sentence sentence = analyzer.getSentence(args[0]);
        sentence.prettyPrint();
    }
}
