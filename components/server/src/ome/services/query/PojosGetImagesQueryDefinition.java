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
import java.util.HashMap;
import java.util.Map;

import ome.conditions.ApiUsageException;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.util.builders.PojoOptions;

import org.hibernate.HibernateException;
import org.hibernate.Session;

public class PojosGetImagesQueryDefinition extends AbstractClassIdsOptionsQuery {

    public PojosGetImagesQueryDefinition(Parameters parameters) {
        super(parameters);
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException,
            SQLException {

        Map<String, Object> params = new HashMap<String, Object>();
        Class klass = (Class) value(CLASS);
        Collection ids = (Collection) value(IDS);
        PojoOptions po = new PojoOptions((Map) value(OPTIONS));

        StringBuilder sb = new StringBuilder();
        sb.append("select img from Image img ");
        sb.append("left outer join fetch img.details.creationEvent ");
        sb.append("left outer join fetch img.details.updateEvent ");
        sb.append("left outer join fetch img.pixels as pix ");
        sb.append("left outer join fetch pix.pixelsType as pt ");
        sb.append("left outer join fetch pix.pixelsDimensions as pd ");
        sb.append("left outer join fetch "
                + "img.annotationLinksCountPerOwner as i_c_ann ");
        sb.append("left outer join fetch "
                + "img.datasetLinksCountPerOwner as i_c_ds ");

        if (Dataset.class.isAssignableFrom(klass)
                || Project.class.isAssignableFrom(klass)) {
            sb.append("join img.datasetLinks dil ");
            sb.append("join dil.parent ds ");
            // sb.append("left outer join fetch "
            // + "ds.annotationLinksCountPerOwner as ds_c ");
        }

        if (Project.class.isAssignableFrom(klass)) {
            sb.append("join ds.projectLinks pdl ");
            sb.append("join pdl.parent prj ");
            // sb.append("left outer join fetch "
            // + "prj.annotationLinksCountPerOwner as prj_c ");
        }

        if (Category.class.isAssignableFrom(klass)
                || CategoryGroup.class.isAssignableFrom(klass)) {
            sb.append("join img.categoryLinks cil ");
            sb.append("join cil.parent cat ");
            // sb.append("left outer join fetch "
            // + "cat.annotationLinksCountPerOwner as cat_c ");
        }

        if (CategoryGroup.class.isAssignableFrom(klass)) {
            sb.append("join cat.categoryGroupLinks cgcl ");
            sb.append("join cgcl.parent cg ");
            // sb.append("left outer join fetch "
            // + "cgcl.annotationLinksCountPerOwner as cgcl_c ");
        }

        sb.append("where ");

        // if PojoOptions sets START_TIME and/or END_TIME
        if (po.getStartTime() != null) {
            sb.append("img.details.creationEvent.time > :starttime and ");
            params.put("starttime", po.getStartTime());
        }
        if (po.getEndTime() != null) {
            sb.append("img.details.creationEvent.time < :endtime and ");
            params.put("endtime", po.getEndTime());
        }

        // see https://trac.openmicroscopy.org.uk/omero/ticket/296
        if (Image.class.isAssignableFrom(klass)) {
            sb.append("img.id in (:ids) ");
        } else if (Dataset.class.isAssignableFrom(klass)) {
            sb.append("ds.id in (:ids)");
        } else if (Project.class.isAssignableFrom(klass)) {
            sb.append("prj.id in (:ids)");
        } else if (Category.class.isAssignableFrom(klass)) {
            sb.append("cat.id in (:ids)");
        } else if (CategoryGroup.class.isAssignableFrom(klass)) {
            sb.append("cg.id in (:ids)");
        } else {
            throw new ApiUsageException("Query not implemented for " + klass);
        }

        org.hibernate.Query q = session.createQuery(sb.toString());
        for (String param : params.keySet()) {
            q.setParameter(param, params.get(param));
        }

        q.setParameterList("ids", ids);
        setQuery(q);
    }

    @Override
    protected void enableFilters(Session session) {
        ownerOrGroupFilters(session, new String[] { Image.OWNER_FILTER },
                new String[] { Image.GROUP_FILTER });
    }

}
// select i from Image i
// #bottomUpHierarchy()
// where
// #imagelist()
// #filters()
// #typeExperimenter()
