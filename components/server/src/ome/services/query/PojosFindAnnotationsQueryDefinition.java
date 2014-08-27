/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.query;

import static ome.parameters.Parameters.CLASS;
import static ome.parameters.Parameters.IDS;

import java.util.Collection;

import ome.parameters.Parameters;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class PojosFindAnnotationsQueryDefinition extends Query {

    static Definitions defs = new Definitions(new IdsQueryParameterDef(),
            new ClassQueryParameterDef(),
            new QueryParameterDef("annotatorIds", Collection.class, true));

    public PojosFindAnnotationsQueryDefinition(Parameters parameters) {
        super(defs, parameters);
        // TODO set local fields here.
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException {

        Class k = (Class) value(CLASS);

        Criteria obj = session.createCriteria(k);
        // TODO refactor into CriteriaUtils
        // Not currently loading, since IContainer.findAnnotations, rearranges
        // them to return the annotations, which have no links to their
        // owning objects
        // obj.setFetchMode("details.owner", FetchMode.JOIN);
        // obj.setFetchMode("details.group", FetchMode.JOIN);
        // obj.setFetchMode("details.creationEvent", FetchMode.JOIN);
        // obj.setFetchMode("details.updateEvent", FetchMode.JOIN);
        Collection ids = (Collection) value(IDS);
        if (ids != null && ids.size() > 0)
        	obj.add(Restrictions.in("id", ids));

        // Here we do want the left join, so the consumer will see
        // that there's an empty set
        Criteria links = obj.createCriteria("annotationLinks", "links",
                LEFT_JOIN);
        Criteria ann = links.createCriteria("child", LEFT_JOIN);
        Criteria ann_owner = ann.createAlias("details.owner", "ann_owner",
                LEFT_JOIN);
        Criteria ann_create = ann.createAlias("details.creationEvent",
                "ann_create", LEFT_JOIN);
        Criteria ann_file = ann.createAlias("file",
                "ann_file", LEFT_JOIN);

        obj.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        if (check("annotatorIds")) {
            Collection annotatorIds = (Collection) value("annotatorIds");
            if (annotatorIds != null && annotatorIds.size() > 0) {
                ann.add(Restrictions.in("details.owner.id", annotatorIds));
            }
        }
        setCriteria(obj);
    }
}
