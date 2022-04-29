/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.matcher.realtime.indexing;

import gnu.trove.TLongCollection;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.hash.TLongHashSet;
import java.util.List;
import lombok.Delegate;
import lombok.Getter;

/**
 * Vector implementations that store integer start and stop positions as packed into a single long value.
 * 
 * @author gann
 */
interface PackedVector extends Vector {
    TLongArrayList  asSortedList(); // Returns the entries in sorted order
    TLongHashSet    asSet();        // Returns entries as a set
    TLongCollection getEntries();   // Returns entries in whatever way is most efficient

    /*
     * Utility methods for packing and unpacking positions
     */
    static class Util {
        public static long pack(int start, int end) {
            return (long) start << 32 | end;
        }
        
        public static int unpackStart(long packed) {
            return (int) (packed  >> 32);
        }
        
        public static int unpackEnd(long packed) {
            return (int) (packed & 0xFFFFFFFF);
        }        
    }
    
    /*
     * A vector that has been labeled via a capture expression.  This is just a PackedVector wrapper
     * around the CapturingVector implementation.
     */
    static class LabeledPackedVector extends CapturingVector.LabeledVector implements PackedVector {
        @Delegate PackedVector delegate;
        
        public LabeledPackedVector(PackedVector delegate, String label) {
            super(delegate, label);
            this.delegate = delegate;
        }
    }
    
    /*
     * A vector whose entries are all contained in one or more of its source vectors.  This is just a PackedVector 
     * wrapper around the CapturingVector implementation.
     */
    static class SourceBasedPackedVector extends CapturingVector.SourceBasedCapturingVector implements PackedVector {
        @Delegate PackedVector delegate;
        
        public SourceBasedPackedVector(PackedVector delegate, List<CapturingVector> sources) {
            super(delegate, sources);
            this.delegate = delegate;
        }
    }
    
    /*
     * A non-capturing packed vector implementation.
     */
    static class SimplePackedVector implements PackedVector {
        @Getter TLongCollection entries;
        private boolean sorted = false;

        public SimplePackedVector(TLongHashSet entries) {
            this.entries = entries;
        }
        
        public SimplePackedVector() {
            this(new TLongHashSet());
        }
        
        @Override
        public TLongArrayList asSortedList() {
            if (!sorted) {
                entries = new TLongArrayList(entries);
                ((TLongArrayList) entries).sort();
                sorted = true;
            }
            return (TLongArrayList) entries;
        }
        
        @Override
        public TLongHashSet asSet() {
            if (sorted) {
                entries = new TLongHashSet(entries);
                sorted = false;
            }
            return (TLongHashSet) entries;
        }
        
        @Override
        public int length() {
            return entries.size();
        }
        
        @Override
        public void add(int start, int end) {
            entries.add(Util.pack(start, end));
        }

        @Override
        public int getStart(int pos) {
            return Util.unpackStart(asSortedList().get(pos));
        }
        
        @Override
        public int getEnd(int pos) {
            return Util.unpackEnd(asSortedList().get(pos));
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < length(); i++) {
                if (i > 0)
                    sb.append(",");
                sb.append("(").append(getStart(i)).append(",").append(getEnd(i)).append(")");
            }
            sb.append("]");
            return sb.toString();
        }
    }
}
