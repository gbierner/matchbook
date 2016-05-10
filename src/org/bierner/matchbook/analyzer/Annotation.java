/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.analyzer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * A arbitrary sentence annotation.
 * @param <T>
 * @author gann
 */
public interface Annotation<T> {
    public static final String STEM             = "STEM";
    public static final String TOKEN            = "TOKEN";
    public static final String SPACE            = "SPACE";
    public static final String CONCEPT          = "CONCEPT";
    public static final String ANCESTOR_CONCEPT = "ANCESTOR_CONCEPT";
    public static final String POS              = "POS";
    public static final String CHUNK            = "CHUNK";
    public static final String BOUNDARY         = "BOUNDARY";
    public static final String BOUNDARY_START   = "START";
    public static final String BOUNDARY_END     = "END";

    /**
     * Returns a string based id for this annotation that can be used by a matcher
     * @return the annotation id
     */
    String getId();

    /**
     * Returns this annotation's value.  This is often the same as the id, but need not be.
     * For example, a concept annotation maybe have a Concept object as a value so that
     * ancestors may be easily obtained for matching purposes.
     * @return the annotation value
     */
    T getValue();

    /**
     * Returns the start position of the annotation, inclusive
     * @return the start position
     */
    int getStart();

    /**
     * Returns the end position of the annotation, exclusive
     * @return the end position
     */
    int getEnd();

    /**
     * An implementation of annotation for annotations of length one
     * @param <T> the type of the annotation's value
     */
    @AllArgsConstructor
    @ToString
    public static class UnitAnnotation<T> implements Annotation<T> {
        @Getter private String id;
        @Getter private T      value;
        private int pos;

        @Override public int getStart() { return pos; }
        @Override public int getEnd()   { return pos + 1; }
    }

    @AllArgsConstructor
    @ToString
    public static class SimpleAnnotation<T> implements Annotation<T> {
        @Getter private String id;
        @Getter private T      value;
        @Getter private int start;
        @Getter private int end;
    }
}
