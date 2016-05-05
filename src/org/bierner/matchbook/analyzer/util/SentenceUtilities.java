/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.util;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.Annotations;
import org.bierner.matchbook.analyzer.Sentence;
import org.bierner.matchbook.analyzer.UnitAnnotations;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher.Match;

/**
 * Some useful utility functions for sentences.  Use import static to easily use these
 * helper methods or lombok's ExtensionMethod (see the implementation of opennlp.PorterStemmer)
 *
 * @author gann
 */
public class SentenceUtilities {

    ///////////////////////////////////////////////////////////////////////////
    // Sentence info
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Gets the number of tokens in the given sentence
     * @param sentence
     * @return the number of tokens
     */
    public static int tokenCount(Sentence sentence) {
        return getTokens(sentence).size();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Annotation convenience methods
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Gets the tokens from a sentence without their associated annotations
     * @param sentence
     * @return a List of tokens
     */
    public static List<String> getTokens(Sentence sentence) {
        return getAnnotationValues(sentence, AnnotationType.TOKEN);
    }

    /**
     * Gets the stems from a sentence without their associated annotations
     * @param sentence
     * @return a List of stems
     */
    public static List<String> getStems(Sentence sentence) {
        return getAnnotationValues(sentence, AnnotationType.STEM);
    }

    /**
     * Gets the part of speech tags from a sentence without their associated annotations
     * @param sentence
     * @return a List of pos tags
     */
    public static List<String> getPOS(Sentence sentence) {
        return getAnnotationValues(sentence, AnnotationType.POS);
    }

    /**
     * Gets the values of an annotation from a sentence
     * @param <T> The value type of the annotation
     * @param sentence
     * @param type type of the values to extract from the sentence
     * @return a List of value of type T
     */
    public static <T> List<T> getAnnotationValues(Sentence sentence, AnnotationType<T> type) {
        Annotations<T> annotations = sentence.getAnnotations(type);
        if (annotations instanceof UnitAnnotations)
            return ((UnitAnnotations<T>) annotations).getValueList();
        else {
            List<T> result = new ArrayList<>();
            for (Annotation<T> a : annotations)
                result.add(a.getValue());
            return result;
        }
    }

    /**
     * Returns a portion of a sentence based on token positions
     * @param sentence
     * @param start
     * @param end
     * @return A string representing the sub sentence
     */
    public static String subSentence(Sentence sentence, int start, int end) {
        if (sentence.hasAnnotation(AnnotationType.SPACE)) {
            StringBuilder sb = new StringBuilder();
            List<Boolean> spaces = getAnnotationValues(sentence, AnnotationType.SPACE);
            List<String> tokens = getAnnotationValues(sentence, AnnotationType.TOKEN);
            for (int i = start; i < end; i++) {
                if (i != start && spaces.get(i))
                    sb.append(" ");
                sb.append(tokens.get(i));
            }
            return sb.toString();
        } else
            return Joiner.on(" ").join(getAnnotationValues(sentence, AnnotationType.TOKEN).subList(start, end));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Pretty printing
    ///////////////////////////////////////////////////////////////////////////
    private static SentencePrinter DEFAULT_PRINTER =
            new BasicSentenceAnnotationPrinter(AnnotationType.TOKEN, AnnotationType.STEM, AnnotationType.POS, AnnotationType.CHUNK);

    /**
     * Prints the tokens, stems, pos tags, and chunks of a sentence.
     * @param sentence
     */
    public static void prettyPrint(Sentence sentence) {
        DEFAULT_PRINTER.print(sentence, System.out);
    }

    /**
     * Prints the requested annotation types of a sentence.
     * @param sentence
     * @param types the annotation types of the sentence to print.
     */
    public static void prettyPrint(Sentence sentence, AnnotationType<?> ... types) {
        new BasicSentenceAnnotationPrinter(types).print(sentence, System.out);
    }

}
