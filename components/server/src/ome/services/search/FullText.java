/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.util.List;

import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.IAnnotated;
import ome.services.SearchBean;
import ome.system.ServiceFactory;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.transaction.TransactionStatus;

/**
 * Search based on Lucene's {@link Query} class. Takes a Google-like search
 * string and returns fully formed objects via Hibernate Search.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class FullText extends SearchAction {

    private static final long serialVersionUID = 1L;

    private final String queryStr;

    private final org.apache.lucene.search.Query q;

    public FullText(SearchValues values, String query) {
        super(values);

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
            final QueryParser parser = new QueryParser("combined_fields",
                    new StandardAnalyzer());
            parser.setAllowLeadingWildcard(values.leadingWildcard);
            q = parser.parse(queryStr);
        } catch (ParseException pe) {
            ApiUsageException aue = new ApiUsageException(queryStr
                    + " caused a parse exception.");
            throw aue;
        }
    }

    public Object doWork(TransactionStatus status, Session s, ServiceFactory sf) {

        final Class<?> cls = values.onlyTypes.get(0);

        FullTextSession session = Search.createFullTextSession(s);
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

        // Main query
        FullTextQuery ftQuery = session.createFullTextQuery(this.q, cls);
        ftQuery.setCriteriaQuery(criteria);

        // orderBy
        if (values.orderBy.size() > 0) {
            SortField[] sorts = new SortField[values.orderBy.size()];
            for (int i = 0; i < sorts.length; i++) {
                String orderBy = values.orderBy.get(i);
                String orderWithoutMode = orderByPath(orderBy);
                boolean ascending = orderByAscending(orderBy);
                if (ascending) {
                    sorts[i] = new SortField(orderWithoutMode,
                            SortField.STRING, false);
                } else {
                    sorts[i] = new SortField(orderWithoutMode,
                            SortField.STRING, true);
                }
            }
            ftQuery.setSort(new Sort(sorts));
        }

        final String ticket975 = "ticket:975 - Wrong return type: %s instead of %s\n"
                + "Under some circumstances, byFullText and related methods \n"
                + "like bySomeMustNone can return instances of the wrong \n"
                + "types. One known case is the use of onlyAnnotatedWith(). \n"
                + "If you are recieving this error, please try using the \n"
                + "intersection/union methods to achieve the same results.";

        List<?> check975 = ftQuery.list();

        // WORKAROUND
        for (Object object : check975) {
            if (!cls.isAssignableFrom(object.getClass())) {
                throw new ApiUsageException(String.format(ticket975, object
                        .getClass(), cls));
            }
        }
        return check975;
    }
}
