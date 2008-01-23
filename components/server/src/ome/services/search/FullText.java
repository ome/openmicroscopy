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
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.services.SearchBean;
import ome.system.ServiceFactory;
import ome.tools.hibernate.QueryBuilder;

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
        OwnerOrGroup oog = new OwnerOrGroup(values.ownedBy);
        if (oog.needed()) {
            oog.on(criteria);
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

        AnnotationCriteria ann = new AnnotationCriteria(criteria);
        OwnerOrGroup aoog = new OwnerOrGroup(values.annotatedBy);
        if (aoog.needed()) {
            aoog.on(ann.getChild());
        }

        if (values.annotatedStart != null) {
            ann.getCreate().add(
                    Restrictions.gt("anncreate.time", values.annotatedStart));
        }

        if (values.annotatedStop != null) {
            ann.getCreate().add(
                    Restrictions.lt("anncreate.time", values.annotatedStop));
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
                        SimpleExpression ofType = new TypeEqualityExpression(
                                "class", annCls);
                        ann.getChild().add(ofType);
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

/**
 * Function-like class to assert either first the {@link Experimenter owner} of
 * an object, or lacking that, the {@link ExperimenterGroup group}.
 */
class OwnerOrGroup {
    final Details d;

    String path;

    long id;

    OwnerOrGroup(Details d) {
        this.d = d;
    }

    boolean needed() {
        return needed("");
    }

    /**
     * @param prefix
     */
    boolean needed(String prefix) {
        if (d != null) {
            if (/* ownable && */d.getOwner() != null) {
                Long _id = d.getOwner().getId();
                if (_id == null) {
                    throw new ApiUsageException("Id for owner cannot be null.");
                }
                id = _id.longValue();
                path = prefix + "details.owner.id";
            } else if (d.getGroup() != null) {
                Long _id = d.getGroup().getId();
                if (_id == null) {
                    throw new ApiUsageException("Id for group cannot be null.");
                }
                id = _id.longValue();
                path = prefix + "details.group.id";
            }
        }
        return path != null;
    }

    void check() {
        if (path == null) {
            throw new ApiUsageException("Please call \"needs()\" first.");
        }
    }

    /**
     * @param criteria
     *            Should be not not null and should have a path of the form
     *            "details.owner.id" an "details.group.id".
     */
    void on(Criteria criteria) {
        check();
        criteria.add(Restrictions.eq(path, id));
    }

    void on(QueryBuilder qb) {
        check();
        String unique = path.replaceAll("[.]", "_");
        qb.append(path);
        qb.append(" = ");
        qb.append(" :");
        qb.append(unique);
        qb.param(unique, id);
    }
}

/**
 * Lazy loading class for {@link Criteria} instances related to annotations.
 * Otherwise the null checks get absurd.
 */
class AnnotationCriteria {
    final Criteria base;
    Criteria annotationLinks;
    Criteria annotationChild;
    Criteria annCreateAlias;

    AnnotationCriteria(Criteria base) {
        this.base = base;
    }

    Criteria getLinks() {
        if (annotationLinks == null) {
            annotationLinks = base.createCriteria("annotationLinks");
        }
        return annotationLinks;
    }

    Criteria getChild() {
        if (annotationChild == null) {
            annotationChild = getLinks().createCriteria("child");
        }
        return annotationChild;
    }

    Criteria getCreate() {
        if (annCreateAlias == null) {
            annCreateAlias = getChild().createAlias("details.creationEvent",
                    "anncreate");
        }
        return annCreateAlias;
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