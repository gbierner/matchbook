/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.analyzer.util;

import java.io.PrintStream;
import org.bierner.matchbook.analyzer.Sentence;

/**
 * Prints some useful information about a sentence.
 * @author gann
 */
public interface SentencePrinter {
    /**
     * Prints information about a sentence.
     * @param sentence
     * @param out a PrintStream to print to
     */
    void print(Sentence sentence, PrintStream out);
}
