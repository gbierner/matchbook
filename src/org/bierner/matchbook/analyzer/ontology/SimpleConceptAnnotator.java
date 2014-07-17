/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.ontology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bierner.matchbook.analyzer.Analyzer;
import org.bierner.matchbook.analyzer.AnalyzingSentenceAnnotator;
import org.bierner.matchbook.analyzer.AnnotatableSentence;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.RangeAnnotations;
import org.bierner.matchbook.analyzer.SentenceAnnotator;
import org.bierner.matchbook.analyzer.ontology.Ontology.Concept;
import org.bierner.matchbook.analyzer.ontology.Ontology.Lemma;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;
import org.bierner.matchbook.matcher.realtime.RealtimeExpressionFactory;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher.Match;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher.Matches;
import org.bierner.matchbook.matcher.realtime.expr.Expression;
import org.bierner.matchbook.matcher.realtime.indexing.IndexingRealtimeSentenceMatcher;
import org.bierner.matchbook.matcher.realtime.indexing.IndexingRealtimeSentenceMatcher.IndexingRealtimeSentenceMatcherBuilder;

/**
 *
 * @author gann
 */
@ExtensionMethod(SentenceUtilities.class)
public class SimpleConceptAnnotator implements SentenceAnnotator, AnalyzingSentenceAnnotator {    
    static {
        AnnotationType.registerAnnotator(SimpleConceptAnnotator.class, AnnotationType.CONCEPT);
    }
    
    private IndexingRealtimeSentenceMatcher matcher;
    private List<Concept>                   concepts = new ArrayList<>();
    private Ontology                        ontology;
    
    public SimpleConceptAnnotator(String... names) {
        ontology = Ontologies.getOntology(names);
    }
    
    public SimpleConceptAnnotator(Ontology ontology) {
        this.ontology = ontology;
    }
    
    @Override
    public void init(Analyzer analyzer) {
        IndexingRealtimeSentenceMatcherBuilder builder     = IndexingRealtimeSentenceMatcher.builder();
        RealtimeExpressionFactory              exprFactory = new RealtimeExpressionFactory(analyzer);

        for (Concept concept : ontology.getEntities(Concept.class)) {
            List<Expression> exprs = new ArrayList<>();                    
            for (Lemma lemma : ontology.getRelation(concept, Ontology.Relation.SYNS)) 
                exprs.add(exprFactory.parse(lemma.getLemma()));
            builder.addExpression(exprFactory.or(exprs));
            concepts.add(concept);
        }
        
        matcher = builder.build();
    }
    
    
    @AllArgsConstructor
    public static class MatchAnnotation implements Annotation<Concept>, Comparable<MatchAnnotation> {
        private Concept concept;
        private Match   match;
        
        @Override public String  getId()    { return concept.getId(); }
        @Override public Concept getValue() { return concept; }
        @Override public int     getStart() { return match.getStart(); }
        @Override public int     getEnd()   { return match.getEnd(); }

        @Override
        public int compareTo(MatchAnnotation m) {
            return getStart() != m.getStart()? getStart() - m.getStart() : getEnd() - m.getEnd();
        }
    }
    
    @Override
    public void annotate(AnnotatableSentence sentence) {
        List<MatchAnnotation> annotations = new ArrayList<>();
        List<Matches>         exprMatches = matcher.matchIndividually(sentence);        
        
        for (int i = 0; i < exprMatches.size(); i++) {
            Concept concept = concepts.get(i);
            for (Match match : exprMatches.get(i))
                annotations.add(new MatchAnnotation(concept, match));
        }
        
        Collections.sort(annotations);
        sentence.setAnnotations(AnnotationType.CONCEPT, new RangeAnnotations<>(annotations));
    }

}
