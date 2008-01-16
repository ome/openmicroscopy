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

        // TODO refactor into CriteriaUtils
        Criteria obj = session.createCriteria(k);
        obj.createAlias("details.owner", "obj_owner");
        obj.createAlias("details.creationEvent", "obj_create");
        obj.createAlias("details.updateEvent", "obj_update");
        obj.add(Restrictions.in("id", (Collection) value(IDS)));

        Criteria links = obj.createCriteria("annotationLinks", "links",
                LEFT_JOIN);

        Criteria ann = links.createCriteria("child", LEFT_JOIN);
        Criteria annotator = ann.createAlias("details.owner", "ann_owner");
        ann.createAlias("details.creationEvent", "ann_create");
        // ann.createAlias("details.updateEvent", "ann_update");
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