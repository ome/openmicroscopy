/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ome.annotations.RolesAllowed;
import ome.api.IQuery;
import ome.api.ServiceInterface;
import ome.api.local.LocalQuery;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.services.SearchBean;
import ome.services.query.Query;
import ome.services.search.FullText;
import ome.services.search.SearchValues;
import ome.tools.hibernate.QueryBuilder;

import org.apache.lucene.analysis.Analyzer;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides methods for directly querying object graphs.
 *
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 *
 */
@Transactional(readOnly = true)
public class QueryImpl extends AbstractLevel1Service implements LocalQuery {

    protected Class<? extends Analyzer> analyzer;

    public void setAnalyzer(Class<? extends Analyzer> analyzer) {
        this.analyzer = analyzer;
    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return IQuery.class;
    }

    /**
     * Temporary WORKAROUND during the removal of HibernateTemplate to
     * let IQuery continue functioning.
     */
    private HibernateTemplate getHibernateTemplate() {
        return new HibernateTemplate(getSessionFactory(), false);
    }
    
    // ~ LOCAL PUBLIC METHODS
    // =========================================================================

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public boolean contains(Object obj) {
        return getHibernateTemplate().contains(obj);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void evict(Object obj) {
        getHibernateTemplate().evict(obj);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void clear() {
        getHibernateTemplate().clear();
    }

    @RolesAllowed("user")
    public void initialize(Object obj) {
        Hibernate.initialize(obj);
    }

    @RolesAllowed("user")
    @Transactional(propagation = Propagation.SUPPORTS)
    public boolean checkType(String type) {
        ClassMetadata meta = getHibernateTemplate().getSessionFactory()
                .getClassMetadata(type);
        return meta == null ? false : true;
    }

    @RolesAllowed("user")
    @Transactional(propagation = Propagation.SUPPORTS)
    public boolean checkProperty(String type, String property) {
        ClassMetadata meta = getHibernateTemplate().getSessionFactory()
                .getClassMetadata(type);
        String[] names = meta.getPropertyNames();
        for (int i = 0; i < names.length; i++) {
            // TODO: possibly with caching and Arrays.sort/search
            if (names[i].equals(property)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see LocalQuery#execute(HibernateCallback)
     */
    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    public <T> T execute(HibernateCallback callback) {
        return (T) getHibernateTemplate().execute(callback);
    }

    /**
     * @see ome.api.local.LocalQuery#execute(Query)
     */
    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    public <T> T execute(ome.services.query.Query<T> query) {
        return (T) getHibernateTemplate().execute(query);
    }

    // ~ INTERFACE METHODS
    // =========================================================================

    @Override
    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    // TODO weirdness here; learn more about CGLIB initialization.
    public IObject get(final Class klass, final long id)
            throws ValidationException {
        return (IObject) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session)
                            throws HibernateException {

                        IObject o = null;
                        try {
                            o = (IObject) session.load(klass, id);
                        } catch (ObjectNotFoundException onfe) {
                            throw new ApiUsageException(
                                    String
                                            .format(
                                                    "The requested object (%s,%s) is not available.\n"
                                                            + "Please use IQuery.find to deteremine existance.\n",
                                                    klass.getName(), id));
                        }

                        Hibernate.initialize(o);
                        return o;

                    }
                });
    }

    @Override
    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    // TODO weirdness here; learn more about CGLIB initialization.
    public IObject find(final Class klass, final long id) {
        return (IObject) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session)
                            throws HibernateException {

                        IObject o = (IObject) session.get(klass, id);
                        Hibernate.initialize(o);
                        return o;

                    }
                });
    }

    @Override
    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    public <T extends IObject> List<T> findAll(final Class<T> klass,
            final Filter filter) {
        if (filter == null) {
            return getHibernateTemplate().loadAll(klass);
        }

        return (List<T>) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session)
                            throws HibernateException, SQLException {
                        Criteria c = session.createCriteria(klass);
                        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                        parseFilter(c, filter);
                        return c.list();
                    }
                });
    }

    @Override
    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    public <T extends IObject> T findByExample(final T example)
            throws ApiUsageException {
        return (T) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                try {
                    Criteria c = session.createCriteria(example.getClass());
                    c.add(Example.create(example));
                    return c.uniqueResult();
                } catch (IncorrectResultSizeDataAccessException irsdae) {
                    throwNonUnique("findByExample");
                } catch (NonUniqueResultException nure) {
                    throwNonUnique("findByExample");
                }
                // Never reached!
                return null;
                
            }
        });
    }

    @Override
    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    public <T extends IObject> List<T> findAllByExample(final T example,
            final Filter filter) {
        return (List<T>) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session)
                            throws HibernateException {

                        Criteria c = session.createCriteria(example.getClass());
                        c.add(Example.create(example));
                        parseFilter(c, filter);
                        return c.list();

                    }
                });
    }

    @Override
    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    public <T extends IObject> T findByString(final Class<T> klass,
            final String fieldName, final String value)
            throws ApiUsageException {
        return (T) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                try {
                    Criteria c = session.createCriteria(klass);
                    c.add(Restrictions.eq(fieldName, value));
                    return c.uniqueResult();
                } catch (IncorrectResultSizeDataAccessException irsdae) {
                    throwNonUnique("findByString");
                } catch (NonUniqueResultException nure) {
                    throwNonUnique("findByString");
                }
                // Never reached
                return null;

            }
        });
    }

    @Override
    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    public <T extends IObject> List<T> findAllByString(final Class<T> klass,
            final String fieldName, final String value,
            final boolean caseSensitive, final Filter filter)
            throws ApiUsageException {
        return (List<T>) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session)
                            throws HibernateException {

                        Criteria c = session.createCriteria(klass);
                        parseFilter(c, filter);

                        if (caseSensitive) {
                            c.add(Restrictions.like(fieldName, value,
                                    MatchMode.ANYWHERE));
                        } else {
                            c.add(Restrictions.ilike(fieldName, value,
                                    MatchMode.ANYWHERE));
                        }

                        return c.list();

                    }
                });
    }

    @Override
    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    public <T extends IObject> T findByQuery(String queryName, Parameters params)
            throws ValidationException {

        if (params == null) {
            params = new Parameters();
        }

        // specify that we should only return a single value if possible
        params.unique();

        Query<T> q = getQueryFactory().lookup(queryName, params);
        T result = null;
        try {
            result = execute(q);
        } catch (ClassCastException cce) {
            throw new ApiUsageException(
                    "Query named:\n\t"
                            + queryName
                            + "\n"
                            + "has returned an Object of type "
                            + cce.getMessage()
                            + "\n"
                            + "Queries must return IObjects when using findByQuery. \n"
                            + "Please try findAllByQuery for queries which return Lists. ");
        } catch (IncorrectResultSizeDataAccessException irsdae) {
            throwNonUnique(queryName);
        } catch (NonUniqueResultException nure) {
            throwNonUnique(queryName);
        }
        return result;
    }

    private void throwNonUnique(String queryName) {
        throw new ApiUsageException(
                "Query named:\n\n\t"
                        + queryName
                        + "\n\n"
                        + "has returned more than one Object\n"
                        + "findBy methods must return a single value.\n"
                        + "Please try findAllBy methods for queries which return Lists.");
    }

    /**
     * @see ome.api.IQuery#findAllByQuery(java.lang.String,
     *      ome.parameters.Parameters)
     */
    @RolesAllowed("user")
    public <T extends IObject> List<T> findAllByQuery(String queryName,
            Parameters params) {
        Query<List<T>> q = getQueryFactory().lookup(queryName, params);
        return execute(q);
    }

    @Override
    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    public <T extends IObject> List<T> findAllByFullText(final Class<T> type,
            final String query, final Parameters params) {
        if (analyzer == null) {
            throw new ApiUsageException(
                    "IQuery not configured for full text search.\n"
                            + "Please use ome.api.Search instead.");
        }

        List<IObject> results = (List<IObject>) getHibernateTemplate().execute(
                new HibernateCallback<List<IObject>>() {
                    @SuppressWarnings("rawtypes")
                    public List<IObject> doInHibernate(Session session)
                            throws HibernateException, SQLException {
                        SearchValues values = new SearchValues();
                        values.onlyTypes = Arrays.asList((Class) type);
                        values.copy(params);
                        FullText fullText = new FullText(values, query,
                                analyzer);
                        return (List<IObject>) fullText.doWork(session, null);
                    }
                });

        if (results == null || results.size() == 0) {
            return new ArrayList<T>();
        }

        SearchBean search = new SearchBean();
        search.addParameters(params);
        search.addResult(results);
        return search.results();
    }

    // ~ Aggregations
    // =========================================================================

    @Override
    @SuppressWarnings("unchecked")
    @RolesAllowed("user")
    public List<Object[]> projection(final String query, Parameters p) {
        final Parameters params = (p == null ? new Parameters() : p);
        final Query<List<Object>> q = getQueryFactory().lookup(query, params);

        @SuppressWarnings("rawtypes")
        final List rv = (List) getHibernateTemplate().execute(q);
        final int size = rv.size();
        Object obj = null;
        for (int i = 0; i < size; i++) {
            obj = rv.get(i);
            if (obj != null) {
                if (!Object[].class.isAssignableFrom(obj.getClass())) {
                    rv.set(i, new Object[]{obj});
                }
            }
        }
        return rv;
    }

    final static Pattern AGGS  = Pattern.compile("(count|sum|max|min)");
    final static Pattern FIELD = Pattern.compile("\\w?[\\w.\\s\\+\\-\\*]*");

    @RolesAllowed("user")
    public Long aggByQuery(String agg, String field, String query,
            Parameters params) {

        if (!AGGS.matcher(agg).matches()) {
            throw new ValidationException(agg + " does not match " + AGGS);
        }

        if (!FIELD.matcher(field).matches()) {
            throw new ValidationException(field + " does not match " + FIELD);
        }

        final QueryBuilder qb = new QueryBuilder();
        qb.select(agg + "("+field+")").append(query);
        qb.params(params);
        return (Long) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session)
                            throws HibernateException, SQLException {
                        return qb.query(session).uniqueResult();
                    }
                });
    }

    @SuppressWarnings("unchecked")
    @RolesAllowed("user")
    public Map<String, Long> aggMapByQuery(String agg, String mapKey,
            String field, String query, Parameters params) {
        if (!AGGS.matcher(agg).matches()) {
            throw new ValidationException(agg + " does not match " + AGGS);
        }

        if (!FIELD.matcher(field).matches()) {
            throw new ValidationException(field + " does not match " + FIELD);
        }

        if (!FIELD.matcher(mapKey).matches()) {
            throw new ValidationException(mapKey + " does not match " + FIELD);
        }

        final QueryBuilder qb = new QueryBuilder();
        qb.select(mapKey, agg + "(" + field + ")").append(query);
        qb.append(" group by " + mapKey);
        qb.params(params);
        List<Object[]> list = (List<Object[]>) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session)
                            throws HibernateException, SQLException {
                        return qb.query(session).list();
                    }
                });

        Map<String, Long> rv = new HashMap<String, Long>();
        for (Object[] objs : list) {
            Object key = objs[0];
            Object value = objs[1];
            Long l = null;
            if (value instanceof Long) {
                l = (Long) value;
            } else if (value instanceof Integer) {
                l = ((Integer) value).longValue();
            } else {
                throw new ValidationException("Value for key " + key + " is " + value);
            }
            rv.put(key.toString(), l);
        }
        return rv;
    }


    // ~ Others
    // =========================================================================

    @Override
    public <T extends IObject> T refresh(T iObject) throws ApiUsageException {
        getHibernateTemplate().refresh(iObject);
        return iObject;
    }

    // ~ HELPERS
    // =========================================================================

    /**
     * Responsible for applying the information provided in a
     * {@link ome.parameters.Filter} to a {@link org.hibernate.Criteria}
     * instance.
     * @param c a criteria instance
     * @param f a filter to limit a query
     */
    protected void parseFilter(Criteria c, Filter f) {
        // ticket:1232 - Reverting for 4.0
        if (f != null && f.offset != null) {
            c.setFirstResult(f.offset);
        } else {
            c.setFirstResult(0);
        }
        if (f != null && f.limit != null) {
            c.setMaxResults(f.limit);
        } else {
            c.setMaxResults(Integer.MAX_VALUE);
        }
    }

}
