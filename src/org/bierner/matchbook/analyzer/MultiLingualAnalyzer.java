/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer;

import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.bierner.matchbook.analyzer.SimpleAnalyzer.SimpleAnalyzerBuilder;

/**
 * An {@link Analyzer} composed of Locale-based factories so that it can adaptively apply the appropriate analysis depending on the language.  
 * There are two ways to localize this analyzer.  The first is to call the {@link #localize(java.util.Locale)} method to get a new Analyzer meant for
 * the requested locale.  Alternatively, calling {@link #getSentence(java.lang.String) } or {@link #getSentences(java.lang.String)} will use
 * the language detector to determine the language and use a localized version of this analyzer for further analysis.
 * <p/>
 * This analyzer can be constructed using a builder but also through a string representation amenable to configuration settings.  The syntax
 * is <code>type[.locale]=class;...</code> where the type is one of "languageDetector", "sentenceDetector", or "annotator" and the class is
 * a fully qualified class name with a constructor that takes a locale parameter.  If a locale is specified, a localized instance of the given class will
 * be used to analyze that sentences with that locale.  Not specifying a locale indicates that the annotator is a default to will apply when no 
 * more specific annotator is provided.
 * <p/>
 * Given a particular locale, the analyzer will first attempt to find an annotator matching both the language and country of the locale.  Failing that, it
 * will look for one matching just the language.  Failing that, the default is used, if it exists.  If no annotator is found for the requested type, a
 * {@link IllegalArgumentException} is thrown.
 * 
 * @author gann
 */
@RequiredArgsConstructor
public class MultiLingualAnalyzer implements Analyzer {
    private static Locale DEFAULT_LOCALE = new Locale("*");
    
    ///////////////////////////////////////////////////////////////////////////
    // Textual Analysis
    ///////////////////////////////////////////////////////////////////////////    
    /**
     * A factory that produces localized modules for the purpose of textual analysis.
     * @param <T> the type of the manufactured objects
     */
    public interface LocalizedFactory<T> {
        T newInstance(Locale locale);
    }
    
    /**
     * A factory that constructs localized modules from a class which is assumed to have a constructor with a single Locale parameter
     * @param <T> the type of the manufactured objects
     */
    @AllArgsConstructor
    public static class ClassLocalizedFactory<T> implements LocalizedFactory<T> {
        private Class<? extends T> clazz;
        @Override
        public T newInstance(Locale locale) {
            try {
                return clazz.getConstructor(Locale.class).newInstance(locale);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new IllegalArgumentException("Localized Factory class must have a constructor that takes a Locale parameter", e);
            }
        }
    }
    
    @NonNull private LanguageDetector languageDetector;   
    @NonNull private Map<Locale, LocalizedFactory<SentenceDetector>> sentenceDetectors;
    @NonNull private Table<AnnotationType<?>, Locale, LocalizedFactory<SentenceAnnotator>> annotators;        
    
    ///////////////////////////////////////////////////////////////////////////
    // Caches
    ///////////////////////////////////////////////////////////////////////////    
    @Value
    private static class LocalizedType {
        private Locale            locale;
        private AnnotationType<?> type;
    }    
    
    private <T> T getLocalized(Map<Locale, LocalizedFactory<T>> factories, Locale locale, String type) {
        LocalizedFactory<T> factory = factories.get(locale);             // Try the entire locale first
        if (factory == null)
            factory = factories.get(new Locale(locale.getLanguage()));   // If that fails, try just the language
        if (factory == null)
            factory = factories.get(DEFAULT_LOCALE);                     // Finally, try the default
        if (factory == null)
            throw new UnsupportedOperationException("Analyzer does not support " + type + " for locale " + locale);
        return factory.newInstance(locale);
    }    

    private LoadingCache<LocalizedType, SentenceAnnotator> annotatorCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<LocalizedType, SentenceAnnotator>() {
                @Override public SentenceAnnotator load(LocalizedType lType) {
                    return getLocalized(annotators.row(lType.getType()), lType.getLocale(), lType.getType().getName());
                }
            });
    
    private LoadingCache<Locale, SentenceDetector> sentenceDetectorCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<Locale, SentenceDetector>() {
                @Override public SentenceDetector load(Locale locale) {
                    return getLocalized(sentenceDetectors, locale, "SentenceDetector");
                }
            });
       
    ///////////////////////////////////////////////////////////////////////////
    // Local implementation
    ///////////////////////////////////////////////////////////////////////////    
    /**
     * Constructs a new analyzer composed of those modules (annotators, sentence detector, etc) that support
     * the requested locale.  This is useful when needing to support multiple languages, but the language
     * of the text is known before analysis.  The new analyzer is not cached, so the client is required
     * to do so to avoid creating a new Analyzer object each time.
     * 
     * @param locale
     * @return a localized analyzer
     */
    public Analyzer localize(Locale locale) {
        SimpleAnalyzerBuilder builder = SimpleAnalyzer.builder().
                locale(locale).
                sentenceDetector(sentenceDetectorCache.getUnchecked(locale));

        for (AnnotationType<?> type : annotators.rowKeySet()) {
            try {
                builder.annotator(annotatorCache.getUnchecked(new LocalizedType(locale, type)));
            } catch (UncheckedExecutionException ex) {
                // Ignore IllegalArgumentExceptions so that we just skip over annotation factories
                // that do not support this locale.  Rethrow anything else
                if (! (ex.getCause() instanceof IllegalArgumentException))
                    throw ex;
            }
        }
        return builder.build();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Analyzer implementation
    ///////////////////////////////////////////////////////////////////////////     
    @Override
    public Sentence getSentence(String text) {
        Locale locale = languageDetector.getLanguage(text);
        return new SimpleSentence(text, locale, this);
    }

    @Override
    public List<Sentence> getSentences(String text) {
        Locale locale = languageDetector.getLanguage(text);        
        List<Sentence> result = new ArrayList<>();
        for (String sentence : sentenceDetectorCache.getUnchecked(locale).getSentences(text))
            result.add(new SimpleSentence(sentence, locale, this));
        return result;
    }

    @Override
    public <T> void applyAnnotations(AnnotatableSentence sentence, AnnotationType<T> type) {
        annotatorCache.getUnchecked(new LocalizedType(sentence.getLocale(), type)).annotate(sentence);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructing
    ///////////////////////////////////////////////////////////////////////////    
    /**
     * Creates a new builder.  This is the preferred programmatic way of instantiating this class.
     * @return a builder
     */
    public static MultiLingualAnalyzerBuilder builder() {
        return new MultiLingualAnalyzerBuilder();
    }
    
    /**
     * A Builder for a {@link MultiLingualAnalyzer}.  Ultimately, all modules (except for the language detector)
     * are represented as {@link LocalizedFactory} objects, but there are convenience methods to build these
     * factories from classes or class names, assuming a constructor with a single Locale object.  The builder is fluent
     * so calls can be easily chained together.
     */
    public static class MultiLingualAnalyzerBuilder {
        private LanguageDetector languageDetector = null;  
        private Map<Locale, LocalizedFactory<SentenceDetector>> sentenceDetectors = null;
        private Table<AnnotationType<?>, Locale, LocalizedFactory<SentenceAnnotator>> annotators = null;

        /**
         * Builds the final {@link MultiLingualAnalyzer} object
         * @return a new instance of the analyzer
         */
        public MultiLingualAnalyzer build() {
            return new MultiLingualAnalyzer(languageDetector, sentenceDetectors, annotators);
        }
        
        /**
         * Specifies the language detector to use.
         * @param languageDetector
         * @return this builder
         */
        public MultiLingualAnalyzerBuilder languageDetector(LanguageDetector languageDetector) {
            this.languageDetector = languageDetector;
            return this;
        }

        /**
         * Specifies a sentence detector for the given locale.
         * @param locale
         * @param factory
         * @return this builder
         */
        public MultiLingualAnalyzerBuilder sentenceDetector(Locale locale, LocalizedFactory<SentenceDetector> factory) {
            if (sentenceDetectors == null)
                sentenceDetectors = new HashMap<>();
            sentenceDetectors.put(locale, factory);
            return this;
        }
        
        /**
         * Specifies a default sentence detector.
         * @param factory
         * @return this builder
         */
        public MultiLingualAnalyzerBuilder sentenceDetector(LocalizedFactory<SentenceDetector> factory) {
            return sentenceDetector(DEFAULT_LOCALE, factory);
        }
        
        /**
         * Specifies a sentence detector for the given locale
         * @param locale
         * @param clazz
         * @return this builder
         */
        public MultiLingualAnalyzerBuilder sentenceDetector(Locale locale, Class<? extends SentenceDetector> clazz) {
            return sentenceDetector(locale, new ClassLocalizedFactory<>(clazz));
        }
        
        /**
         * Specifies a default sentence detector.
         * @param clazz
         * @return this builder
         */
        public MultiLingualAnalyzerBuilder sentenceDetector(Class<? extends SentenceDetector> clazz) {
            return sentenceDetector(DEFAULT_LOCALE, new ClassLocalizedFactory<>(clazz));
        }
        
        /**
         * Specifies a sentence detector for the given locale
         * @param locale 
         * @param className
         * @return this builder
         * @throws ClassNotFoundException  
         */
        public MultiLingualAnalyzerBuilder sentenceDetector(Locale locale, String className) throws ClassNotFoundException {
            return sentenceDetector(locale, Class.forName(className).asSubclass(SentenceDetector.class));
        }

        /**
         * Specifies an annotator for the given locale and types.  This could be done without specifying the types but would
         * require instantiating an annotator from the factory, which we'd like to avoid.  The class-based convenience
         * methods do not require specifying the types.
         * @param types the annotation types supplied by the factory's annotator instances.
         * @param locale
         * @param factory
         * @return this builder
         */
        public MultiLingualAnalyzerBuilder annotator(Collection<? extends AnnotationType<?>> types, Locale locale, LocalizedFactory<SentenceAnnotator> factory) {
            if (annotators == null)                
                annotators = HashBasedTable.create();
            for (AnnotationType<?> type : types) 
                annotators.put(type, locale, factory);
            return this;
        }
        
        /**
         * Specifies a default annotator for the given types.  This could be done without specifying the types but would
         * require instantiating an annotator from the factory, which we'd like to avoid.  The class-based convenience
         * methods do not require specifying the types.
         * @param types
         * @param factory
         * @return this builder
         */
        public MultiLingualAnalyzerBuilder annotator(Collection<? extends AnnotationType<?>> types, LocalizedFactory<SentenceAnnotator> factory) {
            return annotator(types, DEFAULT_LOCALE, factory);
        }
        
        /**
         * Specifies an annotator for the given locale.  The class must be registered with {@link AnnotationType#registerAnnotator(java.lang.Class, org.bierner.matchbook.analyzer.AnnotationType[])}
         * in order for the annotation types to be determined.
         * @param locale
         * @param clazz
         * @return this builder
         */
        public MultiLingualAnalyzerBuilder annotator(Locale locale, Class<? extends SentenceAnnotator> clazz) {
            return annotator(AnnotationType.forSentenceAnnotator(clazz), locale, new ClassLocalizedFactory<>(clazz));
        }
        
        /**
         * Specifies a default annotator.  The class must be registered with {@link AnnotationType#registerAnnotator(java.lang.Class, org.bierner.matchbook.analyzer.AnnotationType[])}
         * in order for the annotation types to be determined.
         * @param clazz
         * @return this builder
         */
        public MultiLingualAnalyzerBuilder annotator(Class<? extends SentenceAnnotator> clazz) {
            return annotator(AnnotationType.forSentenceAnnotator(clazz), DEFAULT_LOCALE, new ClassLocalizedFactory<>(clazz));
        }
        
        /**
         * Specifies an annotator for the given locale.  The class must be registered with {@link AnnotationType#registerAnnotator(java.lang.Class, org.bierner.matchbook.analyzer.AnnotationType[])}
         * in order for the annotation types to be determined.
         * @param locale
         * @param className
         * @return this builder
         * @throws ClassNotFoundException  
         */
        public MultiLingualAnalyzerBuilder annotator(Locale locale, String className) throws ClassNotFoundException {
            return annotator(locale, Class.forName(className).asSubclass(SentenceAnnotator.class));
        }
    }
    
    /**
     * Constructs an instance of this class from a string-based specification amenable to configuration (command line, file-based, etc).  Details are in
     * the class documentation.
     * @param spec 
     * @return an instance of this class
     */
    public static MultiLingualAnalyzer from(String spec) {
        MultiLingualAnalyzerBuilder builder = builder();
        Matcher matcher = Pattern.compile("(languageDetector|sentenceDetector|annotator)(?:\\.([a-z-]+))?\\s*=\\s*(\\S+)\\s*").matcher("");
        for (String item : Splitter.on(';').trimResults().split(spec)) {
            matcher.reset(item);
            if (matcher.matches()) {
                try {
                    switch(matcher.group(1)) {
                        case "languageDetector": {
                            builder.languageDetector(Class.forName(matcher.group(3)).asSubclass(LanguageDetector.class).newInstance()); 
                            break;
                        }
                        case "sentenceDetector": {
                            String localeStr = matcher.group(2);
                            Locale locale = localeStr == null? DEFAULT_LOCALE: Locale.forLanguageTag(localeStr);
                            builder.sentenceDetector(locale, matcher.group(3));
                            break;
                        }
                        case "annotator": {
                            String localeStr = matcher.group(2);
                            Locale locale = localeStr == null? DEFAULT_LOCALE: Locale.forLanguageTag(localeStr);
                            builder.annotator(locale, matcher.group(3));
                            break;
                        }
                    }
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    throw new IllegalArgumentException("Problem creating class for spec '" + item + "'", e);                    
                }
            } else {
                    throw new IllegalArgumentException("Problem parsing spec '" + item + "'");
            }
        }
        
        return builder.build();
    }
        
}
