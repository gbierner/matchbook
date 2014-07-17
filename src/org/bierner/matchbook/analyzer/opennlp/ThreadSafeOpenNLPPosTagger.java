/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.opennlp;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import lombok.experimental.ExtensionMethod;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;
import org.bierner.matchbook.analyzer.UnitAnnotations;

/**
 * A thread-safe adapter of the OpenNLP part of speech tagger.  This creates a new POSTaggerME
 * each call to avoid threading issues, so if this is a concern and you are in a single-threaded
 * environment, use a non-thread-safe version.
 * @author gann
 */
@ExtensionMethod(SentenceUtilities.class)
public class ThreadSafeOpenNLPPosTagger implements SentenceAnnotator {
    static {
        AnnotationType.registerAnnotator(ThreadSafeOpenNLPPosTagger.class, AnnotationType.POS);
    }
    
    private POSModel model;

    public ThreadSafeOpenNLPPosTagger(Locale locale) throws IOException {
        model = new POSModel(OpenNLPModels.getModel(ThreadSafeOpenNLPPosTagger.class, locale, "-pos-maxent.bin"));
    }
    
    List<String> tag(List<String> tokens) {
        return Lists.newArrayList(new POSTaggerME(model).tag(tokens.toArray(new String[tokens.size()])));
    }
    
    @Override
    public void annotate(AnnotatableSentence sentence) {
        sentence.setAnnotations(AnnotationType.POS, new UnitAnnotations<>(tag(sentence.getTokens())));
    }
}
