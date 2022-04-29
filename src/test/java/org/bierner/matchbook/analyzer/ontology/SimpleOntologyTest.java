/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.ontology;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import lombok.experimental.ExtensionMethod;
import org.bierner.matchbook.analyzer.ontology.Ontology.Entity;
import org.bierner.matchbook.analyzer.ontology.Ontology.Relation;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gann
 */
@ExtensionMethod(OntologyUtilities.class)
public class SimpleOntologyTest {
    @Test
    public void testNewRelations() throws IOException, ClassNotFoundException {
        SimpleOntology o = SimpleOntology.builder().add(new InputStreamReader(OntologyTest.class.getResourceAsStream("car.ont"))).build();
        assertEquals(set(o.getConcept("car")), o.getRelation(o.getConcept("wheel"), Relation.getRelation("PART_OF")));
        assertEquals(set(o.getConcept("wheel"), o.getConcept("engine")), o.getRelation(o.getConcept("car"), Relation.getRelation("CONTAINS")));        
    }
    
     private Set<Entity> set(Entity ... entities) {
        Set<Entity> set = new HashSet<>();
        for (Entity e : entities)
            set.add(e);
        return set;
    }
}
