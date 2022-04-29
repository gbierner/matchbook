/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.opennlp;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.bierner.matchbook.analyzer.SentenceDetector;

/**
 * A thread-safe adapter of the OpenNLP sentence detector.  This creates a new SentenceDetectorME
 * each call to avoid threading issues, so if this is a concern and you are in a single-threaded
 * environment, use a non-thread-safe version.
 * @author gann
 */
public class ThreadSafeOpenNLPSentenceDetector implements SentenceDetector {
    private SentenceModel model;

    public ThreadSafeOpenNLPSentenceDetector(Locale locale) throws IOException {
        model = new SentenceModel(OpenNLPModels.getModel(ThreadSafeOpenNLPSentenceDetector.class, locale, "-sent.bin"));
    }

    @Override
    public List<String> getSentences(String text) {
        return Lists.newArrayList(new SentenceDetectorME(model).sentDetect(text));
    }
    
}
