/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.ontology;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Setter;
import org.bierner.matchbook.analyzer.ontology.SimpleOntology.SimpleOntologyBuilder;

/**
 *
 * @author gann
 */
public class Ontologies {
    /**
     * A strategy for loading an Ontology by name.
     */
    public interface LoadStrategy {
        Ontology load(String ... names);
    }
    
    /**
     * A model loading strategy that loads as a resource from a given base path.
     */
    @AllArgsConstructor
    public static class ResourceLoadStrategy implements LoadStrategy {
        private String basePath;
        @Override
        public Ontology load(String... names) {
                SimpleOntologyBuilder builder = SimpleOntology.builder();
                for (String name : names) {
                    try {
                        @Cleanup Reader reader = new InputStreamReader(ClassLoader.getSystemResourceAsStream(basePath + "/" + name));                    
                        builder.add(reader);
                    } catch (ClassNotFoundException | IOException | NullPointerException e) {
                        throw new IllegalArgumentException("Unable to load ontology: " + name, e);
                    }
                }
                return builder.build();
        }
    }
    
    @Setter private static LoadStrategy loader = new ResourceLoadStrategy(Ontologies.class.getPackage().getName().replaceAll("\\\\.", "/"));
    
    public static Ontology getOntology(String... names) {
        return loader.load(names);
    }
}
