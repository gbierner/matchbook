/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.matcher.realtime.indexing;

import org.bierner.matchbook.analyzer.Sentence;

/**
 * A basic vector interface as needed by the {@link IndexingRealtimeSentenceMatcher}.  All vectors 
 * are relative to a specific {@link Sentence}.  Entries are not necessarily required to be made in 
 * order (that depends on the Vector implementation), but a Vector is required to implement getStart
 * and getEnd such that entries are provided in sorted order (first by start position, then end position).
 * 
 * @author gann
 */
interface Vector {
    int length();                 // The number of entries in the vector
    int getStart(int pos);        // The sentence start position of the given entry
    int getEnd(int pos);          // The sentence end position of the given entry
    void add(int start, int end); // Add a new entry to the vector
}
