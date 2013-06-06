/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.graphs;

import java.math.BigInteger;
import java.util.List;

import ome.tools.hibernate.QueryBuilder;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.impl.SQLQueryImpl;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractHierarchyGraphSpec} specialized for only loading a single
 * /Fileset regardless of how many may exist at a given level.
 *
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.0.0
 * @see IGraph
 */
public class FilesetGraphSpec extends BaseGraphSpec {

    private final static Logger log = LoggerFactory
            .getLogger(FilesetGraphSpec.class);

    /**
     * Creates a new instance.
     *
     * @param entries
     *            The entries to handle.
     */
    public FilesetGraphSpec(List<String> entries) {
        super(entries);
    }

    /**
     * Create an SQL-based {@link QueryBuilder} so we can make use of
     * "distinct on" which is not available in Hibernate.
     */
    @Override
    protected QueryBuilder createQueryBuilder(String[] sub, GraphEntry subpath) {
        final QueryBuilder qb = new QueryBuilder(true); // SQL
        final StringBuilder sb = new StringBuilder();
        final int size = subpath.ownParts();
        final int from = sub.length - size;

        sb.append(" distinct on ( ");
        sb.append("ROOT");
        sb.append(from);
        sb.append(".id");
        for (int i = from+1; i < sub.length; i++) {
            sb.append(", ROOT");
            sb.append(i);
            sb.append(".id");
        }
        sb.append(" ) ");
        final String distinct = sb.toString();
        log.debug("Prepending {}", distinct);
        qb.select(distinct);
        return qb;
    }

    /**
     * Calls {@link SQLQueryImpl#addScalar(String, org.hibernate.type.Type)}
     * to work around missing return type information in the SQL query which
     * leads to bizarre behaviors like wrong IDs and/or nulls being returned,
     * type conversion to BigInteger, etc.
     */
    @Override
    protected List<List<Long>> runQuery(String[] sub, GraphEntry subpath,
            Query q, Session session) {
        SQLQueryImpl impl = (SQLQueryImpl) q;
        for (int i = 0; i < sub.length; i++) {
            impl.addScalar("ROOT"+i, new LongType());
        }
        return super.runQuery(sub, subpath, q, session);
    }

    /**
     * SQL-specific implementation has to use a different joining strategy
     * from {@link BaseGraphSpec}.
     */
    @Override
    protected void walk(String[] sub, final GraphEntry entry, final QueryBuilder qb)
            throws GraphException {
        qb.from(sub[0], "ROOT0");
        for (int p = 1; p < sub.length; p++) {
            qb.from(sub[p], "ROOT"+p); // Added a basic from for the table
            String p_1 = sub[p - 1];
            String r_1 = "ROOT" + (p-1);
            String p_0 = sub[p];
            String r_0 = "ROOT" + p;
            qb.where();
            qb.and(em.getSQLJoin(p_1, r_1, p_0, r_0));
        }
    }

    /**
     * For the non-distinct rows other than the first, use "-1" for the value.
     * With "distinct on ( ... )", there's no guarantee that we will get the
     * same values back per row which causes the GraphTables match() logic to
     * miss rows.
     */
    @Override
    protected long[][] parseResults(String[] sub, GraphEntry subpath,
            List<List<Long>> results) {
        long[][] rv = super.parseResults(sub, subpath, results);

        final int size = subpath.ownParts();
        final int upto = sub.length - size;
        for (long[] row : rv) {
            // From the second element upto the beginning of "distinct ()"
            // set all values to -2.
            for (int i = 1; i < upto; i++) {
                row[i] = -2;
            }
        }
        return rv;
    }
}