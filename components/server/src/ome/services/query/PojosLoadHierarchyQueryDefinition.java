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

import ome.conditions.ApiUsageException;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.parameters.Parameters;
import ome.util.builders.PojoOptions;

import org.hibernate.HibernateException;
import org.hibernate.Session;

public class PojosLoadHierarchyQueryDefinition extends Query {

    static Definitions defs = new Definitions(new CollectionQueryParameterDef(
            Parameters.IDS, true, Long.class), new OptionsQueryParameterDef(),
            new ClassQueryParameterDef());

    public PojosLoadHierarchyQueryDefinition(Parameters parameters) {
        super(defs, parameters);
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException,
            SQLException {

        PojoOptions po = new PojoOptions((Map) value(OPTIONS));
        Class klass = (Class) value(CLASS);

        StringBuilder sb = new StringBuilder();
        if (Project.class.isAssignableFrom(klass)) {
            sb.append("select this from Project this ");
            sb.append("left outer join fetch this.datasetLinks pdl ");
            sb.append("left outer join fetch pdl.child ds ");
            if (po.isLeaves()) {
                sb.append("left outer join fetch ds.imageLinks dil ");
                sb.append("left outer join fetch dil.child img ");
            }
            sb.append("left outer join fetch "
                    + "this.annotationLinksCountPerOwner this_a_c ");
        } else if (Dataset.class.isAssignableFrom(klass)) {
            sb.append("select this from Dataset this ");
            if (po.isLeaves()) {
                sb.append("left outer join fetch this.imageLinks dil ");
                sb.append("left outer join fetch dil.child img ");
            }
            sb.append("left outer join fetch "
                    + "this.annotationLinksCountPerOwner this_a_c ");
            sb.append("left outer join fetch "
                    + "this.imageLinksCountPerOwner this_i_c ");
        } else if (CategoryGroup.class.isAssignableFrom(klass)) {
            sb.append("select this from CategoryGroup this ");
            sb.append("left outer join fetch this.categoryLinks cgcl ");
            sb.append("left outer join fetch cgcl.child cat ");
            if (po.isLeaves()) {
                sb.append("left outer join fetch cat.imageLinks cil ");
                sb.append("left outer join fetch cil.child img ");
            }
        } else if (Category.class.isAssignableFrom(klass)) {
            sb.append("select this from Category this ");
            if (po.isLeaves()) {
                sb.append("left outer join fetch this.imageLinks cil ");
                sb.append("left outer join fetch cil.child img ");
            }
        } else {
            throw new ApiUsageException("Unknown container class: "
                    + klass.getName());
        }

        if (po.isLeaves()) {

            sb.append("left outer join fetch img.pixels as pix ");
            sb.append("left outer join fetch pix.pixelsType as pt ");
            sb.append("left outer join fetch pix.pixelsDimensions as pd ");
        }

        // optional ids
        Collection ids = (Collection) value(IDS);
        if (ids != null && ids.size() > 0) {
            sb.append("where this.id in (:ids)");
        }

        org.hibernate.Query q = session.createQuery(sb.toString());
        if (ids != null && ids.size() > 0) {
            q.setParameterList("ids", ids);
        }
        setQuery(q);
    }

    @Override
    protected void enableFilters(Session session) {
        ownerOrGroupFilters(session,
        // TODO this needs to be moved to Hierarchy.
                new String[] { CategoryGroup.OWNER_FILTER,
                        Category.OWNER_FILTER, Project.OWNER_FILTER,
                        Dataset.OWNER_FILTER }, new String[] {
                        CategoryGroup.GROUP_FILTER, Category.GROUP_FILTER,
                        Project.GROUP_FILTER, Dataset.GROUP_FILTER });
    }

}
