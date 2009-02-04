/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.util.ArrayList;
import java.util.List;

import ome.model.IObject;
import ome.model.annotations.TagAnnotation;
import ome.services.SearchBean;
import ome.system.ServiceFactory;
import ome.tools.hibernate.QueryBuilder;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

/**
 * Query template used by {@link SearchBean} to store user requests.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class TagsAndGroups extends SearchAction {

    private static final long serialVersionUID = 1L;

    private final String str;

    private final boolean byTagForGroups;

    public TagsAndGroups(SearchValues values, String query,
            boolean byTagForGroups) {
        super(values);
        this.byTagForGroups = byTagForGroups;
        if (query == null || query.length() < 1) {
            str = null;
        } else {
            this.str = query;
        }
    }

    @Transactional(readOnly = true)
    public Object doWork(Session session, ServiceFactory sf) {

        // Ignore:
        // values.onlyTypes
        // annotatedWith
        // onlyIds

        QueryBuilder qb = new QueryBuilder(128);
        qb.select("target.textValue");
        qb.from("AnnotationAnnotationLink", "link");
        if (byTagForGroups) {
            qb.join("link.parent", "source", false, false);
            qb.join("link.child", "target", false, false);
        } else {
            qb.join("link.parent", "target", false, false);
            qb.join("link.child", "source", false, false);
        }
        qb.where();
        qb.and("source.class = TagAnnotation");
        qb.and("target.class = TagAnnotation");
        if (str != null) {
            qb.and("source.textValue = :str");
            qb.param("str", str);
        }

        ownerOrGroup(TagAnnotation.class, qb, "source.");
        createdOrModified(TagAnnotation.class, qb, "source.");
        if (byTagForGroups) {
            annotatedBy(qb, "source.");
            annotatedBetween(qb, "source.");
        } else {
            annotatedBy(qb, "target.");
            annotatedBetween(qb, "target.");
        }

        Query query = qb.query(session);

        List<IObject> rv = new ArrayList<IObject>();
        List<String> tags = query.list();
        for (String tag : tags) {
            TagAnnotation ta = new TagAnnotation();
            ta.setTextValue(tag);
            rv.add(ta);
        }
        return rv;
    }
}
