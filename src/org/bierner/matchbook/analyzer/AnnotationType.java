/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.analyzer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Value;
import org.bierner.matchbook.analyzer.ontology.Ontology.Concept;

/**
 * An annotation type used to identify the information one wishes to extract from a sentence.  All new types should be registered with
 * this class using the {@link #registerAnnotator(java.lang.Class, org.bierner.matchbook.analyzer.AnnotationType[])} static method.
 * @param <T>
 * @author gann
 */
@Value
public class AnnotationType<T> {
    private static Map<String, AnnotationType<?>> TYPES = new HashMap<>();
    private static Multimap<Class<? extends SentenceAnnotator>, AnnotationType<?>> ANNOTATORS = HashMultimap.create();

    public static final AnnotationType<String>  STEM     = new AnnotationType<>(Annotation.STEM, String.class);
    public static final AnnotationType<String>  TOKEN    = new AnnotationType<>(Annotation.TOKEN, String.class);
    public static final AnnotationType<Boolean> SPACE    = new AnnotationType<>(Annotation.SPACE, Boolean.class);
    public static final AnnotationType<String>  POS      = new AnnotationType<>(Annotation.POS, String.class);
    public static final AnnotationType<String>  CHUNK    = new AnnotationType<>(Annotation.CHUNK, String.class);
    public static final AnnotationType<String>  BOUNDARY = new AnnotationType<>(Annotation.BOUNDARY, String.class);
    public static final AnnotationType<String>  SENTENCE = new AnnotationType<>(Annotation.SENTENCE, String.class);
    public static final AnnotationType<Concept> CONCEPT  = new AnnotationType<>(Annotation.CONCEPT, Concept.class);
    public static final AnnotationType<Concept> ANCESTOR_CONCEPT  = new AnnotationType<>(Annotation.ANCESTOR_CONCEPT, Concept.class);

    private String   name;
    private Class<T> valueClass;


    /**
     * Creates a new annotation type with the given name and value type.
     * @param name the name of the annotation
     * @param valueClass the value type of the annotation
     */
    public AnnotationType(String name, Class<T> valueClass) {
        if (TYPES.containsKey(name))
            throw new IllegalArgumentException("AnnotationType '" + name + "' already exists");

        this.name = name;
        this.valueClass = valueClass;
        TYPES.put(name, this);
    }

    /**
     * Returns the type for the given name
     * @param name
     * @return an annotation type
     */
    public static AnnotationType<?> getType(String name) {
        return TYPES.get(name);
    }

    /**
     * Registers a new annotator for the given types.  This is typically done in the static block of the annotator and is
     * used when building analyzers.
     * @param clazz the annotator class
     * @param types the annotation types provided by the annotator.
     */
    public static void registerAnnotator(Class<? extends SentenceAnnotator> clazz, AnnotationType<?>... types) {
        for (AnnotationType<?> type : types)
            ANNOTATORS.put(clazz, type);
    }

    /**
     * Returns the annotation types provided by the requested annotator class. This is typically used by analyzers.
     * @param clazz the annotator class
     * @return a collection of annotation types provided by the requested annotator.
     * @throws IllegalArgumentException when an unknown annotator is requested.
     */
    public static Collection<AnnotationType<?>> forSentenceAnnotator(Class<? extends SentenceAnnotator> clazz) {
        if (! ANNOTATORS.containsKey(clazz))
            throw new IllegalArgumentException("Attempt to get types for unregistered annotator: " + clazz.getName());

        return ANNOTATORS.get(clazz);
    }
}
