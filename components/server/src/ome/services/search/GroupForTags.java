/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.util.ArrayList;
import java.util.List;

import ome.conditions.ApiUsageException;
import ome.model.IAnnotated;
import ome.model.IObject;
import ome.model.annotations.TagAnnotation;
import ome.model.internal.Details;
import ome.services.SearchBean;
import ome.system.ServiceFactory;
import ome.tools.hibernate.QueryBuilder;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;

/**
 * Query template used by {@link SearchBean} to store user requests.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class GroupForTags extends SearchAction {

    private final static QueryParser parser = new QueryParser(
            "combined_fields", new StandardAnalyzer());

    private static final long serialVersionUID = 1L;

    private final String groupStr;

    public GroupForTags(SearchValues values, String query) {
        super(values);
        if (query == null || query.length() < 1) {
            groupStr = null;
        } else {
            this.groupStr = query;
        }
    }

    public void doWork(TransactionStatus status, Session session,
            ServiceFactory sf) {

        // Ignore:
        // values.onlyTypes

        QueryBuilder qb = new QueryBuilder(128);
        qb.select("tag.textValue");
        qb.from("TagAnnotation", "tag");
        qb.join("tag.annotationLinks", "link", false, false);
        qb.join("link.child", "taggroup", false, false);
        qb.where();
        qb.and("taggroup.class = TagAnnotation");
        if (groupStr != null) {
            qb.and("taggroup.textValue = :groupStr");
            qb.param("groupStr", groupStr);
        }

        if (values.ownedBy != null) {
            Details d = values.ownedBy;
            if (/* ownable && */d.getOwner() != null) {
                Long id = d.getOwner().getId();
                if (id == null) {
                    throw new ApiUsageException("Id for owner cannot be null.");
                }
                qb.and("taggroup.details.owner.id = :id");
                qb.param("id", id);
            } else if (d.getGroup() != null) {
                Long id = d.getGroup().getId();
                if (id == null) {
                    throw new ApiUsageException("Id for group cannot be null.");
                }
                qb.and("taggroup.details.group.id = :id");
                qb.param("id", id);
            }
        }

        // criteria.createAlias("details.creationEvent", "create");
        // criteria.createAlias("details.updateEvent", "update");

        if (values.createdStart != null) {
            // criteria.add(Restrictions.gt("create.time",
            // values.createdStart));
        }

        if (values.createdStop != null) {
            // criteria.add(Restrictions.lt("create.time", values.createdStop));
        }

        if (values.modifiedStart != null) {
            // criteria.add(Restrictions.gt("update.time",
            // values.modifiedStart));
        }

        if (values.modifiedStop != null) {
            // criteria.add(Restrictions.lt("update.time",
            // values.modifiedStop));
        }

        Class cls = values.onlyTypes == null ? null : values.onlyTypes.get(0);
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
                    // result = null;
                    // return; // EARLY EXIT !
                } else {
                    for (Class annCls : values.onlyAnnotatedWith) {
                        // Criteria links = criteria
                        // .createCriteria("annotationLinks");
                        // Criteria child = links.createCriteria("child");

                        // SimpleExpression ofType = new TypeEqualityExpression(
                        // "class", annCls);
                        // child.add(ofType);
                    }
                }
            } else {
                // criteria.add(Restrictions.isEmpty("annotationLinks"));
            }
        }

        Query query = qb.query(session);

        result = new ArrayList<IObject>();
        List<String> tags = query.list();
        for (String tag : tags) {
            TagAnnotation ta = new TagAnnotation();
            ta.setTextValue(tag);
            result.add(ta);
        }
    }
}
