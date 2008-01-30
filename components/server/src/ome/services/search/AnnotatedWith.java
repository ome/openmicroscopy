/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
import ome.tools.hibernate.QueryBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
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

    private static final Log log = LogFactory.getLog(AnnotatedWith.class);

    private static final long serialVersionUID = 1L;

    private final Annotation[] annotation;

    private final String[] path;

    private final Class[] annCls;

    private final Class[] type;

    private final Object[] value;

    private final boolean[] fetch;

    /**
     * copy of fetchAnnotations list, so that items which are already fetched
     * via {@link #fetch} are not fetched again.
     */
    private final List<Class> fetchAnnotationsCopy = new ArrayList<Class>();

    private final boolean useNamespace;

    private final boolean useLike;

    private final Class cls;

    public AnnotatedWith(SearchValues values, Annotation[] annotation,
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

        if (annotation == null || annotation.length == 0) {
            throw new ApiUsageException("Must specify at least one annotation.");
        }
        this.path = new String[annotation.length];
        this.annCls = new Class[annotation.length];
        this.type = new Class[annotation.length];
        this.value = new Object[annotation.length];
        this.fetch = new boolean[annotation.length];
        this.fetchAnnotationsCopy.addAll(values.fetchAnnotations);
        for (int i = 0; i < annotation.length; i++) {
            if (annotation[i] instanceof TextAnnotation) {
                annCls[i] = TextAnnotation.class;
                type[i] = String.class;
                path[i] = "textValue";
                value[i] = ((TextAnnotation) annotation[i]).getTextValue();
            } else if (annotation[i] instanceof BooleanAnnotation) {
                annCls[i] = BooleanAnnotation.class;
                type[i] = Boolean.class;
                path[i] = "boolValue";
                value[i] = ((BooleanAnnotation) annotation[i]).getBoolValue();
            } else if (annotation[i] instanceof TimestampAnnotation) {
                annCls[i] = TimestampAnnotation.class;
                type[i] = Timestamp.class;
                path[i] = "timeValue";
                value[i] = ((TimestampAnnotation) annotation[i]).getTimeValue();
            } else if (annotation[i] instanceof FileAnnotation) {
                annCls[i] = FileAnnotation.class;
                type[i] = OriginalFile.class;
                path[i] = "file";
                value[i] = ((FileAnnotation) annotation[i]).getFile();
            } else if (annotation[i] instanceof ThumbnailAnnotation) {
                annCls[i] = ThumbnailAnnotation.class;
                type[i] = Thumbnail.class;
                path[i] = "thumbnail";
                value[i] = ((ThumbnailAnnotation) annotation[i]).getThumbnail();
            } else if (annotation[i] instanceof DoubleAnnotation) {
                annCls[i] = DoubleAnnotation.class;
                type[i] = Double.class;
                path[i] = "doubleValue";
                value[i] = ((DoubleAnnotation) annotation[i]).getDoubleValue();
            } else if (annotation[i] instanceof LongAnnotation) {
                annCls[i] = LongAnnotation.class;
                type[i] = Long.class;
                path[i] = "longValue";
                value[i] = ((LongAnnotation) annotation[i]).getLongValue();
            } else {
                throw new ApiUsageException("Unsupported annotation type:"
                        + annotation);
            }
            // fetch annotations
            for (Class ac : values.fetchAnnotations) {
                if (annCls[i].isAssignableFrom(ac)) {
                    fetch[i] = true;
                    fetchAnnotationsCopy.remove(ac);
                }
            }
        }

    }

    public Object doWork(TransactionStatus status, Session session,
            ServiceFactory sf) {

        String[] link = new String[annotation.length];
        String[] ann = new String[annotation.length];

        QueryBuilder qb = new QueryBuilder();
        qb.select("this");
        qb.from(cls.getName(), "this");
        for (int i = 0; i < ann.length; i++) {
            link[i] = qb.unique_alias("link");
            ann[i] = link[i] + "_child";
            qb.join("this.annotationLinks", link[i], false, fetch[i]);
            qb.join(link[i] + ".child", ann[i], false, fetch[i]);
        }

        // fetch annotations
        for (int i = 0; i < fetchAnnotationsCopy.size(); i++) {
            qb.join("this.annotationLinks", "fetchannlink" + i, false, true);
            qb.join("fetchannlink" + i + ".child", "fetchannchild" + i, false,
                    true);
        }
        qb.where();
        for (int i = 0; i < fetchAnnotationsCopy.size(); i++) {
            qb.and("fetchannchild" + i + ".class = "
                    + fetchAnnotationsCopy.get(i).getSimpleName());
        }

        ids(qb, "this.");
        ownerOrGroup(cls, qb, "this.");
        createdOrModified(cls, qb, "this.");

        for (int j = 0; j < annotation.length; j++) {
            // Main criteria
            if (useNamespace) {
                notNullOrLikeOrEqual(qb, ann + ".name", type[j], annotation[j]
                        .getName(), useLike, values.caseSensitive);
            }

            notNullOrLikeOrEqual(qb, ann[j] + "." + path[j], type[j], value[j],
                    useLike, values.caseSensitive);

            annotatedBetween(qb, ann[j] + ".");
            annotatedBy(qb, ann[j] + ".");
        }

        // orderBy
        for (String orderBy : values.orderBy) {
            String orderByPath = orderByPath(orderBy);
            boolean ascending = orderByAscending(orderBy);
            qb.order("this." + orderByPath, ascending);
        }

        log.debug(qb.toString());
        return qb.query(session).list();
    }
}
