/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer;

import java.util.List;

/**
 * An adapter for 3rd party tokenizers.
 * @author gann
 */
public interface Tokenizer {
    /**
     * Returns a list of tokens from a text string.
     * @param text to tokenize
     * @return a list of tokens
     */
    List<String> tokenize(String text);
}
