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
import ome.parameters.Filter;
import ome.parameters.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopProxy;

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

    private final static Logger log = LoggerFactory.getLogger(QueryBuilder.class);

    private int count = 0;

    private final StringBuilder select = new StringBuilder();

    private final StringBuilder from = new StringBuilder();

    private final StringBuilder join = new StringBuilder();

    private final StringBuilder where = new StringBuilder();

    private final StringBuilder order = new StringBuilder();

    private final StringBuilder group = new StringBuilder();

    private final Set<String> random = new HashSet<String>();

    private final Map<Integer, Object> seqParams = new HashMap<Integer, Object>();

    private final Map<String, Object> params = new HashMap<String, Object>();

    private final Map<String, Collection> listParams = new HashMap<String, Collection>();

    /**
     * final {@link StringBuilder} instance which is currently being modified.
     * For example, when "select" is called, then the {@link #select} instance
     * is stored in {@link #current} so that calls to methods like
     * {@link #append(String)} do the right thing.
     */
    private StringBuilder current;

    /**
     * Number of clauses in the where.
     */
    private int whereCount = 0;

    private String self;

    private Filter filter;

    /**
     * The path which the userId and the groupId in the {@link Filter} (if
     * present} will be applied to.
     */
    private String filterTarget;

    /**
     * @see {@link QueryBuilder#QueryBuilder(boolean)}
     */
    private boolean sqlQuery = false;

    public QueryBuilder() {
        // no-op
    }

    /**
     * Whether {@link Session#createSQLQuery(String)} should be used or not during
     * {@link #query(Session)} and similar.
     */
    public QueryBuilder(boolean sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public QueryBuilder(int size) {
        // ignore size for the moment
        this();
    }

    /**
     * Obtain a unique alias to be used in the SQL.
     * @param prefix the prefix for the alias
     * @return a unique alias
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
        current.append(string);
        return this;
    }

    public QueryBuilder select(String... selects) {
        _type("select");

        if (selects == null || selects.length == 0) {
            throw new ApiUsageException("Empty select");
        }

        for (int i = 0; i < selects.length; i++) {
            if (i != 0) {
                select.append(", ");
            }
            select.append(selects[i]);
            appendSpace();
        }
        return this;
    }

    void _type(String type) {
        current = select;
        if (select.length() == 0) {
            select.append(type);
            appendSpace();
        }

    }

    public QueryBuilder from(String type, String alias) {
        current = from;
        if (from.length() == 0) {
            from.append("from ");
        } else {
            from.append(", ");
        }

        this.self = alias;
        from.append(type);
        appendSpace();
        from.append("as ");
        from.append(alias);
        appendSpace();
        return this;
    }

    public QueryBuilder join(String path, String alias, boolean outer,
            boolean fetch) {
        current = join;

        if (outer) {
            join.append("left outer ");
        }
        join.append("join ");
        if (fetch) {
            join.append("fetch ");
        }
        join.append(path);
        appendSpace();
        join.append("as ");
        join.append(alias);
        appendSpace();
        return this;
    }

    /**
     * Marks the end of all fetches by adding a "where" clause to the string.
     */
    public QueryBuilder where() {
        current = where;

        if (where.length() == 0) {
            where.append("where ");
        }

        return this;
    }

    /**
     * Appends "and" plus your string unless this is the first where-spec in
     * which case it is simply appended.
     */
    public QueryBuilder and(String str) {
        return _where("and ", str);
    }


    /**
     * Appends "or" plus your string unless this is the first where-spec in
     * which case it is simply appended.
     */
    public QueryBuilder or(String str) {
        return _where("or ", str);
    }

    private QueryBuilder _where(String bool, String str) {
        where(); // check size and set current
        whereCount++;

        if (whereCount != 1) {
            where.append(bool);
         }
        where.append(str);
        appendSpace();
        return this;
    }

    /**
     * Appends the string representation of the {@link QueryBuilder} argument
     * inside of parentheses.
     */
    public QueryBuilder subselect(QueryBuilder subselect) {

        // Leave current as is.

        if ("".equals(subselect.queryString().trim())) {
            // Nothing to do.
            return this;
        }
        current.append("(");
        current.append(subselect.queryString());
        current.append(")");
        for (String key : subselect.listParams.keySet()) {
            this.listParams.put(key, subselect.listParams.get(key));
        }
        appendSpace();
        return this;
    }

    public QueryBuilder order(String path, boolean ascending) {
        current = order;

        if (order.length() == 0) {
            order.append("order by ");
        } else {
            order.append(", ");
        }
        order.append(path);
        appendSpace();
        if (ascending) {
            order.append("asc ");
        } else {
            order.append("desc ");
        }
        return this;
    }

    public QueryBuilder param(int id, Object o) {
        seqParams.put(id, o);
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

    /**
     * In order to support the order() method in addition
     * to a filter, we allow applying the filter and nulling
     * the instance eagerly before the user calls order.
     */
    public QueryBuilder filterNow() {

        if (filter != null && filterTarget != null) {
            if (filter.owner() >= 0) {
                this.and(filterTarget+".details.owner.id = ");
                String alias = this.unique_alias("owner");
		this.append(":");
                this.append(alias);
                this.param(alias, filter.owner());
                appendSpace();
            }
            if (filter.group() >= 0) {
                this.and(filterTarget+".details.group.id = ");
                String alias = this.unique_alias("group");
		this.append(":");
                this.append(alias);
                this.param(alias, filter.group());
                appendSpace();
            }
        }
        return this;
    }

    public Query queryWithoutFilter(Session session) {
        return __query(session, false);
    }

    public Query query(Session session) {
        return __query(session, true);
    }

    private Query __query(Session session, boolean usefilter) {

        if (usefilter) {
            filterNow();
        }

        Query q = null;
        try {
            final String s = queryString();
            if (sqlQuery) {
                // ticket:9435 - in order to allow updates with raw
                // SQL we will unwrap the session. This is the only
                // location that is doing such unwrapping.
                // Also see ticket:9496 about deleting rdefs.
                if (s.startsWith("update") || s.startsWith("delete")) {
                    if (session instanceof Advised) {
                        Advised proxy = (Advised) session;
                        try {
                            session = (Session) proxy.getTargetSource().getTarget();
                        } catch (Exception e) {
                            RuntimeException rt = new RuntimeException(e);
                            rt.initCause(e);
                            throw rt;
                        }
                    }
                }
                q = session.createSQLQuery(queryString());
            } else {
                q = session.createQuery(queryString());
            }
        } catch (RuntimeException rt) {
            // We're logging failed queries because the almost always point
            // to an internal exception that shouldn't be happening.
            log.warn("Failed query: " + queryString(), rt);
            throw rt;
        }

        for (String key : params.keySet()) {
            q.setParameter(key, params.get(key));
        }
        for (Integer key : seqParams.keySet()) {
            q.setParameter(key, seqParams.get(key));
        }
        for (String key : listParams.keySet()) {
            q.setParameterList(key, listParams.get(key));
        }
        if (filter != null) {
            if (filter.limit !=null) {
                q.setMaxResults(filter.limit);
            }
            if (filter.offset != null) {
                q.setFirstResult(filter.offset);
            }
        }
        return q;
    }

    public QueryBuilder appendSpace() {
        if (current.length() == 0) {
            current.append(' ');
        } else if (current.charAt(current.length() - 1) != ' ') {
            current.append(' ');
        }
        return this;

    }

    /**
     * Returns the current query as a String. As opposed to {@link #toString()},
     * this method should return parseable HQL.
     * @return the current HQL query
     */
    public String queryString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.select.toString());
        sb.append(this.from.toString());
        sb.append(this.join.toString());
        sb.append(this.where.toString());
        sb.append(this.group.toString());
        sb.append(this.order.toString());
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder();
        toString.append(queryString());
        toString.append(params);
        toString.append(seqParams);
        toString.append(listParams);
        return toString.toString();
    }

    // State methods
    // Used in case the standard workflow is not optimal

    public void update(String table) {
        _type("update");
        select.append(table);
        appendSpace();
        skipFrom();
    }

    public void delete(String table) {
        if (sqlQuery) {
            _type("delete from ");
        } else {
            _type("delete");
        }
        select.append(table);
        appendSpace();
        skipFrom();
    }

    public QueryBuilder skipFrom() {
        current = from;
        appendSpace();
        return this;
    }

    public QueryBuilder skipWhere() {
        current = where;
        appendSpace();
        return this;
    }

    /**
     * Similar to how skipFrom and skipWhere were previously used, this sets
     * the current builder to {@link #where()} but without prefacing the
     * "where " string. Instead, it adds a space so that further calls to
     * {@link #where()} also won't add it. This can be used to create a clause
     * that can later be combined via {@link #subselect(QueryBuilder)}.
     */
    public QueryBuilder whereClause() {
        current = where;
        appendSpace(); // Add something empty.
        return this;
     }

    public void filter(String string, Filter filter) {
        filterTarget = string;
        this.filter = filter;
    }

    public void params(Parameters params2) {
        if (params2 != null) {
            for (String key : params2.keySet()) {
                Object o = params2.get(key).value;
                if (o instanceof Collection) {
                    paramList(key, (Collection) o);
                } else {
                    param(key, o);
                }
            }
        }

    }

}
