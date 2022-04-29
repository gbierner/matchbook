/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.matcher.realtime.indexing;

import com.google.common.collect.Lists;
import gnu.trove.set.hash.TLongHashSet;
import java.util.ArrayList;
import java.util.List;
import lombok.Delegate;
import org.bierner.matchbook.matcher.realtime.indexing.CapturingVector.ArbitraryCapturingVector;
import org.bierner.matchbook.matcher.realtime.indexing.PackedVector.SimplePackedVector;

/**
 * Vector operations using vectors that store start and end positions as integers packed into a single long to save space
 * and simplify operations. An effort is made to be efficient about storing capture groups only if necessary.
 *
 * @author gann
 */
public class PackedVectorFactory implements VectorFactory {
    private static Vector EMPTY = new SimplePackedVector() {
        @Override public void add(int start, int end) { throw new UnsupportedOperationException("Cannot modify immutable empty vector"); }
    };

    @Override
    public Vector newInstance() {
        return new SimplePackedVector();
    }

    @Override
    public Vector emptyInstance() {
        return EMPTY;
    }

    @Override
    public Vector sequence(List<Vector> vectors) {
        Vector result = vectors.get(0);
        for (int i = 1; i < vectors.size(); i++) {
            result = sequence(result, vectors.get(i));
            if (result.length() == 0)
                break;
        }
        return result;
    }

    private Vector sequence(Vector a, Vector b) {
        ResultBuilder result = getResultBuilder(a, b);
        int pos = 0;
        for (int i = 0; i < a.length(); i++) {
            int start = a.getStart(i);
            int end = a.getEnd(i);
            while (pos < b.length() && b.getStart(pos) < start)
                pos++;

            if (pos == b.length())
                break;

            for (int j = pos; j < b.length(); j++) {
                int bStart = b.getStart(j);
                if (bStart == end) {
                    result.add(start, b.getEnd(j));
                    result.addSource(a, i);
                    result.addSource(b, j);
                } else if (bStart > end) {
                    break;
                }
            }
        }
        return result.getResult();
    }

    @Override
    public Vector or(List<Vector> vectors) {
        TLongHashSet result = new TLongHashSet();
        for (Vector v : vectors) {
            result.addAll(((PackedVector) v).asSet());
        }
        return getFinalResult(new SimplePackedVector(result), vectors);
    }

    @Override
    public Vector is(List<Vector> vectors) {
        TLongHashSet result = new TLongHashSet(((PackedVector) vectors.get(0)).asSet());
        for (int i = 1; i < vectors.size(); i++) {
            result.retainAll(((PackedVector) vectors.get(i)).asSet());
        }
        return getFinalResult(new SimplePackedVector(result), vectors);
    }

    @Override
    public Vector isnt(Vector a, Vector b) {
        TLongHashSet result = new TLongHashSet(((PackedVector) a).asSet());
        result.removeAll(((PackedVector) b).asSet());
        return getFinalResult(new SimplePackedVector(result), Lists.newArrayList(a, b));
    }

    @Override
    public Vector repeat(Vector v, int min, int max, int maxTokens) {
        Vector result;
        if (min == 0) {
            result = new SimplePackedVector();
            for (int i = 0; i < maxTokens + 1; i++)
                result.add(i, i);

        } else {
            List<Vector> start = new ArrayList<>(min);
            for (int i = 0; i < min; i++)
                start.add(v);
            result = sequence(start);
        }

        if (result.length() == 0)
            return result;

        List<Vector> vectors = new ArrayList<>();
        vectors.add(result);
        for (int i = min + 1; i <= max; i++) {
            result = sequence(result, v);

            if (result.length() == 0)
                break;

            vectors.add(result);
        }

        return or(vectors);
    }

    @Override
    public Vector with(Vector v, Vector with) {
        ResultBuilder result = getResultBuilder(v, with);

        int pos = 0;
        for (int i = 0; i < v.length(); i++) {
            int start = v.getStart(i), end = v.getEnd(i);

            while (pos < with.length() && with.getStart(pos) < start)
                    pos++;

            for (int j = pos; j < with.length() && with.getStart(j) < end; j++)
                if (with.getEnd(j) <= end) {
                    result.add(start, end);
                    result.addSource(v, i);
                    result.addSource(with, j);
                    break;
                }
        }

        return result.getResult();
    }

    @Override
    public Vector capture(Vector v, String label) {
        return new PackedVector.LabeledPackedVector((PackedVector) v, label);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helper methods for constructing the proper vector type based on
    // captures.
    ///////////////////////////////////////////////////////////////////////////
    // If any of the given sources are capturing, then a new capuring vector is
    // created.  This only works if the final vector contains entries taken
    // directly from the sources (eg OR or IS).  If entries are manipulated in
    // any way (eg sequences), use ResultBuilder instead.
    private Vector getFinalResult(Vector v, List<Vector> sources) {
        List<CapturingVector> captureSources = null;
        for (Vector source : sources)
            if (source instanceof CapturingVector) {
                if (captureSources == null)
                    captureSources = new ArrayList<>();
                captureSources.add((CapturingVector) source);
            }
        if (captureSources == null)
            return v;
        else
            return new PackedVector.SourceBasedPackedVector((PackedVector) v, captureSources);
    }

    // An abstracted vector result that will build a capturing or non-capturing
    // result vector depending on the source vectors.
    private interface ResultBuilder {
        void add(int start, int end);
        void addSource(Vector vector, int pos);
        Vector getResult();

        static class SimpleResultBuilder implements ResultBuilder {
            @Delegate Vector vector = new SimplePackedVector();
            @Override public void addSource(Vector vector, int pos) {  }
            @Override public Vector getResult() { return vector; }
        }

        static class CapturingResultBuilder implements ResultBuilder {
            ArbitraryCapturingVector vector = new ArbitraryCapturingVector();

            @Override public void add(int start, int end) { vector.add(start, end); }
            @Override public void addSource(Vector vector, int pos) { this.vector.addSource(vector, pos); }
            @Override public Vector getResult() { return vector; }
        }
    }

    // Returns the appropriate builder based on the source vectors.
    private ResultBuilder getResultBuilder(Vector... vectors) {
        for (Vector v : vectors)
            if (v instanceof CapturingVector)
                return new ResultBuilder.CapturingResultBuilder();
        return new ResultBuilder.SimpleResultBuilder();
    }

}
