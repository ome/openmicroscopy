/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import ome.conditions.ApiUsageException;

import org.apache.lucene.analysis.Analyzer;

/**
 * {@link FullText} subclass which
 * {@link #parse(String[], String[], String[]) parses} 3 arrays of strings into
 * into a single Lucene query. If no text is produced, then an exception will be
 * thrown. Some terms are joined in to "( a OR b OR c)", must terms are turned
 * into "+d +e +f", and none terms are turned into "-g -h -i".
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SomeMustNone extends FullText {

    private static final long serialVersionUID = 1L;

    private final String[] some;
    private final String[] must;
    private final String[] none;

    public SomeMustNone(SearchValues values, String[] some, String[] must,
            String[] none, Class<? extends Analyzer> analyzer) {
        super(values, parse(some, must, none), analyzer);
        this.some = some;
        this.must = must;
        this.none = none;
    }

    protected static String parse(String[] some, String[] must, String[] none) {
        final StringBuilder sb = new StringBuilder();

        if (some != null && some.length > 0) {
            sb.append("(");
            boolean first = true;
            for (String string : some) {
                if (string.length() > 0) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(" OR ");
                    }
                    sb.append(string);
                }
            }
            sb.append(")");
        }

        if (sb.length() > 0) {
            sb.append(" ");
        }

        if (must != null && must.length > 0) {
            for (String string : must) {
                if (string.length() > 0) {
                    sb.append(" +");
                    sb.append(string);
                }
            }
        }

        if (sb.length() > 0) {
            sb.append(" ");
        }

        if (none != null && none.length > 0) {
            for (String string : none) {
                if (string.length() > 0) {
                    sb.append(" -");
                    sb.append(string);
                }
            }
        }

        if (sb.toString().length() == 0) {
            throw new ApiUsageException(
                    "No search terms provided for SomeMustNone");
        }
        return sb.toString();
    }
}
