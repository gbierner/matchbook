package org.bierner.matchbook.analyzer.opennlp;

import lombok.experimental.ExtensionMethod;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@ExtensionMethod(SentenceUtilities.class)
public class ThreadSafeOpenNLPPersonNER implements SentenceAnnotator {
    static {
        AnnotationType.registerAnnotator(ThreadSafeOpenNLPPersonNER.class, AnnotationType.ENTITY);
    }

    private TokenNameFinderModel model;

    public ThreadSafeOpenNLPPersonNER(Locale locale) throws IOException {
        model = new TokenNameFinderModel(OpenNLPModels.getModel(ThreadSafeOpenNLPChunker.class, locale, "-ner-person.bin"));
    }

    Span[] findNames(List<String> tokens) {
        return new NameFinderME(model).find(tokens.toArray(new String[] {}));
    }

    @Override
    public void annotate(AnnotatableSentence sentence) {
        Span[] names = findNames(sentence.getTokens());
        sentence.setAnnotations(AnnotationType.ENTITY, new SpanAnnotations(names));
    }
}
