/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import ome.conditions.ApiUsageException;
import ome.model.IAnnotated;
import ome.model.internal.Details;
import ome.services.SearchBean;
import ome.system.ServiceFactory;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.transaction.TransactionStatus;

/**
 * Query template used by {@link SearchBean} to store user requests.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class FullText extends SearchAction {

    private final static QueryParser parser = new QueryParser(
            "combined_fields", new StandardAnalyzer());

    private static final long serialVersionUID = 1L;

    private final String queryStr;

    private FullTextSession session;

    private org.apache.lucene.search.Query q;

    private Query query;

    private ScrollableResults scroll;

    public FullText(SearchValues values, String query) {
        super(values);
        if (query == null || query.length() < 1) {
            throw new IllegalArgumentException("Query string must be non-empty");
        }
        this.queryStr = query;
        try {
            q = parser.parse(queryStr);
        } catch (ParseException pe) {
            ApiUsageException aue = new ApiUsageException(queryStr
                    + " caused a parse exception.");
            throw aue;
        }
    }

    public void doWork(TransactionStatus status, Session session,
            ServiceFactory sf) {

        if (values.onlyTypes == null || values.onlyTypes.size() != 1) {
            throw new ApiUsageException(
                    "Searches by full text are currently limited to a single type.\n"
                            + "Plese use Search.onlyType().");
        }

        Class cls = values.onlyTypes.get(0);

        this.session = Search.createFullTextSession(session);
        Criteria criteria = session.createCriteria(cls);
        if (values.ownedBy != null) {
            Details d = values.ownedBy;
            if (/* ownable && */d.getOwner() != null) {
                criteria.add(Restrictions.eq("details.owner.id", values.ownedBy
                        .getOwner().getId()));
            } else if (d.getGroup() != null) {
                criteria.add(Restrictions.eq("details.group.id", values.ownedBy
                        .getGroup().getId()));
            }
        }

        if (values.onlyAnnotatedWith != null) {
            if (values.onlyAnnotatedWith.size() > 0) {
                if (!IAnnotated.class.isAssignableFrom(cls)) {
                    // A non-IAnnotated object cannot have any
                    // Annotations, and so our results are null
                    result = null;
                    return; // EARLY EXIT !
                }
                Criteria anns = criteria.add(Restrictions
                        .isNotEmpty("annotationLinks"));
            } else {
                criteria.add(Restrictions.isEmpty("annotationLinks"));
            }
        }

        query = this.session.createFullTextQuery(this.q).setCriteriaQuery(
                criteria);
        // TODO And if allTypes? or multiple types
        // scroll = query.scroll(ScrollMode.FORWARD_ONLY);
        result = query.list();
    }
}
