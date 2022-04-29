/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer;

import java.util.Locale;

/**
 * Determines the language of some given text.  This is used in the {@link MultiLingualAnalyzer} to 
 * analyzer with the appropriate locale.
 * 
 * @author gann
 */
public interface LanguageDetector {
    /**
     * Gets the language for the given text.
     * @param text 
     * @return a locale
     */
    Locale getLanguage(String text);
}
