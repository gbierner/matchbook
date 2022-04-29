package org.bierner.matchbook.matcher.realtime;

import lombok.Data;
import lombok.Delegate;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher.Match;

/**
 * An annotation constructed out of a sentence match
 * @author gann
 * @param <T>
 */
@Data
public class MatchAnnotation<T> implements Annotation<T>, Comparable<MatchAnnotation> {
    @Delegate(excludes = Comparable.class)
    private final RealtimeSentenceMatcher.Match match;

    private final String id;
    private final T value;

     @Override
     public int compareTo(MatchAnnotation m) {
         return getStart() != m.getStart()? getStart() - m.getStart() : getEnd() - m.getEnd();
     }
}