/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.analyzer.ontology;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author gann
 */
public interface Ontology {
    @EqualsAndHashCode(exclude = "reverse")
    public static class Relation<From extends Entity, To extends Entity> {
        private static Map<String, Relation<?,?>> RELATIONS = new HashMap<>();
        
        public static final Relation<Concept, Concept> HYPO = new Relation<>("HYPO", Concept.class, Concept.class);
        public static final Relation<Concept, Concept> HYPE = new Relation<>("HYPE", Concept.class, Concept.class);
        public static final Relation<Concept, Lemma>   SYNS = new Relation<>("SYNS", Concept.class, Lemma.class);
        
        static {
            HYPO.reverse = HYPE;
            HYPE.reverse = HYPO;            
        }
        
        @Getter private String             id;
        @Getter private Class<From>        from;
        @Getter private Class<To>          to;        
        @Getter private Relation<To, From> reverse;

        private Relation(String id, Class<From> from, Class<To> to) {
            this.id   = id;
            this.from = from;
            this.to   = to;
            
            RELATIONS.put(id, this);
        }
        
        @SuppressWarnings("unchecked")
        public static <From extends Entity, To extends Entity> Relation<From,To> getRelation(String id) {
            return (Relation<From, To>) RELATIONS.get(id);
        }
        
        public static <From extends Entity, To extends Entity> Relation<From, To> register(String id, Class<From> from, Class<To> to) {
            return new Relation<>(id, from, to);
        }
        
        public static <From extends Entity, To extends Entity> Relation<From, To> register(String id, String revId, Class<From> from, Class<To> to) {
            Relation<From, To> relation = new Relation<>(id, from, to);
            Relation<To, From> revRelation = new Relation<>(revId, to, from);            
            relation.reverse = revRelation;
            revRelation.reverse = relation;
            return relation;
        }
        
        @Override
        public String toString() {
            return id + ":" + from.getSimpleName() + "->" + to.getSimpleName();
        }
    }
    
    public interface Entity {
        String getId();    
        Ontology getOntology();
    }
    
    public interface Concept extends Entity { }
    
    public interface Lemma extends Entity {
        String getLemma();
    }
    
    <From extends Entity, To extends Entity> Set<To> getRelation(From from, Relation<From, To> relation);

    <E extends Entity> Set<E> getEntities(Class<E> clazz);
    
    <E extends Entity> E getEntity(Class<E> clazz, String id);

}
