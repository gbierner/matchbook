/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.matcher.realtime.indexing;

import java.util.List;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher;

/**
 * An implementation of the matchbook operations in vector form.  Any
 * implementation of this class can be used with the {@link IndexingRealtimeSentenceMatcher}
 * implementation of the {@link RealtimeSentenceMatcher}.
 * 
 * Each operation below corresponds to an operation in the matchbook grammar.  See
 * {@link PackedVectorFactory} for an example implementation.
 * 
 * @author gann
 */
public interface VectorFactory {

    Vector newInstance();

    Vector emptyInstance();

    Vector sequence(List<Vector> vectors);

    Vector or(List<Vector> vectors);

    Vector is(List<Vector> vectors);

    Vector isnt(Vector a, Vector b);
    
    Vector repeat(Vector v, int min, int max, int maxTokens);
    
    Vector with(Vector v, Vector with);
    
    Vector capture(Vector v, String label);
}
