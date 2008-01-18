/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.query;

import static ome.parameters.Parameters.CLASS;
import static ome.parameters.Parameters.IDS;
import static ome.parameters.Parameters.OPTIONS;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import ome.parameters.Parameters;
import ome.util.builders.PojoOptions;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class PojosFindAnnotationsQueryDefinition extends Query {

    static Definitions defs = new Definitions(new IdsQueryParameterDef(),
            new OptionsQueryParameterDef(), new ClassQueryParameterDef(),
            new QueryParameterDef("annotatorIds", Collection.class, true));

    public PojosFindAnnotationsQueryDefinition(Parameters parameters) {
        super(defs, parameters);
        // TODO set local fields here.
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException,
            SQLException {
        PojoOptions po = new PojoOptions((Map) value(OPTIONS));

        Class k = (Class) value(CLASS);

        Criteria obj = session.createCriteria(k);
        // TODO refactor into CriteriaUtils
        // Not currently loading, since IPojos.findAnnotations, rearranges
        // them to return the annotations, which have no links to their
        // owning objects
        // obj.setFetchMode("details.owner", FetchMode.JOIN);
        // obj.setFetchMode("details.group", FetchMode.JOIN);
        // obj.setFetchMode("details.creationEvent", FetchMode.JOIN);
        // obj.setFetchMode("details.updateEvent", FetchMode.JOIN);
        obj.add(Restrictions.in("id", (Collection) value(IDS)));

        // Here we do want the left join, so the consumer will see
        // that there's an empty set
        Criteria links = obj.createCriteria("annotationLinks", "links",
                LEFT_JOIN);
        Criteria ann = links.createCriteria("child", LEFT_JOIN);
        Criteria annotator = ann.createAlias("details.owner", "ann_owner");

        ann.createAlias("details.creationEvent", "ann_create");
        ann.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        if (check("annotatorIds")) {
            Collection annotatorIds = (Collection) value("annotatorIds");
            if (annotatorIds != null && annotatorIds.size() > 0) {
                annotator.add(Restrictions.in("id", annotatorIds));
            }
        }
        setCriteria(obj);
    }
}