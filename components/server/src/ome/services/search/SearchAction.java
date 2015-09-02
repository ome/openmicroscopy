/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.io.Serializable;
import java.util.List;

import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.model.IGlobal;
import ome.model.IMutable;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.services.SearchBean;
import ome.tools.hibernate.QueryBuilder;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.engine.TypedValue;

/**
 * Serializable action used by {@link SearchBean} to generate results lazily.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public abstract class SearchAction implements Serializable,
        ome.services.util.Executor.Work {

    protected final SearchValues values = new SearchValues();

    /**
     * List of {@link IObject} instances which have currently been found. This
     * {@link SearchAction} may want to take these values into account if
     * present.
     */
    protected List<IObject> chainedList;

    public SearchAction(SearchValues values) {
        if (values == null) {
            throw new IllegalArgumentException(
                    "SearchValues argument must not be null");
        }
        this.values.copy(values);
    }

    /**
     * Returns the {@link SearchAction} subclass as the description.
     */
    public String description() {
        return this.getClass().getName();
    }
    
    public SearchValues copyOfValues() {
        SearchValues copy = new SearchValues();
        copy.copy(this.values);
        return copy;
    }

    /**
     * Set the current list of found ids from previous searches, which should be
     * chained in this search. See the documentation on each by* method in
     * {@link ome.api.Search} to know how chained ids will be used, if at all.
     * 
     * @param idList
     *            Can be null to disabled chaining.
     */
    public void chainedSearch(List<IObject> chainedList) {
        this.chainedList = chainedList;
    }

    protected void ids(Criteria criteria) {
        ids(criteria, null, null);
    }

    protected void ids(QueryBuilder qb, String path) {
        ids(null, qb, path);
    }

    private void ids(Criteria criteria, QueryBuilder qb, String path) {
        if (values.onlyIds != null) {
            if (criteria != null) {
                criteria.add(Restrictions.in("id", values.onlyIds));
            }

            if (qb != null) {
                String unique = qb.unique_alias("ids");
                qb.and(String.format("%sid in (:%s) ", path, unique));
                qb.paramList(unique, values.onlyIds);
            }
        }
    }

    protected void ownerOrGroup(Class cls, Criteria criteria) {
        ownerOrGroup(cls, criteria, null, null);
    }

    protected void ownerOrGroup(Class cls, QueryBuilder qb, String path) {
        ownerOrGroup(cls, null, qb, path);
    }

    private void ownerOrGroup(Class cls, Criteria criteria, QueryBuilder qb,
            String path) {
        if (!IGlobal.class.isAssignableFrom(cls)) {
            OwnerOrGroup oog = new OwnerOrGroup(values.ownedBy, path);
            if (oog.needed()) {
                if (criteria != null) {
                    oog.on(criteria);
                }
                if (qb != null) {
                    oog.on(qb);
                }
            }
            OwnerOrGroup noog = new OwnerOrGroup(values.notOwnedBy, path);
            if (noog.needed()) {
                if (criteria != null) {
                    noog.on(criteria, false);
                }
                if (qb != null) {
                    noog.on(qb, false);
                }
            }

        }
    }

    protected void createdOrModified(Class cls, Criteria criteria) {
        createdOrModified(cls, criteria, null, null);
    }

    protected void createdOrModified(Class cls, QueryBuilder qb, String path) {
        createdOrModified(cls, null, qb, path);
    }

    private void createdOrModified(Class cls, Criteria criteria,
            QueryBuilder qb, String path) {

        if (!IGlobal.class.isAssignableFrom(cls)) {

            if (criteria != null) {
                criteria.createAlias("details.creationEvent", "create");
            }

            if (values.createdStart != null) {
                if (criteria != null) {
                    criteria.add(Restrictions.gt("create.time",
                            values.createdStart));
                }
                if (qb != null) {
                    String ctime = qb.unique_alias("ctimestart");
                    qb.and(path + "details.creationEvent.time > :" + ctime);
                    qb.param(ctime, values.createdStart);
                }
            }

            if (values.createdStop != null) {
                if (criteria != null) {
                    criteria.add(Restrictions.lt("create.time",
                            values.createdStop));
                }
                if (qb != null) {
                    String ctime = qb.unique_alias("ctimestop");
                    qb.and(path + "details.creationEvent.time < :" + ctime);
                    qb.param(ctime, values.createdStop);
                }
            }

            if (IMutable.class.isAssignableFrom(cls)) {

                if (criteria != null) {
                    criteria.createAlias("details.updateEvent", "update");
                }

                if (values.modifiedStart != null) {
                    if (criteria != null) {
                        criteria.add(Restrictions.gt("update.time",
                                values.modifiedStart));
                    }

                    if (qb != null) {
                        String mtime = qb.unique_alias("mtimestart");
                        qb.and(path + "details.updateEvent.time > :" + mtime);
                        qb.param(mtime, values.modifiedStart);
                    }
                }

                if (values.modifiedStop != null) {
                    if (criteria != null) {
                        criteria.add(Restrictions.lt("update.time",
                                values.modifiedStop));
                    }

                    if (qb != null) {
                        String mtime = qb.unique_alias("mtimestart");
                        qb.and(path + "details.updateEvent.time < :" + mtime);
                        qb.param(mtime, values.modifiedStop);
                    }
                }
            }
        }
    }

    protected void annotatedBy(AnnotationCriteria ann) {
        annotatedBy(ann, null, null);
    }

    protected void annotatedBy(QueryBuilder qb, String path) {
        annotatedBy(null, qb, path);
    }

    private void annotatedBy(AnnotationCriteria ann, QueryBuilder qb,
            String path) {
        OwnerOrGroup aoog = new OwnerOrGroup(values.annotatedBy, path);
        if (aoog.needed()) {
            if (ann != null) {
                aoog.on(ann.getChild());
            }
            if (qb != null) {
                aoog.on(qb);
            }
        }
        OwnerOrGroup naoog = new OwnerOrGroup(values.notAnnotatedBy, path);
        if (naoog.needed()) {
            if (ann != null) {
                naoog.on(ann.getChild(), false);
            }
            if (qb != null) {
                naoog.on(qb, false);
            }
        }
    }

    protected void annotatedBetween(AnnotationCriteria ann) {
        annotatedBetween(ann, null, null);
    }

    protected void annotatedBetween(QueryBuilder qb, String path) {
        annotatedBetween(null, qb, path);
    }

    private void annotatedBetween(AnnotationCriteria ann, QueryBuilder qb,
            String path) {
        if (values.annotatedStart != null) {
            if (ann != null) {
                ann.getCreate().add(
                        Restrictions
                                .gt("anncreate.time", values.annotatedStart));
            }

            if (qb != null) {
                String astart = qb.unique_alias("astart");
                qb.and(path + "details.creationEvent.time > :" + astart);
                qb.param(astart, values.annotatedStart);
            }
        }

        if (values.annotatedStop != null) {
            if (ann != null) {
                ann.getCreate()
                        .add(
                                Restrictions.lt("anncreate.time",
                                        values.annotatedStop));
            }

            if (qb != null) {
                String astop = qb.unique_alias("astop");
                qb.and(path + "details.creationEvent.time < :" + astop);
                qb.param(astop, values.annotatedStop);
            }
        }
    }

    public static void notNullOrLikeOrEqual(QueryBuilder qb, String path,
            Class type, Object value, boolean useLike, boolean caseSensitive) {
        if (null == value) {
            qb.and(path + " is null ");
        } else {
            String operator;
            if (useLike && String.class.isAssignableFrom(type)) {
                if (caseSensitive) {
                    operator = "like";
                } else {
                    operator = "ilike";
                }
            } else {
                operator = "=";
            }
            String alias = qb.unique_alias("main");
            qb.and(path);
            qb.append(operator);
            qb.append(":");
            qb.append(alias);
            qb.appendSpace();
            qb.param(alias, value);
        }
    }

    public static Criterion notNullOrLikeOrEqual(String path, Class type,
            Object value, boolean useLike, boolean caseSensitive) {
        if (null == value) {
            return Restrictions.isNull(path);
        } else if (useLike && String.class.isAssignableFrom(type)) {
            if (caseSensitive) {
                return Restrictions.like(path, value);
            } else {
                return Restrictions.ilike(path, value);
            }
        } else {
            return Restrictions.eq(path, value);
        }
    }

    public static String orderByPath(String orderBy) {
        String orderWithoutMode = orderBy.substring(1, orderBy.length());
        return orderWithoutMode;
    }

    public static boolean orderByAscending(String orderBy) {
        if (orderBy.startsWith("A")) {
            return true;
        } else if (orderBy.startsWith("D")) {
            return false;
        } else {
            throw new InternalException(
                    "Unsupported orderBy mode added to values.orderBy");
        }
    }
}

/**
 * Function-like class to assert either first the {@link Experimenter owner} of
 * an object, or lacking that, the {@link ExperimenterGroup group}.
 */
class OwnerOrGroup {

    String path;

    long id;

    OwnerOrGroup(Details d) {
        this(d, "");
    }

    OwnerOrGroup(Details d, String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        if (d != null) {
            if (d.getOwner() != null) {
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
    }

    boolean needed() {
        return path != null;
    }

    private void check() {
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
        on(criteria, true);
    }

    void on(Criteria criteria, boolean equals) {
        check();
        if (equals) {
            criteria.add(Restrictions.eq(path, id));
        } else {
            criteria.add(Restrictions.ne(path, id));
        }
    }

    void on(QueryBuilder qb) {
        on(qb, true);
    }

    void on(QueryBuilder qb, boolean equals) {
        check();
        String op = equals ? "=" : "!=";
        String unique = qb.unique_alias("owner");
        qb.and(path);
        qb.append(" ");
        qb.append(op);
        qb.append(" :");
        qb.append(unique);
        qb.appendSpace();
        qb.param(unique, id);
    }
}

/**
 * Lazy loading class for {@link Criteria} instances related to annotations.
 * Otherwise the null checks get absurd.
 */
class AnnotationCriteria {
    final Criteria base;
    final List<Class> fetchAnnotations;
    Criteria annotationLinks;
    Criteria annotationChild;
    Criteria annCreateAlias;
    final int joinType;

    AnnotationCriteria(Criteria base, List<Class> fetchAnnotations) {
        this.base = base;
        this.fetchAnnotations = fetchAnnotations;
        if (fetchAnnotations.size() > 0) {
            joinType = Criteria.LEFT_JOIN;
            base.setFetchMode("annotationLinks", FetchMode.JOIN);
            getLinks().setFetchMode("child", FetchMode.JOIN);
        } else {
            joinType = Criteria.INNER_JOIN;
        }
    }

    Criteria getLinks() {
        if (annotationLinks == null) {
            annotationLinks = base.createCriteria("annotationLinks", joinType);
        }
        return annotationLinks;
    }

    Criteria getChild() {
        if (annotationChild == null) {
            annotationChild = getLinks().createCriteria("child", joinType);
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

        //
        // If begins and ends with a quote,
        // then replace leading and trailing apostrophes,
        // otherwise return
        //
        String svalue = value.toString();
        if (svalue.charAt(0) == '\''
                && svalue.charAt(svalue.length() - 1) == '\'') {
            value = svalue.substring(1, svalue.length() - 1);
            return new TypedValue(typedValue.getType(), value, EntityMode.POJO);
        } else {
            return typedValue;
        }
    }

}
