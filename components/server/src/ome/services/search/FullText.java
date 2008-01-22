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
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.engine.TypedValue;
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
                Long id = d.getOwner().getId();
                if (id == null) {
                    throw new ApiUsageException("Id for owner cannot be null.");
                }
                criteria.add(Restrictions.eq("details.owner.id", id));
            } else if (d.getGroup() != null) {
                Long id = d.getGroup().getId();
                if (id == null) {
                    throw new ApiUsageException("Id for group cannot be null.");
                }
                criteria.add(Restrictions.eq("details.group.id", id));
            }
        }

        criteria.createAlias("details.creationEvent", "create");
        criteria.createAlias("details.updateEvent", "update");

        if (values.createdStart != null) {
            criteria.add(Restrictions.gt("create.time", values.createdStart));
        }

        if (values.createdStop != null) {
            criteria.add(Restrictions.lt("create.time", values.createdStop));
        }

        if (values.modifiedStart != null) {
            criteria.add(Restrictions.gt("update.time", values.modifiedStart));
        }

        if (values.modifiedStop != null) {
            criteria.add(Restrictions.lt("update.time", values.modifiedStop));
        }

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
                    result = null;
                    return; // EARLY EXIT !
                } else {
                    for (Class annCls : values.onlyAnnotatedWith) {
                        Criteria links = criteria
                                .createCriteria("annotationLinks");
                        Criteria child = links.createCriteria("child");

                        SimpleExpression ofType = new TypeEqualityExpression(
                                "class", annCls);
                        child.add(ofType);
                    }
                }
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

// Copied from http://opensource.atlassian.com/projects/hibernate/browse/HHH-746
class TypeEqualityExpression extends SimpleExpression {

    private final Class classValue;
    private final String classPropertyName;

    public TypeEqualityExpression(String propertyName, Class value) {
        super(propertyName, value, "=");
        this.classPropertyName = propertyName;
        this.classValue = value;
    }

    @Override
    public TypedValue[] getTypedValues(Criteria criteria,
            CriteriaQuery criteriaQuery) throws HibernateException {

        return new TypedValue[] { fixDiscriminatorTypeValue(criteriaQuery
                .getTypedValue(criteria, classPropertyName, classValue)) };

    }

    private TypedValue fixDiscriminatorTypeValue(TypedValue typedValue) {
        Object value = typedValue.getValue();

        // check to make sure we can reconstruct an equivalent TypedValue
        if (!String.class.isInstance(value)
                || !typedValue.equals(new TypedValue(typedValue.getType(),
                        typedValue.getValue(), EntityMode.POJO))) {
            return typedValue;
        }

        /** replace leading and trailing apostrophes* */
        String svalue = value.toString();

        if (svalue.charAt(0) == '\''
                && svalue.charAt(svalue.length() - 1) == '\'') {
            value = svalue.substring(0, svalue.length() - 1).substring(1);
            /** ***************************************** */
        }

        if (!value.equals(typedValue.getValue())) {
            return new TypedValue(typedValue.getType(), value, EntityMode.POJO);
        } else {
            return typedValue;
        }
    }

}