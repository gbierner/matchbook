/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.analyzer.ontology;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bierner.matchbook.analyzer.ontology.Ontology.Concept;
import org.bierner.matchbook.analyzer.ontology.Ontology.Entity;
import org.bierner.matchbook.analyzer.ontology.Ontology.Lemma;
import org.bierner.matchbook.analyzer.ontology.Ontology.Relation;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author gann
 */
@RunWith(Parameterized.class)
@AllArgsConstructor
@ExtensionMethod(OntologyUtilities.class)
public class OntologyTest {
    
    private Ontology o;
    
    /**
     * Test of getRelation method, of class SimpleOntology.
     */
    @Test
    public void testGetRelation() {
        assertEquals(set(o.getConcept("animal")), o.getRelation(o.getEntity(Concept.class, "dog"), Relation.HYPE));
        assertEquals(set(o.getConcept("boxer"), o.getConcept("poodle")), o.getRelation(o.getConcept("dog"), Relation.HYPO));        
        assertEquals(set(o.getConcept("dog"), o.getConcept("cat")), o.getRelation(o.getConcept("animal"), Relation.HYPO));        
        assertEquals(set(o.getLemma("canine"), o.getLemma("dog")), o.getRelation(o.getConcept("dog"), Relation.SYNS));                
    }

    /**
     * Test of getEntities method, of class SimpleOntology.
     */
    @Test
    public void testGetEntities() {
        assertEquals(set(o.getConcept("animal"), o.getConcept("dog"), o.getConcept("poodle"), o.getConcept("boxer"), o.getConcept("cat")),
                     o.getEntities(Concept.class));
        
        assertEquals(set(o.getLemma("dog"), o.getLemma("canine"), o.getLemma("poodle"), o.getLemma("boxer"), o.getLemma("cat"), o.getLemma("feline")),
                     o.getEntities(Lemma.class));
    }

    private Set<Entity> set(Entity ... entities) {
        Set<Entity> set = new HashSet<>();
        for (Entity e : entities)
            set.add(e);
        return set;
    }
    
    
    @Parameters
    public static Collection<Object[]> getParameters() throws IOException, ClassNotFoundException {
        return Arrays.asList(new Object[][] {
            { getOntologyByBuilder() },
            { getOntologyByText() }            
        });
    }
    
    private static Ontology getOntologyByText() throws IOException, ClassNotFoundException {
        return SimpleOntology.builder().add(new InputStreamReader(OntologyTest.class.getResourceAsStream("animal.ont"))).build();
    }
    
    private static Ontology getOntologyByBuilder() {
        return SimpleOntology.builder().
                add("animal", "dog", Relation.HYPO).
                add("animal", "cat", Relation.HYPO).
                add("dog",    "poodle", Relation.HYPO).                
                add("dog",    "boxer", Relation.HYPO).
                add("dog",    "dog", Relation.SYNS).
                add("dog",    "canine", Relation.SYNS).                
                add("cat",    "cat", Relation.SYNS).                                
                add("cat",    "feline", Relation.SYNS).                                                
                add("boxer",  "boxer", Relation.SYNS).                
                add("poodle", "poodle", Relation.SYNS).
                build();
    }
}