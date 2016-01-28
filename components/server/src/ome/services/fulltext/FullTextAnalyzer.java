/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.LetterTokenizer;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenStream;

/**
 * {@link Analyzer} implementation based largely on {@link SimpleAnalyzer}, but
 * with extensions for handling scientific and OS-type strings.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class FullTextAnalyzer extends Analyzer {

    private final static Logger log = LoggerFactory.getLogger(FullTextAnalyzer.class);

    static {
        log.info("Initialized FullTextAnalyzer");
    }

    /**
     * Based on {@link LowerCaseTokenizer}, with the same optimization.
     * However, in order to do alphanumeric tokenizing, rather than just
     * alphabetic, it was necessary to combine that implementation with
     * {@link LetterTokenizer} and extend {@link CharTokenizer} directly.
     * 
     */
    static class LowercaseAlphaNumericTokenizer extends CharTokenizer {

        public LowercaseAlphaNumericTokenizer(Reader input) {
            super(input);
        }

        /**
         * Returns true if "c" is {@link Character#isLetter(char)} or
         * {@link Character#isDigit(char)}.
         */
        @Override
        protected boolean isTokenChar(char c) {
            return Character.isLetter(c) || Character.isDigit(c);
        }

        /**
         * Lower cases via {@link Character#toLowerCase(char)}
         */
        @Override
        protected char normalize(char c) {
            return Character.toLowerCase(c);
        }
    }

    /**
     * Returns a {@link ome.services.fulltext.FullTextAnalyzer.LowercaseAlphaNumericTokenizer}
     */
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new LowercaseAlphaNumericTokenizer(reader);
    }

}
