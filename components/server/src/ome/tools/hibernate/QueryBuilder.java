/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.hibernate;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ome.conditions.ApiUsageException;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Very thin wrapper around a {@link StringBuilder} to generate HQL queries.
 * This comes from the very real deficiencies of the Criteria API when trying to
 * implement ome.services.SearchBean.
 * 
 * Note: It is the responsibility of each method here to end with a blank space,
 * meaning that each method may also begin without one.
 * 
 * This class is NOT thread-safe.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class QueryBuilder {

    private int count = 0;

    private final StringBuilder sb;

    private final Set<String> random = new HashSet<String>();

    private final Map<String, Object> params = new HashMap<String, Object>();

    private final Map<String, Collection> listParams = new HashMap<String, Collection>();

    private String self;

    /** Booleans which represent what is already complete */
    private boolean select, from, join, where, order, group;

    public QueryBuilder() {
        sb = new StringBuilder();
    }

    public QueryBuilder(int size) {
        sb = new StringBuilder(size);
    }

    public void throwUsage() throws ApiUsageException {
        StringBuilder err = new StringBuilder();
        err.append("It is required to call in the following order:\n");
        err.append("select, from, join*, where, [and,or]*, order*, group*");
        throw new ApiUsageException(err.toString());
    }

    /**
     * Obtain a unique alias to be used in the SQL.
     * 
     * @param prefix
     *            Not null
     */
    public String unique_alias(String prefix) {
        StringBuilder sb = new StringBuilder(prefix.length() + 8);
        sb.append(prefix.trim());
        while (random.contains(sb.toString())) {
            sb.append(count++);
        }
        String alias = sb.toString();
        random.add(alias);
        return alias;
    }

    /**
     * Simple delegate method to allow appending arbitrary strings.
     */
    public QueryBuilder append(String string) {
        sb.append(string);
        return this;
    }

    public QueryBuilder select(String... selects) {
        if (select || from || join || where || order || group) {
            throwUsage();
        }

        if (selects == null || selects.length == 0) {
            throwUsage();
        }

        sb.append("select ");
        for (int i = 0; i < selects.length; i++) {
            sb.append(selects[i]);
            if (i > 0) {
                sb.append(", ");
            }
        }
        appendSpace();
        select = true;
        return this;
    }

    /**
     * 
     * @param type
     * @param alias
     * @return
     */
    public QueryBuilder from(String type, String alias) {
        if (!select || join || where || order || group) {
            throwUsage();
        }
        this.self = alias;
        sb.append("from ");
        sb.append(type);
        appendSpace();
        sb.append("as ");
        sb.append(alias);
        appendSpace();
        select = true;
        from = true;
        return this;
    }

    public QueryBuilder join(String path, String alias, boolean outer,
            boolean fetch) {
        if (!select || !from || where || order || group) {
            throwUsage();
        }
        if (outer) {
            sb.append("left outer ");
        }
        sb.append("join ");
        if (fetch) {
            sb.append("fetch ");
        }
        sb.append(path);
        appendSpace();
        sb.append("as ");
        sb.append(alias);
        appendSpace();
        return this;
    }

    /**
     * Marks the end of all fetches by adding a "where" clause to the string.
     */
    public QueryBuilder where() {
        if (!select || !from || where || order || group) {
            throwUsage();
        }
        sb.append("where ");
        join = true;
        return this;
    }

    /**
     * Appends "and" plus your string unless this is the first where-spec in
     * which case it is simply appended.
     * 
     * @param str
     * @return
     */
    public QueryBuilder and(String str) {
        if (!select || !from || !join || order || group) {
            throwUsage();
        }
        if (!where) {
            sb.append(str);
            where = true;
        } else {
            sb.append("and ");
            sb.append(str);
        }
        appendSpace();
        return this;
    }

    /**
     * Appends "or" plus your string unless this is the first where-spec in
     * which case it is simply appended.
     * 
     * @param str
     * @return
     */
    public QueryBuilder or(String str) {
        if (!select || !from || !join || order || group) {
            throwUsage();
        }
        if (!where) {
            sb.append(str);
            where = true;
        } else {
            sb.append("or ");
            sb.append(str);
        }
        appendSpace();
        return this;
    }

    public QueryBuilder order(String path, boolean ascending) {
        if (!select || !from || !join || !where || group) {
            throwUsage();
        }
        if (!order) {
            sb.append("order by ");
            order = true;
        } else {
            sb.append(", ");
        }
        sb.append(path);
        appendSpace();
        if (ascending) {
            sb.append("asc ");
        } else {
            sb.append("desc ");
        }
        return this;
    }

    public QueryBuilder param(String key, Object o) {
        params.put(key, o);
        return this;
    }

    public QueryBuilder paramList(String key, Collection c) {
        listParams.put(key, c);
        return this;
    }

    public Query query(Session session) {
        Query q = session.createQuery(sb.toString());
        for (String key : params.keySet()) {
            q.setParameter(key, params.get(key));
        }
        for (String key : listParams.keySet()) {
            q.setParameterList(key, listParams.get(key));
        }
        return q;
    }

    public QueryBuilder appendSpace() {
        if (sb.charAt(sb.length() - 1) != ' ') {
            sb.append(' ');
        }
        return this;

    }

    /**
     * Returns the current query as a String. As opposed to {@link #toString()},
     * this method should return parseable HQL.
     */
    public String queryString() {
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder();
        toString.append(sb);
        toString.append(params);
        toString.append(listParams);
        return toString.toString();
    }
}