/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import ome.tools.hibernate.QueryBuilder;

import org.hibernate.Session;

/**
 * algorithms for sorting and indexing the data returned by
 * {@link GraphSpec#queryBackupIds(Session, int, GraphEntry, QueryBuilder)}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class GraphTables {

    /**
     * Original data as returned by
     * {@link GraphSpec#queryBackupIds(Session, int, GraphEntry, QueryBuilder)}
     * References to these rows will be stored in {@link #pointers}.
     */
    final Map<GraphEntry, long[][]> tables = new HashMap<GraphEntry, long[][]>();

    final static Comparator<long[]> CMP = new Comparator<long[]>() {
        public int compare(long[] o1, long[] o2) {
            for (int i = 0; i < o1.length; i++) {
                long l1 = o1[i];
                long l2 = o2[i];
                // Copied from java.lang.Long.compareTo
                int cmp = (l1 < l2 ? -1 : (l1 == l2 ? 0 : 1));
                if (cmp != 0) {
                    return cmp;
                }
            }
            throw new IllegalStateException(
                    "Should never return two identical items! " + o1);
        }
    };

    public void add(GraphEntry entry, long[][] results) {
        if (results != null && results.length > 0) {
            Arrays.sort(results, CMP);
            tables.put(entry, results);
        }
    }

    /**
     * Returns all column sets where the first LENGTH - 1 members of "match"
     * coincide with the values of the columns in the set. (Remembering that a
     * set is a group of rows which are all equal except for the last value it
     * suffices to check any member of the set).
     *
     * @param entry
     * @param match
     * @return
     */
    public Iterator<List<long[]>> columnSets(final GraphEntry entry,
            final long[] match) {

        final long[][] r = tables.get(entry);

        return new Iterator<List<long[]>>() {

            /**
             * Current index of the next item which will be loaded. As soon as
             * an entry is found that does not match the current value, then the
             * offset is set to the current index.
             */
            private int offset = 0;

            /**
             * Toggle to prevent from constantly researching items when none are
             * to found. On starting through a loop, "matched" is false, meaning
             * that all values are to be tested. As soon as "matched" is true,
             * however, then once a match fails no further values need to be
             * checked (since all entries are ordered).
             */
            private boolean matched = false;

            /**
             * The value which will be returned by the next call to next(). If
             * null, then either load() has not yet been called or there is no
             * further valid entry.
             */
            private List<long[]> next = null;

            void load() {

                if (r == null) {
                    // We don't have any data for this entry.
                    return;
                }

                if (next != null) {
                    // Another object is active; must wait until next()
                    // is called and sets it to null.
                    return;
                }

                long[] cols = null;
                long[] check = null;
                int sz = -1;

                // Initialize sz. Since all the rows are the same
                // length, we only need to do it once.
                if (r.length > 0) {
                    sz = Math.max(1, r[0].length - 1);
                }

                LOOP: for (int idx = offset; idx < r.length; idx++) {
                    if (cols == null) {

                        // This is the first item in the loop, so take it
                        // and create a new "next" value IF it matches
                        // the (possibly null) "match" argument.
                        cols = r[idx];

                        if (match != null) {
                            int size = match.length - 1;
                            for (int w = 0; w < size; w++) {

                                if (w >= match.length || w >= cols.length) {
                                    break; // FIXME THIS IS STILL ODD
                                }

                                if (match[w] != cols[w]) {
                                    cols = null;
                                    if (matched) {
                                        offset = r.length; // CANCEL further.
                                        break LOOP;
                                    } else {
                                        offset = idx + 1;
                                        continue LOOP; // Goto the next.
                                    }
                                }

                            }
                            matched = true;
                        }

                        // Here we've matched (or there is none)
                        // so save it.
                        next = new ArrayList<long[]>();
                        next.add(cols);

                    } else {

                        // Second or later pass through the loop, so
                        // check for a match. If yes, append; if no,
                        // reset for the next loop;
                        check = r[idx];
                        for (int w = 0; w < sz; w++) {
                            if (check[w] != cols[w]) {
                                cols = null;
                                check = null;
                                offset = idx;
                                break LOOP; // Redo this value _next_ time
                            }
                        }

                        next.add(check);

                    }

                    // If we reach here, then an element has been saved,
                    // and therefore we reset to idx+1 because the current
                    // element doesn't need reprocessing.
                    offset = idx + 1;
                }
            }

            public boolean hasNext() {
                load();
                return next != null;
            }

            public List<long[]> next() {
                load();
                if (next == null) {
                    throw new NoSuchElementException();
                }
                try {
                    return next;
                } finally {
                    next = null;
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("\n");
        for (GraphEntry key : tables.keySet()) {
            sb.append(key);
            sb.append("=");
            sb.append(Arrays.deepToString(tables.get(key)));
            sb.append("\n");
        }
        return sb.toString();
    }

}
