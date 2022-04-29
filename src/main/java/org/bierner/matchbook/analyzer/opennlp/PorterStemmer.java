/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.opennlp;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.ExtensionMethod;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;
import org.bierner.matchbook.analyzer.UnitAnnotations;

/**
 * A wrapper around OpenNLP's Porter stemmer implementation.
 * @author gann
 */
@ExtensionMethod(SentenceUtilities.class)
public class PorterStemmer implements SentenceAnnotator {
    static {
        AnnotationType.registerAnnotator(PorterStemmer.class, AnnotationType.STEM);
    }
    
    @Override
    public void annotate(AnnotatableSentence sentence) {
        opennlp.tools.stemmer.PorterStemmer stemmer = new opennlp.tools.stemmer.PorterStemmer();
        List<String> tokens = sentence.getTokens();
        List<String> stems = new ArrayList<>(tokens.size()); 
        for (String token: tokens)
            stems.add(stemmer.stem(token).toLowerCase());  // Sorry, in my opinion, stems should be downcased
        sentence.setAnnotations(AnnotationType.STEM, new UnitAnnotations<>(stems));
    }
}
