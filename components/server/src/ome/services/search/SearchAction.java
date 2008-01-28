/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import ome.conditions.InternalException;
import ome.model.IGlobal;
import ome.services.SearchBean;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 * Serializable action used by {@link SearchBean} to generate results lazily.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public abstract class SearchAction implements ome.services.util.Executor.Work {

    protected final SearchValues values = new SearchValues();

    public SearchAction(SearchValues values) {
        if (values == null) {
            throw new IllegalArgumentException(
                    "SearchValues argument must not be null");
        }
        this.values.copy(values);
    }

    protected void ownerOrGroup(Class cls, Criteria criteria) {
        if (!IGlobal.class.isAssignableFrom(cls)) {
            OwnerOrGroup oog = new OwnerOrGroup(values.ownedBy, "");
            if (oog.needed()) {
                oog.on(criteria);
            }
        }
    }

    protected void createdOrModified(Class cls, Criteria criteria) {
        CreatedOrModified com = new CreatedOrModified(cls, criteria, values);
    }

    protected void annotatedBy(AnnotationCriteria ann) {
        OwnerOrGroup aoog = new OwnerOrGroup(values.annotatedBy);
        if (aoog.needed()) {
            aoog.on(ann.getChild());
        }
    }

    protected void annotatedBetween(AnnotationCriteria ann) {
        AnnotatedBetween abetween = new AnnotatedBetween(ann, values);
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
