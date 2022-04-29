/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.opennlp;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.analyzer.Tokenizer;
import org.bierner.matchbook.analyzer.UnitAnnotations;

/**
 * A thread-safe adapter of the OpenNLP Tokenizer.  This creates a new TokenizerME
 * each call to avoid threading issues, so if this is a concern and you are in a single-threaded
 * environment, use a non-thread-safe version.
 * @author gann
 */
public class ThreadSafeOpenNLPTokenizer implements SentenceAnnotator, Tokenizer {
    static {
        AnnotationType.registerAnnotator(ThreadSafeOpenNLPTokenizer.class, AnnotationType.TOKEN);
    }
    
    private TokenizerModel model;
    
    public ThreadSafeOpenNLPTokenizer(Locale locale) throws IOException {
        model = new TokenizerModel(OpenNLPModels.getModel(ThreadSafeOpenNLPSentenceDetector.class, locale, "-token.bin"));
    }
    
    @Override
    public List<String> tokenize(String text) {
        // Make a new TokenizerME each time for thread safety
        return Lists.newArrayList(new TokenizerME(model).tokenize(text));
    }
    
    @Override
    public void annotate(AnnotatableSentence sentence) {
        sentence.setAnnotations(AnnotationType.TOKEN, new UnitAnnotations<>(tokenize(sentence.getText())));
    }
}
