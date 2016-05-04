/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.matcher.realtime.indexing;

import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.ExtensionMethod;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.Sentence;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher;
import org.bierner.matchbook.matcher.realtime.RealtimeSentenceMatcher.Match;
import org.bierner.matchbook.matcher.realtime.expr.AnnotationExpression;
import org.bierner.matchbook.matcher.realtime.expr.CaptureExpression;
import org.bierner.matchbook.matcher.realtime.expr.CompoundExpression;
import static org.bierner.matchbook.matcher.realtime.expr.CompoundExpression.Type.IS;
import static org.bierner.matchbook.matcher.realtime.expr.CompoundExpression.Type.ISNT;
import org.bierner.matchbook.matcher.realtime.expr.Expression;
import org.bierner.matchbook.matcher.realtime.expr.ExpressionVisitor;
import org.bierner.matchbook.matcher.realtime.expr.RepeatExpression;
import org.bierner.matchbook.matcher.realtime.expr.WithExpression;

/**
 * A sentence matcher that indexes an input sentence then performs vector based implementations
 * of the matchbook operations to provide results.  In that sense, this sentence matcher is much
 * like one that would be implemented for a search engine like Solr/Lucene.  However, in this case
 * the matching expressions are known before hand, so the indexing process can be optimized to
 * include only those items contained in the expressions.
 *
 * @author gann
 */
public class IndexingRealtimeSentenceMatcher implements RealtimeSentenceMatcher {
    @NonNull private final List<Expression> exprs;       // The accepting expressions for this matcher
    @NonNull private final VectorFactory vectorFactory;  // An implementation of vector operations

    // A map containing just those elements that are required to be indexed for matching purposes.
    // The map is from annotation type to annotation value.
    private HashMultimap<String, String> idsToMatch = HashMultimap.create();

    ///////////////////////////////////////////////////////////////////////////
    // Construction
    ///////////////////////////////////////////////////////////////////////////
    /**
     * A builder to simplify the construction of this matcher.
     */
    @Accessors(fluent = true, chain = true)
    public static class IndexingRealtimeSentenceMatcherBuilder {
        @NonNull private final List<Expression> expressions = Lists.newArrayList();
        @Setter @NonNull private VectorFactory vectorFactory = new PackedVectorFactory();

        public IndexingRealtimeSentenceMatcherBuilder setExpressions(List<Expression> expr) {
            expressions.clear();
            expressions.addAll(expr);
            return this;
        }

        public IndexingRealtimeSentenceMatcherBuilder addExpression(Expression expr) {
            expressions.add(expr);
            return this;
        }

        public IndexingRealtimeSentenceMatcher build() {
            return new IndexingRealtimeSentenceMatcher(vectorFactory, expressions);
        }
    }

    /**
     * Gets a new builder for this matcher.
     * @return a builder
     */
    public static IndexingRealtimeSentenceMatcherBuilder builder() {
        return new IndexingRealtimeSentenceMatcherBuilder();
    }

    /**
     * A constructor, but unless you're planning on setting the vector factory, you might as well
     * use the builder.
     *
     * @param vectorFactory An implementation of vectors and their operations when evaluating expressions.
     * @param exprs The accepting expressions for this matcher.
     */
    public IndexingRealtimeSentenceMatcher(VectorFactory vectorFactory, List<Expression> exprs) {
        this.exprs = Lists.newArrayList(exprs);
        this.vectorFactory = vectorFactory;

        for (Expression expr : exprs) {
            expr.accept(new ExpressionVisitor() {
                @Override public void visit(AnnotationExpression expr) {
                    idsToMatch.put(expr.getType(), expr.getValue());
                }

                @Override public void visit(CaptureExpression expr) { }
                @Override public void visit(CompoundExpression expr) { }
                @Override public void visit(RepeatExpression expr) { }
                @Override public void visit(WithExpression expr) { }
            });
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Matching implementation
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public Matches match(Sentence sentence) {
        // Index the sentence
        Table<String, String, Vector> index = index(sentence);

        // Build vectors of results from the expressions
        List<Vector> vectors = new ArrayList<>(exprs.size());
        VectorExpressionVisitor visitor = new VectorExpressionVisitor(index, vectorFactory, sentence);
        for (Expression expr : exprs) {
            expr.accept(visitor);
            vectors.add(visitor.getVector(expr));
            visitor.clear();
        }

        // Return the result!
        return getMatches(vectorFactory.or(vectors));
    }

    /**
     * Match the accepting expressions on at a time and return the matches separately
     * @param sentence The sentence to match.
     * @return A list of Matches, one for each accepting expression and in the same order.
     */
    public List<Matches> matchIndividually(Sentence sentence) {
        // Index the sentence
        Table<String, String, Vector> index = index(sentence);

        // Build vectors of results from the expressions
        List<Matches> matches = new ArrayList<>(exprs.size());
        VectorExpressionVisitor visitor = new VectorExpressionVisitor(index, vectorFactory, sentence);
        for (Expression expr : exprs) {
            expr.accept(visitor);
            matches.add(getMatches(visitor.getVector(expr)));
            visitor.clear();
        }

        // Return the result!
        return matches;
    }

    // Save the positions of all the sentence annotations that could possibly be relevant to the
    // accepting expressions.
    private Table<String, String, Vector> index(Sentence sentence) {
        Table<String, String, Vector> index = HashBasedTable.create(); // Annotation Type Name x Annotation Id -> Vector
        for (String typeName : idsToMatch.keySet()) {
            Set<String> ids = idsToMatch.get(typeName);
            for (Annotation<?> annotation : sentence.getAnnotations(AnnotationType.getType(typeName))) {
                if (ids.contains(annotation.getId())) {
                    Vector v = index.get(typeName, annotation.getId());
                    if (v == null)
                        index.put(typeName, annotation.getId(), v = vectorFactory.newInstance());
                    v.add(annotation.getStart(), annotation.getEnd());
                }
                if (ids.contains(null)) {
                    Vector v = index.get(typeName, "");
                    if (v == null)
                        index.put(typeName, "", v = vectorFactory.newInstance());
                    v.add(annotation.getStart(), annotation.getEnd());
                }
            }
        }
        return index;
    }

    // A class to traverse the accepting expressions and build up a vector of matches along the way.
    @RequiredArgsConstructor
    @ExtensionMethod(SentenceUtilities.class)
    private class VectorExpressionVisitor implements ExpressionVisitor {
        // A map from expressions to the corresponding vector of matches.  This is where
        // the results are saved while traversing the accepting expression.
        private IdentityHashMap<Expression, Vector> vectors = new IdentityHashMap<>();

        @NonNull private Table<String, String, Vector> index;    // The index of the sentence to match
        @NonNull private VectorFactory                 factory;  // Vector operation implementation
        @NonNull private Sentence                      sentence; // The sentence to match

        // Returns the matched vector for the requested expression
        public Vector getVector(Expression expr) {
            return vectors.get(expr);
        }

        // Clear state to do another run
        public void clear() {
            vectors.clear();
        }

        @Override
        public void visit(AnnotationExpression expr) {
            Vector v = index.get(expr.getType(), expr.getValue() == null? "" : expr.getValue());
            vectors.put(expr, v == null? vectorFactory.emptyInstance() : v);
        }

        @Override
        public void visit(CaptureExpression expr) {
            vectors.put(expr, vectorFactory.capture(vectors.get(expr.getExpression()), expr.getId()));
        }

        @Override
        public void visit(CompoundExpression expr) {
            List<Vector> subs = new ArrayList<>(expr.getSubExpressions().size());
            for (Expression e : expr.getSubExpressions())
                subs.add(vectors.get(e));

            switch (expr.getType()) {
                case IS:       vectors.put(expr, factory.is(subs)); break;
                case ISNT:     vectors.put(expr, factory.isnt(subs.get(0), subs.get(1))); break;
                case OR:       vectors.put(expr, factory.or(subs)); break;
                case SEQUENCE: vectors.put(expr, factory.sequence(subs)); break;
            }
        }

        @Override
        public void visit(RepeatExpression expr) {
            vectors.put(expr, factory.repeat(vectors.get(expr.getExpression()), expr.getFrom(), expr.getTo(), sentence.tokenCount()));
        }

        @Override
        public void visit(WithExpression expr) {
            vectors.put(expr, factory.with(vectors.get(expr.getAnnotation()), vectors.get(expr.getWithExpression())));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Match and Matches implementation
    //
    // These are simply facades on vectors to the simpler match interface
    // required by the RealtimeSentenceMatcher.
    ///////////////////////////////////////////////////////////////////////////

    // Provide the proper Matches implementation depending on whether the vector contains capture
    // groups or not.  But providing different implementations, we can avoid some extra space for
    // each match.
    private Matches getMatches(Vector v) {
        if (v instanceof CapturingVector)
            return new CaptureVectorMatches((CapturingVector) v);
        else
            return new VectorMatches(v);
    }

    // A simple non-capturing match
    @AllArgsConstructor
    private static class BasicMatch implements Match {
        @Getter private final int start;
        @Getter private final int end;

        @Override public CaptureGroups getCaptureGroups() { return null; }
        @Override public String toString() { return "(" + start + "," + end + ")"; }
    }

    // A match with capture groups
    @AllArgsConstructor
    @ExtensionMethod(CaptureGroups.Utilities.class)
    private static class CaptureMatch implements Match {
        @Getter private final int start;
        @Getter private final int end;
        @Getter private final CaptureGroups captureGroups;

        @Override public String toString() {
            return "(" + start + "," + end + "):" + captureGroups.asMap();
        }
    }


    // A non-capturing Matches implementation.  It returns simple BasicMatches.
    @AllArgsConstructor
    private static class VectorMatches implements Matches {
        private final Vector vector;

        @Override
        public int size() {
            return vector.length();
        }

        @Override
        public Match get(int pos) {
            return new BasicMatch(vector.getStart(pos), vector.getEnd(pos));
        }

        @Override
        public Iterator<Match> iterator() {
            return new Iterator<Match>() {
                private int pos = 0;
                @Override public boolean hasNext() { return pos < vector.length(); }
                @Override public Match next() { return get(pos++); }
                @Override public void remove() { throw new UnsupportedOperationException(); }
            };
        }

        @Override
        public String toString() {
            return "[" + Joiner.on(",").join(this) + "]";
        }
    }


    // A Matches implementation that handles capture groups.
    private static class CaptureVectorMatches extends VectorMatches {
        private final CapturingVector vector;

        public CaptureVectorMatches(CapturingVector v) {
            super(v);
            this.vector = v;
        }

        @Override
        public Match get(int pos) {
            return new CaptureMatch(vector.getStart(pos), vector.getEnd(pos), vector.getCaptured(pos));
        }
    }

}
