/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.ontology;

import com.google.common.base.Splitter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Table;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

/**
 *
 * @author gann
 */
@AllArgsConstructor
public class SimpleOntology implements Ontology {
    private Map<Relation<?,?>, HashMultimap<Entity, Entity>>  values;
    private Table<Class<? extends Entity>, String, Entity>    entities;

    @Override
    @SuppressWarnings("unchecked")
    public <From extends Entity, To extends Entity> Set<To> getRelation(From from, Relation<From, To> relation) {
        return (Set<To>) values.get(relation).get(from);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Entity> Set<E> getEntities(Class<E> clazz) {
        Set<E> result = new HashSet<>();
        for (Relation<?,?> relation : values.keySet()) {
            HashMultimap<Entity, Entity> m  = values.get(relation);
            if (relation.getFrom() == clazz)
                result.addAll((Set<E>) m.keySet());
            if (relation.getTo() == clazz)
                result.addAll((Collection<E>) m.values());
        }
        
        return result;
    }
    
    @Override
    public <E extends Entity> E getEntity(Class<E> clazz, String id) {
        return getEntity(clazz, id, entities, this);
    }

    
    @Value
    private static class SimpleConcept implements Concept {
        @NonNull private String id;
        @NonNull private Ontology ontology;                

    }

    @Value
    private static class SimpleLemma implements Lemma {
        @NonNull private String id;        
        @NonNull private Ontology ontology;
        @Override public String getLemma() { return id; }
    }
    
    @SuppressWarnings("unchecked")
    private static <E extends Entity> E getEntity(Class<E> clazz, String id, Table<Class<? extends Entity>, String, Entity> entities, Ontology ontology) {
        if (entities.contains(clazz, id))
                return (E) entities.get(clazz, id);
            else if (clazz == Concept.class)
                return (E) new SimpleConcept(id, ontology);
            else if (clazz == Lemma.class)
                return (E)new SimpleLemma(id, ontology); 
            else {
                try {
                    return clazz.getConstructor(String.class).newInstance(id);
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new IllegalArgumentException("Unable to instantiate " + clazz.getCanonicalName() + " with a string argument");
                }
            }
    };
    
    public static SimpleOntologyBuilder builder() {
        return new SimpleOntologyBuilder();
    }
    
    public static class SimpleOntologyBuilder {
        private Map<Relation<?,?>, HashMultimap<Entity, Entity>> values   = new HashMap<>();
        private Table<Class<? extends Entity>, String, Entity>   entities = HashBasedTable.create();        
        private SimpleOntology ontology = new SimpleOntology(values, entities);
        
        public <From extends Entity, To extends Entity> SimpleOntologyBuilder add(From from, To to, Relation<From, To> relation) {
            return _add(from, to, relation);
        }
        
        public SimpleOntologyBuilder add(String from, String to, Relation<? extends Entity, ? extends Entity> relation) {
            return _add(getEntity(relation.getFrom(), from, entities, ontology), getEntity(relation.getTo(), to, entities, ontology), relation);
        }
        
        private SimpleOntologyBuilder _add(Entity from, Entity to, Relation<? extends Entity, ? extends Entity> relation) {
            entities.put(relation.getFrom(), from.getId(), from);
            entities.put(relation.getTo(), to.getId(), to);            
            
            if (! values.containsKey(relation))
                values.put(relation, HashMultimap.<Entity, Entity>create());
            values.get(relation).put(from, to);
            
            if (relation.getReverse() != null) {
                if (! values.containsKey(relation.getReverse()))                
                    values.put(relation.getReverse(), HashMultimap.<Entity, Entity>create());
                values.get(relation.getReverse()).put(to, from);
            }
            return this;
        }
        
        public SimpleOntologyBuilder add(Reader reader)  throws IOException, ClassNotFoundException {
            BufferedReader br = new BufferedReader(reader);
            String line;
            Splitter splitter = Splitter.on('\t').trimResults();
            while ((line = br.readLine()) != null) {
                Iterator<String> split = splitter.split(line).iterator();
                
                if (! split.hasNext())
                    continue;
                
                String key = split.next();
                if (key.equals("#")) {
                    // Save the new relation by creating a new instance.  We'll be able to then access by name.
                    String name = split.next(), from = split.next(), to = split.next();
                    Class<? extends Entity> fromClass = Class.forName(from).asSubclass(Entity.class);
                    Class<? extends Entity> toClass   = Class.forName(to).asSubclass(Entity.class);                
                    if (split.hasNext()) 
                        Relation.register(name, split.next(), fromClass, toClass);                    
                    else 
                        Relation.register(name, fromClass, toClass);
                } else {
                    Relation<? extends Entity,? extends Entity> relation = Relation.getRelation(key);
                    String from = split.next();                
                    while (split.hasNext()) 
                        add(from, split.next(), relation);
                    
                }
            }
            return this;
        }
        
        public SimpleOntology build() {
            return ontology;
        }
    }

}
