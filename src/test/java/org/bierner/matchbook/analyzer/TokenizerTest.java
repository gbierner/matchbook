/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import org.bierner.matchbook.analyzer.java.BreakIteratorTokenizer;
import org.bierner.matchbook.analyzer.opennlp.ThreadSafeOpenNLPTokenizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.*;

/**
 *
 * @author gann
 */
@RunWith(Parameterized.class)
@AllArgsConstructor
public class TokenizerTest {
    
    private Tokenizer tokenizer;
    
    @Test
    public void test() {
        test("foo bar", "foo", "bar");
        test("hello, there", "hello", ",", "there");
    }
    
    private void test(String sentence, String ... tokens) {
        List<String> tokenList = tokenizer.tokenize(sentence);
        assertEquals(Arrays.asList(tokens), tokenList);
    }
    
    @Parameters
    public static Collection<Object[]> getParameters() throws IOException {
        return Arrays.asList(new Object[][] {
            { new BreakIteratorTokenizer(Locale.ENGLISH)},
            { new ThreadSafeOpenNLPTokenizer(Locale.ENGLISH)}
        });
    }
}
