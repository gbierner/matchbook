/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.matcher.realtime.indexing;

import com.google.common.collect.Sets;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Delegate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher.CaptureGroups;
import org.bierner.matchbook.matcher.realtime.indexing.PackedVector.SimplePackedVector;

/**
 * A vector that contains label information for at least some of its entries.  This is the result of a capture
 * expression in the matchbook grammar.
 * 
 * @author gann
 */
public interface CapturingVector extends Vector {
    // Returns the captured groups for the vector entry at the given position.
    CaptureGroups getCaptured(int pos);

    /**
     * A capturing vector that simply applies a label to all entries in its delegate.
     */
    @AllArgsConstructor
    public static class LabeledVector implements CapturingVector {
        @Delegate        
        private Vector delegate;
        private String label;
        
        @AllArgsConstructor
        private class LabeledCaptureGroups implements CaptureGroups {
            private int pos;
            @Override public Set<String> getIds() { return Collections.singleton(label); }
            @Override public int getStart(String id) { return delegate.getStart(pos); }
            @Override public int getEnd(String id) { return delegate.getEnd(pos); }
        }

        @Override
        public CaptureGroups getCaptured(final int pos) {
            if (delegate instanceof CapturingVector) {
                final CaptureGroups cg = ((CapturingVector) delegate).getCaptured(pos);
                if (cg == null) 
                    return new LabeledCaptureGroups(pos);
                else {
                    return new CaptureGroups() {
                        @Override
                        public Set<String> getIds() {
                            Set<String> ids = Sets.newHashSet(cg.getIds());
                            ids.add(label);
                            return ids;
                        }
                        
                        @Override
                        public int getStart(String id) {
                            if (id.equals(label))
                                return delegate.getStart(pos);
                            else
                                return cg.getStart(id);
                        }
                        
                        @Override
                        public int getEnd(String id) {
                            if (id.equals(label))
                                return delegate.getEnd(pos);
                            else
                                return cg.getEnd(id);
                        }
                    };
                }
            } else 
                return new LabeledCaptureGroups(pos);
        }
    }

    static class SimpleCaptureGroups implements CaptureGroups {
        private TObjectLongHashMap<String> groups = new TObjectLongHashMap<>();
        
        private void add(CaptureGroups cg) {
            for (String id : cg.getIds())
                groups.put(id, PackedVector.Util.pack(cg.getStart(id), cg.getEnd(id)));                     
        }
        
        @Override public Set<String> getIds() { return groups.keySet(); }
        @Override public int getStart(String id) { return PackedVector.Util.unpackStart(groups.get(id)); }
        @Override public int getEnd(String id) { return PackedVector.Util.unpackEnd(groups.get(id)); }
        
    }
    
    /**
     * A capturing vector whose entries are all contained in one or more of its source vectors.  Therefore, capture groups may be
     * constructed by looking up labels in the source vectors.
     */
    @RequiredArgsConstructor
    public static class SourceBasedCapturingVector implements CapturingVector {
        @Delegate @NonNull private Vector delegate;
        @NonNull private List<CapturingVector> sources;
        
        private TIntObjectHashMap<SimpleCaptureGroups> captured;

        private void init() {
            TLongIntHashMap delegatePositions = new TLongIntHashMap();
            for (int i = 0; i < delegate.length(); i++)
                delegatePositions.put(PackedVector.Util.pack(delegate.getStart(i), delegate.getEnd(i)), i);
            
            captured = new TIntObjectHashMap<>();
            for (CapturingVector source : sources)
                for (int i = 0; i < source.length(); i++) {
                    CaptureGroups cg = source.getCaptured(i);
                    if (cg != null && ! cg.getIds().isEmpty()) {
                        int pos = delegatePositions.get(PackedVector.Util.pack(source.getStart(i), source.getEnd(i)));
                        SimpleCaptureGroups scg  = captured.get(pos);
                        if (scg == null)
                            captured.put(pos, scg = new SimpleCaptureGroups());
                        scg.add(cg);
                    }
                }      
        }
        
        @Override
        public CaptureGroups getCaptured(int pos) {            
            if (captured == null)
                init();
            return captured.get(pos);
        }
    }
    
    /*
     * A capture vector that keeps track of what vectors and their positions wered used
     * to create the entries in the resulting vector.  This information is then used to
     * contruct new, combined capture information for the resulting vector on demand.
     */
    public static class ArbitraryCapturingVector implements CapturingVector, PackedVector {
        @Delegate @NonNull private PackedVector delegate = new SimplePackedVector();
        
        @AllArgsConstructor
        private static class Source {
            private CapturingVector vector;
            private int pos;            
        }
        
        private TIntObjectHashMap<List<Source>> captured = new TIntObjectHashMap<>();
        
        public void addSource(Vector vector, int pos) {
            if (vector instanceof CapturingVector) {
                List<Source> sources = captured.get(delegate.length() - 1);
                if (sources == null)
                    captured.put(delegate.length() - 1, sources = new ArrayList<>());
                sources.add(new Source((CapturingVector) vector, pos));
            }
        }
        
        @Override
        public CaptureGroups getCaptured(int pos) {            
            SimpleCaptureGroups cg = new SimpleCaptureGroups();
            for (Source source : captured.get(pos)) {
                CaptureGroups sub = source.vector.getCaptured(source.pos);                
                if (sub != null)
                    cg.add(sub);
            }
            
            return cg;
        }
    }
    
}
