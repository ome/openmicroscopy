/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ome.conditions.ApiUsageException;
import ome.model.IAnnotated;
import ome.model.IObject;
import ome.system.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.ProjectionConstants;
import org.hibernate.search.Search;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Search based on Lucene's {@link Query} class. Takes a Google-like search
 * string and returns fully formed objects via Hibernate Search.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class FullText extends SearchAction {

    public final static String ALL_PROJECTIONS = "__ALL_PROJECTIONS";

    public final static String TOTAL_SIZE = "TOTAL_SIZE";

    private static final Log log = LogFactory.getLog(FullText.class);

    private static final long serialVersionUID = 1L;

    private final String queryStr;

    private final org.apache.lucene.search.Query q;

    private final Class<? extends Analyzer> analyzer;

    public FullText(SearchValues values, String query,
            Class<? extends Analyzer> analyzer) {
        super(values);
        Assert.notNull(analyzer, "Analyzer required");
        this.analyzer = analyzer;

        if (values.onlyTypes == null || values.onlyTypes.size() != 1) {
            throw new ApiUsageException(
                    "Searches by full text are currently limited to a single type.\n"
                            + "Plese use Search.onlyType()");
        }

        if (query == null || query.length() < 1) {
            throw new IllegalArgumentException("Query string must be non-empty");
        }

        if ((query.startsWith("*") || query.startsWith("?"))
                && !values.leadingWildcard) {
            throw new ApiUsageException("Searches starting with a leading "
                    + "wildcard (*,?) can be slow.\nPlease use "
                    + "setAllowLeadingWildcard() to permit this usage.");
        }

        if (query.equals("*")) {
            throw new ApiUsageException(
                    "Wildcard searches (*) must contain more than a single wildcard. ");
        }

        this.queryStr = query;
        try {
            final Analyzer a = analyzer.newInstance();
            final QueryParser parser = new /*Analyzing*/QueryParser("combined_fields", a);
            parser.setAllowLeadingWildcard(values.leadingWildcard);
            q = parser.parse(queryStr);
        } catch (ParseException pe) {
            final String msg = queryStr + " caused a parse exception: " +
                pe.getMessage();
            // No longer logging these, since it's a simple user error
            ApiUsageException aue = new ApiUsageException(msg);
            throw aue;
        } catch (InstantiationException e) {
            ApiUsageException aue = new ApiUsageException(analyzer.getName()
                    + " cannot be instantiated.");
            throw aue;
        } catch (IllegalAccessException e) {
            ApiUsageException aue = new ApiUsageException(analyzer.getName()
                    + " cannot be instantiated.");
            throw aue;
        }
    }

    private Criteria criteria(FullTextSession session) {
        final Class<?> cls = values.onlyTypes.get(0);
        Criteria criteria = session.createCriteria(cls);
        AnnotationCriteria ann = new AnnotationCriteria(criteria,
                values.fetchAnnotations);

        ids(criteria);
        ownerOrGroup(cls, criteria);
        createdOrModified(cls, criteria);
        annotatedBy(ann);
        annotatedBetween(ann);

        // annotatedWith
        if (values.onlyAnnotatedWith != null) {
            if (values.onlyAnnotatedWith.size() > 1) {
                throw new ApiUsageException(
                        "HHH-879: "
                                + "At the moment Hibernate cannot fulfill this request.\n"
                                + "Please use only a single onlyAnnotatedWith "
                                + "parameter when performing full text searches.");
            } else if (values.onlyAnnotatedWith.size() > 0) {
                if (!IAnnotated.class.isAssignableFrom(cls)) {
                    // A non-IAnnotated object cannot have any
                    // Annotations, and so our results are null
                    return null; // EARLY EXIT !
                } else {
                    for (Class<?> annCls : values.onlyAnnotatedWith) {
                        SimpleExpression ofType = new TypeEqualityExpression(
                                "class", annCls);
                        ann.getChild().add(ofType);
                    }
                }
            } else {
                criteria.add(Restrictions.isEmpty("annotationLinks"));
            }
        }

        // orderBy
        if (values.orderBy.size() > 0) {
            for (int i = 0; i < values.orderBy.size(); i++) {
                String orderBy = values.orderBy.get(i);
                String orderWithoutMode = orderByPath(orderBy);
                boolean ascending = orderByAscending(orderBy);
                if (ascending) {
                    criteria.addOrder(Order.asc(orderWithoutMode));
                } else {
                    criteria.addOrder(Order.desc(orderWithoutMode));
                }
            }
        }
        return criteria;
    }

    /**
     * Allows settings offset and limit on the query. The default implementation
     * calls setProjection with SCORE and ID, which MUST BE the first two
     * projection values. Any overriding method may add further projections but
     * must start with these two.
     *
     * @param ftQuery
     */
    protected void initializeQuery(FullTextQuery ftQuery) {
        ftQuery
        .setProjection(ProjectionConstants.SCORE,
                ProjectionConstants.ID);
    }

    @Transactional(readOnly = true)
    public Object doWork(Session s, ServiceFactory sf) {

        final Class<?> cls = values.onlyTypes.get(0);
        FullTextSession session = Search.createFullTextSession(s);
        Criteria criteria = criteria(session);
        if (criteria == null) {
            return null; // EARLY EXIT. See criteria method.
        }

        final String ticket975 = "ticket:975 - Wrong return type: %s instead of %s\n"
                + "Under some circumstances, byFullText and related methods \n"
                + "like bySomeMustNone can return instances of the wrong \n"
                + "types. One known case is the use of onlyAnnotatedWith(). \n"
                + "If you are recieving this error, please try using the \n"
                + "intersection/union methods to achieve the same results.";

        // Main query
        FullTextQuery ftQuery = session.createFullTextQuery(this.q, cls);
        initializeQuery(ftQuery);
        List<?> result = ftQuery.list();
        int totalSize = ftQuery.getResultSize();

        if (result.size() == 0) {
            // EARLY EXIT 
            return result; // of wrong type but with generics it doesn't matter
        }

        final Map<Long, Integer> order = new HashMap<Long, Integer>();
        final Map<Long, Float> scores = new HashMap<Long, Float>();
        final Map<Long, Object[]> projections = new HashMap<Long, Object[]>();
        for (int i = 0; i < result.size(); i++) {
            Object[] parts = (Object[]) result.get(i);
            scores.put((Long) parts[1], (Float) parts[0]);
            order.put((Long) parts[1], i);
            projections.put((Long) parts[1], parts);
        }

        // TODO Could add a performance optimization here on returnUnloaded

        final LinkedList<Long> ids = new LinkedList<Long>(scores.keySet());
        final List<IObject> check975 = new ArrayList<IObject>();

        while (ids.size() > 0) {
            final List<Long> page = new ArrayList<Long>();
            for (int i = 0; i < 1000 && ids.size() > 0; i++) {
                page.add(ids.removeFirst());
            }
            if (criteria == null) {
                criteria = criteria(session);
            }
            criteria.add(Restrictions.in("id", page));
            check975.addAll(criteria.list());
            criteria = null;
        }

        for (IObject object : check975) {
            // TODO This is now all but impossible. Remove
            if (!cls.isAssignableFrom(object.getClass())) {
                throw new ApiUsageException(String.format(ticket975, object
                        .getClass(), cls));
            } else {
                object.putAt(TOTAL_SIZE, totalSize);
                object.putAt(ProjectionConstants.SCORE, scores.get(object
                        .getId()));
                object.putAt(ALL_PROJECTIONS, projections.get(object.getId()));
            }
        }

        // Order return value based on the original ordering

        final Comparator cmp = new Comparator() {
            public int compare(Object obj1, Object obj2) {
                IObject o1 = (IObject) obj1;
                IObject o2 = (IObject) obj2;
                Long id1 = o1.getId();
                Long id2 = o2.getId();
                Integer idx1 = order.get(id1);
                Integer idx2 = order.get(id2);
                return idx1.compareTo(idx2);
            }
        };
        Collections.sort(check975, cmp);
        return check975;
    }

    public Float getScore(IObject object) {
        Object o = object.retrieve(ProjectionConstants.SCORE);
        if (o instanceof Float) {
            return (Float) o;
        }
        return null;
    }

    public Integer getTotalSize(IObject object) {
        Object o = object.retrieve(TOTAL_SIZE);
        if (o instanceof Integer) {
            return (Integer) o;
        }
        return null;
    }

    public Object[] getProjections(IObject object) {
        Object o = object.retrieve(ALL_PROJECTIONS);
        if (o instanceof Object[]) {
            return (Object[]) o;
        }
        return null;
    }
}
