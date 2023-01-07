/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer;

import com.google.common.base.Splitter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An {@link Analyzer} implementation for sentences from a single language,
 * or, more specifically, a locale.
 * <p/>
 * This analyzer can be constructed using a builder but also through a string representation amenable to configuration settings.  The syntax
 * is <code>type=class;...</code> where the type is one of "locale", "sentenceDetector", or "annotator" and the class is
 * a fully qualified class name with either a no-argument constructor or a constructor that takes a locale parameter.  If the latter is present,
 * it is used given the previously specified locale (which is required in this case), otherwise the former is used.
 * @author gann
 */
@RequiredArgsConstructor
public class SimpleAnalyzer implements Analyzer {
    @NonNull private Locale locale;
    @NonNull private SentenceDetector sentenceDetector;
    @NonNull private Map<AnnotationType<?>, SentenceAnnotator> annotators;

    public interface SentenceFactory {
        Sentence getSentence(String text, Locale local, Analyzer analyzer);
    }

    private static final  SentenceFactory DEFAULT_SENTENCE_FACTORY = SimpleSentence::new;

    ///////////////////////////////////////////////////////////////////////////
    // Analyzer implementation
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public Sentence getSentence(String text) {
        return getSentence(text, DEFAULT_SENTENCE_FACTORY);
    }

    public Sentence getSentence(String text, SentenceFactory sentenceFactory) {
        return sentenceFactory.getSentence(text, locale, this);
    }

    @Override
    public List<Sentence> getSentences(String text) {
        return getSentences(text, DEFAULT_SENTENCE_FACTORY);
    }

    public List<Sentence> getSentences(String text, SentenceFactory sentenceFactory) {
        if (! this.locale.equals(locale))
            throw new IllegalArgumentException("Analyzer for " + this.locale + " cannot be used for " + locale);
        List<Sentence> result = new ArrayList<>();
        for (String sentence : sentenceDetector.getSentences(text))
            result.add(sentenceFactory.getSentence(sentence, locale, this));

        return result;
    }

    @Override
    public <T> void applyAnnotations(AnnotatableSentence sentence, AnnotationType<T> type) {
        SentenceAnnotator annotator = annotators.get(type);
        if (type == null)
            throw new UnsupportedOperationException("Unknown type");
        if (annotator == null)
            throw new UnsupportedOperationException("Analyzer does not support type: " + type.getName());
        else
            annotator.annotate(sentence);
    }

    @Override
    public <T> boolean provides(AnnotatableSentence sentence, AnnotationType<T> type) {
        return annotators.containsKey(type);
    }

   ///////////////////////////////////////////////////////////////////////////
    // Constructing
    ///////////////////////////////////////////////////////////////////////////
     /**
     * Creates a new builder.  This is the preferred programmatic way of instantiating this class.
     * @return a builder
     */
    public static SimpleAnalyzerBuilder builder() {
        return new SimpleAnalyzerBuilder();
    }

    /**
     * A Builder for a {@link SimpleAnalyzer}.  The builder is fluent so calls can be easily chained together.
     */
    public static class SimpleAnalyzerBuilder {
        private Locale locale;
        private SentenceDetector sentenceDetector;
        private Map<AnnotationType<?>, SentenceAnnotator> annotators;

        /**
         * Specifies the locale for the analyzer.
         * @param locale
         * @return this builder
         */
        public SimpleAnalyzerBuilder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        /**
         * Specifies the sentence detector for the analyzer.
         * @param sd a sentenceDetector
         * @return this builder
         */
        public SimpleAnalyzerBuilder sentenceDetector(SentenceDetector sd) {
            this.sentenceDetector = sd;
            return this;
        }

         /**
         * Specifies an annotator for the analyzer.  The annotator must be registered with
         * {@link AnnotationType#registerAnnotator(java.lang.Class, org.bierner.matchbook.analyzer.AnnotationType[])}
         * in order for the annotation types to be determined.
         * @param annotator
         * @return this builder
         */
        public SimpleAnalyzerBuilder annotator(SentenceAnnotator annotator) {
            if (annotators == null)
                annotators = new HashMap<>();
             for (AnnotationType<?> type : AnnotationType.forSentenceAnnotator(annotator.getClass()))
                annotators.put(type, annotator);
            return this;
        }

        /**
         * Builds the final {@link SimpleAnalyzer} object
         * @return a new instance of the analyzer
         */
        public SimpleAnalyzer build() {
            SimpleAnalyzer analyzer = new SimpleAnalyzer(locale, sentenceDetector, annotators);

            for (SentenceAnnotator a : annotators.values())
                if (a instanceof AnalyzingSentenceAnnotator)
                    ((AnalyzingSentenceAnnotator) a).init(analyzer);

            return analyzer;
        }
    }


    /**
     * Constructs an instance of this class from a string-based specification amenable to configuration (command line, file-based, etc).  Details are in
     * the class documentation.  The class must have either a non-argument constructor or a constructor with a single, Locale, parameter.  If it has
     * the latter, that will be used given a previously specified locale (which must have been provided).  Otherwise, the empty constructor is used.
     * @param spec
     * @return an instance of this class
     */
    public static SimpleAnalyzer from(String spec) {
        SimpleAnalyzerBuilder builder = builder();
        Matcher matcher = Pattern.compile("(locale|sentenceDetector|annotator)\\s*=\\s*(\\S+)\\s*").matcher("");
        Locale locale = null;
        for (String item : Splitter.on(';').trimResults().split(spec)) {
            matcher.reset(item);
            if (matcher.matches()) {
                try {
                    switch(matcher.group(1)) {
                        case "locale": {
                            locale = Locale.forLanguageTag(matcher.group(2));
                            builder.locale(locale);
                            break;
                        }
                        case "sentenceDetector": {
                            builder.sentenceDetector(newInstance(matcher.group(2), locale, SentenceDetector.class, null));
                            break;
                        }
                        case "annotator": {
                            String   str    = matcher.group(2);
                            String[] params = null;
                            int split = str.indexOf("(");
                            if (split > 0 && str.endsWith(")")) {
                                params = str.substring(split + 1, str.length() - 1).split(",");
                                str = str.substring(0, split);
                            }
                            builder.annotator(newInstance(str, locale, SentenceAnnotator.class, params));
                            break;
                        }
                    }
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    throw new IllegalArgumentException("Problem creating class for spec '" + item + "'", e);
                }
            } else {
                    throw new IllegalArgumentException("Problem parsing spec '" + item + "'");
            }
        }

        return builder.build();
    }

    private static <T> T newInstance(String className, Locale locale, Class<T> superClass, String[] params)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<? extends T> clazz = Class.forName(className).asSubclass(superClass);

        if (params != null) {
            try {
                Constructor<? extends T> constructor = clazz.getConstructor(String[].class);
                return constructor.newInstance((Object) params);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Parameterized configuration for class without appropriate constructor: " + className);
            }
        } else {
            try {
                Constructor<? extends T> constructor = clazz.getConstructor(Locale.class);
                return constructor.newInstance(locale);
            } catch (NoSuchMethodException ex) {
                return clazz.newInstance();
            }
        }
    }

}
