/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.util.List;

import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.services.SearchBean;

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

    public synchronized void init(Session session) {
        this.session = Search.createFullTextSession(session);
        Criteria criteria = session.createCriteria(values.onlyTypes.get(0));
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
        query = this.session.createFullTextQuery(this.q).setCriteriaQuery(
                criteria);
        // TODO And if allTypes? or multiple types
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T extends IObject> List<T> getNext() {
        // scroll = query.scroll(ScrollMode.FORWARD_ONLY);
        return query.list();
    }

}
