/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bierner.matchbook.analyzer.misc;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.ExtensionMethod;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.analyzer.UnitAnnotations;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;

/**
 * Annotates each token with a boolean indicating whether or not whitespace occurred before that token
 * in the original string.
 * @author gann
 */
@ExtensionMethod(SentenceUtilities.class)
public class SpaceAnnotator implements SentenceAnnotator {

    static {
        AnnotationType.registerAnnotator(SpaceAnnotator.class, AnnotationType.SPACE);
    }

    @Override
    public void annotate(AnnotatableSentence sentence) {
        List<String> tokens = sentence.getTokens();

        String[] wsTokens = sentence.getText().split("\\s+");
        List<Boolean> spaces;
        spaces = new ArrayList(tokens.size());
        spaces.add(Boolean.FALSE);
        if (wsTokens.length == tokens.size()) {
            for (int i = 1; i < tokens.size(); i++)
                spaces.add(Boolean.TRUE);
        } else {
            String token = tokens.get(0);
            for (int i = 1, wsPos=0; i < tokens.size(); i++) {
                if (wsTokens[wsPos].equals(token)) {
                    spaces.add(Boolean.TRUE);
                    token = tokens.get(i);
                    wsPos++;
                } else {
                    spaces.add(Boolean.FALSE);
                    token += tokens.get(i);
                }
            }
        }
        sentence.setAnnotations(AnnotationType.SPACE, new UnitAnnotations<>(spaces));
    }

}
