/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.analyzer;

import java.util.List;

/**
 * Adapter for 3rd party sentence detectors.
 * @author gann
 */
public interface SentenceDetector {
    /**
     * Divides the given text into sentences.
     * @param text
     * @return a list of Strings, each one a sentence.
     */
    List<String> getSentences(String text);
}
