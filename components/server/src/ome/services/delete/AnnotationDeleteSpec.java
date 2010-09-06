/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.api.IDelete;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.FatalBeanException;

/**
 * {@link DeleteSpec} specialized for deleting annotations. Adds options which
 * classes to delete as well as which namespaces.
 *
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 */
public class AnnotationDeleteSpec extends BaseDeleteSpec {

    private final static Log log = LogFactory
            .getLog(AnnotationDeleteSpec.class);

    /**
     * Collection of namespace values (or LIKE values if they contain '%' or
     * '?') which will be deleted automatically.
     */
    public static final Set<String> nsIncludes = Collections
            .unmodifiableSet(new HashSet<String>(Arrays.asList("foo")));

    /**
     * Collection of namespace values (defined as {@link #nsIncludes}) which
     * will be omitted from deletes by default.
     */
    public static final Set<String> nsExcludes = Collections
            .unmodifiableSet(new HashSet<String>(Arrays.asList("foo")));

    //
    // Initialization-time values
    //

    public AnnotationDeleteSpec(List<String> entries) {
        super(entries);
    }

    @Override
    public void setExtendedMetadata(ExtendedMetadata em) {
        super.setExtendedMetadata(em);
        if (em.getAnnotationTypes().size() != entries.size()) {
            throw new FatalBeanException(
                    "Mismatch between anntotations defined and those found: "
                            + entries + "<> " + em.getAnnotationTypes());
        }
    }

    @Override
    protected List<Long> queryBackupIds(Session session, DeleteEntry subpath,
            QueryBuilder and) throws DeleteException {

        if (and != null) {
            throw new DeleteException(true, "Unexpected non-null and: " + and);
        }

        // Copying the entry since we cannot currently find relationships
        // to subclasses, i.e. getRelationship(ImageAnnotationLink, LongAnnotation)==null.
        final DeleteEntry copy = new DeleteEntry(this, "/Annotation", subpath.op, subpath.path);
        final QueryBuilder qb = new QueryBuilder();

        final String[] sub = copy.path(superspec);
        int which = sub.length - 1;
        final String klass = subpath.name.substring(1);

        qb.skipFrom();
        qb.skipWhere();
        qb.append("ROOT" + which + ".class = " + klass);

        return super.queryBackupIds(session, copy, and);
    }

    /**
     * Overrides {@link BaseDeleteSpec#execute(Session, DeleteEntry, List) in
     * order to delete only those ids which match the {@link #options} passed by
     * the user.
     */
    @Override
    protected String execute(Session session, DeleteEntry entry, List<Long> ids)
            throws DeleteException {
        if (true) {
            if (ids != null && ids.size() > 0) {
                Query q = session.createQuery("delete ome.model.IAnnotationLink where child.id in (:ids)");
                q.setParameterList("ids", ids);
                int count = q.executeUpdate();
                log.info("Deleted " + count + " annotation links.");
                return super.execute(session, entry, ids);
            } else {
                return "";
            }
        }
        if (options != null) {
            String typeIncludes = options.get("type.includes");
            String typeExcludes = options.get("type.excludes");
            String nsIncludes = options.get("ns.includes");
            String nsExcludes = options.get("ns.excludes");
        }

        final DeleteEntry only = entries.get(0);
        final String[] sub = only.path(superspec);
        final int which = sub.length - 1;
        final String alias = "ROOT" + which;
        final QueryBuilder qb = new QueryBuilder();

        QueryBuilder and = new QueryBuilder();
        and.skipFrom();
        and.skipWhere();
        and.append(alias + ".class = TagAnnotation");
        return null;
    }
}
