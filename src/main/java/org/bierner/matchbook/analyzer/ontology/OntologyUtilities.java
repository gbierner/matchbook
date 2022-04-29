/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.ontology;

import java.util.HashSet;
import java.util.Set;
import org.bierner.matchbook.analyzer.ontology.Ontology.Concept;
import org.bierner.matchbook.analyzer.ontology.Ontology.Lemma;
import org.bierner.matchbook.analyzer.ontology.Ontology.Relation;

/**
 *
 * @author gann
 */
public class OntologyUtilities {
    public static Concept getConcept(Ontology o, String id) {
        return o.getEntity(Concept.class, id);
    }
    
    public static Lemma getLemma(Ontology o, String id) {
        return o.getEntity(Lemma.class, id);
    }
        
    public static Set<Concept> isa(Concept from) {
        Set<Concept> result = new HashSet<>();
        isa(from, result);
        return result;
    }
    
    private static void isa(Concept from, Set<Concept> accumulator) {
        for (Concept c : from.getOntology().getRelation(from, Relation.HYPE)) {
            accumulator.add(c);
            isa(c, accumulator);
        }
    }
}
