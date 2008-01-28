/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.sql.Timestamp;

import ome.conditions.ApiUsageException;
import ome.model.annotations.Annotation;
import ome.model.annotations.BooleanAnnotation;
import ome.model.annotations.DoubleAnnotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.LongAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.annotations.ThumbnailAnnotation;
import ome.model.annotations.TimestampAnnotation;
import ome.model.core.OriginalFile;
import ome.model.display.Thumbnail;
import ome.model.internal.Details;
import ome.system.ServiceFactory;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.springframework.transaction.TransactionStatus;

/**
 * Query for {@link ome.api.Search} which uses an example {@link Annotation}
 * instance as the basis for comparison. Instances of the specified
 * {@link SearchValues#onlyTypes type} are found with a matching annotation.
 * 
 * Currently only the class of the annotation and its main attribute --
 * {@link TextAnnotation#textValue}, {@link FileAnnotation#file}, etc. -- are
 * considered. Use the other methods on {@link ome.api.Search} like
 * {@link ome.api.Search#onlyOwnedBy(Details)} to refine your search.
 * 
 * Ignores {@link ome.api.Search#onlyAnnotatedWith(Class...)}
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class AnnotatedWith extends SearchAction {

    private static final long serialVersionUID = 1L;

    private final Annotation annotation;

    private final String path;

    private final Class annCls;

    private final Class type;

    private final Object value;

    private final boolean useNamespace;

    private final boolean useLike;

    private final Class cls;

    public AnnotatedWith(SearchValues values, Annotation annotation,
            boolean useNamespace, boolean useLike) {
        super(values);
        this.annotation = annotation;
        this.useNamespace = useNamespace;
        this.useLike = useLike;

        // Note: It should be possible to set cls = IAnnotated.class, but
        // there must be a way to then remove the conditionals for
        // IGlobals below.
        if (values.onlyTypes == null || values.onlyTypes.size() != 1) {
            throw new ApiUsageException(
                    "Searches by annotated with are currently limited "
                            + "to a single type\n"
                            + "Plese use Search.onlyType()");
        } else {
            cls = values.onlyTypes.get(0);
        }

        if (annotation instanceof TextAnnotation) {
            annCls = TextAnnotation.class;
            type = String.class;
            path = "textValue";
            value = ((TextAnnotation) annotation).getTextValue();
        } else if (annotation instanceof BooleanAnnotation) {
            annCls = BooleanAnnotation.class;
            type = Boolean.class;
            path = "boolValue";
            value = ((BooleanAnnotation) annotation).getBoolValue();
        } else if (annotation instanceof TimestampAnnotation) {
            annCls = TimestampAnnotation.class;
            type = Timestamp.class;
            path = "timeValue";
            value = ((TimestampAnnotation) annotation).getTimeValue();
        } else if (annotation instanceof FileAnnotation) {
            annCls = FileAnnotation.class;
            type = OriginalFile.class;
            path = "file";
            value = ((FileAnnotation) annotation).getFile();
        } else if (annotation instanceof ThumbnailAnnotation) {
            annCls = ThumbnailAnnotation.class;
            type = Thumbnail.class;
            path = "thumbnail";
            value = ((ThumbnailAnnotation) annotation).getThumbnail();
        } else if (annotation instanceof DoubleAnnotation) {
            annCls = DoubleAnnotation.class;
            type = Double.class;
            path = "doubleValue";
            value = ((DoubleAnnotation) annotation).getDoubleValue();
        } else if (annotation instanceof LongAnnotation) {
            annCls = LongAnnotation.class;
            type = Long.class;
            path = "longValue";
            value = ((LongAnnotation) annotation).getLongValue();
        } else {
            throw new ApiUsageException("Unsupported annotation type:"
                    + annotation);
        }

    }

    public Object doWork(TransactionStatus status, Session session,
            ServiceFactory sf) {

        Criteria criteria = session.createCriteria(cls);
        AnnotationCriteria ann = new AnnotationCriteria(criteria);

        // Main criteria
        if (useNamespace) {
            ann.getChild().add(
                    notNullOrLikeOrEqual("name", type, annotation.getName(),
                            useLike, values.caseSensitive));
        }

        ann.getChild().add(
                notNullOrLikeOrEqual(path, type, value, useLike,
                        values.caseSensitive));

        ownerOrGroup(cls, criteria);
        createdOrModified(cls, criteria);
        annotatedBy(ann);
        annotatedBetween(ann);

        // orderBy
        for (String orderBy : values.orderBy) {
            String orderByPath = orderByPath(orderBy);
            boolean ascending = orderByAscending(orderBy);
            if (ascending) {
                criteria.addOrder(Order.asc(orderByPath));
            } else {
                criteria.addOrder(Order.desc(orderByPath));
            }
        }

        return criteria.list();
    }
}
