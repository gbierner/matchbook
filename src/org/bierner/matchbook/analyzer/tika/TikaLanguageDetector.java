/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.tika;

import java.util.Locale;
import org.apache.tika.language.LanguageIdentifier;
import org.bierner.matchbook.analyzer.LanguageDetector;

/**
 * An adapter of the Tika language detector.
 * @author gann
 */
public class TikaLanguageDetector implements LanguageDetector {

    @Override
    public Locale getLanguage(String text) {
        String lang = new LanguageIdentifier(text).getLanguage();
        return Locale.forLanguageTag(lang);
    }
    
    public static void main(String[] args) {
        System.out.println(new TikaLanguageDetector().getLanguage(args[0]));
    }
}
