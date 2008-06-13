/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import ome.services.fulltext.FullTextAnalyzer;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.Test;

public class TokenizationTest extends MockObjectTestCase {

    void assertTokenizes(String text, String... tokens) {
        List<Token> results = tokenize(text);
        assertEquals(tokens.length, results.size());
        for (int i = 0; i < tokens.length; i++) {
            assertEquals(tokens[i], results.get(i).termText());
        }
    }

    @Test
    public void testDefaults() {
        assertTokenizes("foo bar", "foo", "bar");
        assertTokenizes("foo/bar", "foo", "bar");
        assertTokenizes("foo-bar", "foo", "bar");
        assertTokenizes("foo_bar", "foo", "bar");
        assertTokenizes("foo.bar", "foo", "bar");
        assertTokenizes("U.S.A.", "u", "s", "a");
        assertTokenizes("26.8.06-antiCSFV/CSFV-GFP/CSFV-GFP01_1_R3D_D3D.dv",
                "26", "8", "06", "anticsfv", "csfv", "gfp", "csfv", "gfp01",
                "1", "r3d", "d3d", "dv");
        assertTokenizes("...FRAP-23.8.05/IAGFP-Noc05_R3D.dv", "frap", "23",
                "8", "05", "iagfp", "noc05", "r3d", "dv");
        assertTokenizes("will/Desktop/CSFV-GFP01_3_R3D_D3D.dv", "will",
                "desktop", "csfv", "gfp01", "3", "r3d", "d3d", "dv");
        assertTokenizes("Documents/biology-data/CSFV-GFP01_3_R3D_D3D.dv",
                "documents", "biology", "data", "csfv", "gfp01", "3", "r3d",
                "d3d", "dv");
    }

    private List<Token> tokenize(String a) {
        System.out.println("Print tokening: " + a);
        // StandardAnalyzer sa = new StandardAnalyzer();
        FullTextAnalyzer sa = new FullTextAnalyzer();
        TokenStream ts = sa.tokenStream("field", new StringReader(a));
        List<Token> tokens = new ArrayList<Token>();
        try {
            while (true) {
                Token t = ts.next();
                if (t == null) {
                    break;
                }
                tokens.add(t);
            }
        } catch (IOException io) {
            // ok
        }
        return tokens;
    }
}
