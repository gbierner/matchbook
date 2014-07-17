/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.opennlp;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import lombok.experimental.ExtensionMethod;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.util.Span;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;

/**
 * A thread-safe adapter of the OpenNLP chunker.  This creates a new ChunkerME each call
 * to avoid threading issues, so if this is a concern and you are in a single-threaded
 * environment, use a non-thread-safe version.
 * 
 * @author gann
 */
@ExtensionMethod(SentenceUtilities.class)
public class ThreadSafeOpenNLPChunker implements SentenceAnnotator {
    static {
        AnnotationType.registerAnnotator(ThreadSafeOpenNLPChunker.class, AnnotationType.CHUNK);
    }
    
    private ChunkerModel model;

    public ThreadSafeOpenNLPChunker(Locale locale) throws IOException {
        model = new ChunkerModel(OpenNLPModels.getModel(ThreadSafeOpenNLPChunker.class, locale, "-chunker.bin"));
    }
    
    Span[] chunk(List<String> tokens, List<String> pos) {
        return new ChunkerME(model).chunkAsSpans(tokens.toArray(new String[tokens.size()]),
                                                 pos.toArray(new String[tokens.size()]));
    }
    
    @Override
    public void annotate(AnnotatableSentence sentence) {
        Span[] chunks = chunk(sentence.getTokens(), sentence.getPOS());
        sentence.setAnnotations(AnnotationType.CHUNK, new SpanAnnotations(chunks));
    }
}
