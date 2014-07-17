/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.matcher.realtime;

import com.google.common.collect.Range;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bierner.matchbook.analyzer.Sentence;
import org.bierner.matchbook.matcher.realtime.expr.Expression;

/**
 * A RealTimeSentenceMatcher matches individual sentences on-the-fly.  In other words, there is
 * no prior knowledge of the input text, so no preprocessing can take place.  This does not preclude an
 * index-based approach, for example, but the indexing must take place at the time of the matching process
 * and not off-line.
 * 
 * Although it is not specified in the interface, it's expected that an implementation be built given one
 * or more {@link Expression}s to match.
 * 
 * @author gann
 */
public interface RealtimeSentenceMatcher {

    /**
     * Capture groups are the result of successfully matching named expressions, as in
     * <code>name=expression</code>.  From here, one can obtain the matched names and 
     * the positions of the matches in the sentence.
     */
    public interface CaptureGroups {
        public static class Utilities {
            public static Map<String, Range<Integer>> asMap(CaptureGroups cg) {
                Map<String, Range<Integer>> result = new HashMap<>();
                for (String id : cg.getIds())
                    result.put(id, Range.closedOpen(cg.getStart(id), cg.getEnd(id)));
                return result;
            }
        }
        
        /**
         * Returns the names of the captured positions.
         * @return a {@link Set} of names
         */
        Set<String> getIds();
        
        /**
         * Returns the starting position of requested name
         * @param id
         * @return the starting position
         */
        int getStart(String id);

        /**
         * Returns the ending position of requested name
         * @param id
         * @return the ending position
         */
        int getEnd(String id);        
    }
    
    /**
     * A Match is a single instance of an expression matching within a sentence.
     */
    public interface Match {
        /**
         * Returns the starting position of the match.
         * @return the start position
         */
        int getStart();
        
        /**
         * Returns the ending position of the match.
         * @return the end position
         */
        int getEnd();

        /**
         * Returns any captured sub-matches within the match.
         * @return a CaptureGroups object or null if nothing was captured.
         */
        CaptureGroups getCaptureGroups();
    }
    
    /**
     * An collection of matches ordered by match position.  Sorting is first by start position
     * and then by end position of the match.
     */
    public interface Matches extends Iterable<Match> {
        /**
         * Returns the number of matches
         * @return the number of matches
         */
        int size();
        
        /**
         * Returns the match for the given position
         * @param pos
         * @return a {@link Match} object
         */
        Match get(int pos);
    }
    
    /**
     * Returns the matches within a given sentence.
     * @param sentence
     * @return a {@link Matches} object.
     */
    Matches match(Sentence sentence);
}
