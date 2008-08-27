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
import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.tools.hibernate.QueryBuilder;
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

        Class klass = (Class) value(CLASS);
        Collection ids = (Collection) value(IDS);
        PojoOptions po = new PojoOptions((Map) value(OPTIONS));

        QueryBuilder qb = new QueryBuilder(256);
        qb.select("img");
        qb.from("Image", "img");
        qb.join("img.details.creationEvent", "ce", true, true);
        qb.join("img.details.updateEvent", "ue", true, true);
        qb.join("img.pixels", "pix", true, true);
        qb.join("pix.pixelsType", "pt", true, true);
        qb.join("pix.pixelsDimensions", "pd", true, true);
        qb.join("img.annotationLinksCountPerOwner", "i_c_ann", true, true);
        qb.join("img.datasetLinksCountPerOwner", "i_c_ds", true, true);

        // see https://trac.openmicroscopy.org.uk/omero/ticket/296
        if (Image.class.isAssignableFrom(klass)) {
            qb.where();
            qb.and("img.id in (:ids)");
        } else if (Dataset.class.isAssignableFrom(klass)) {
            qb.join("img.datasetLinks", "dil", false, false);
            qb.join("dil.parent", "ds", false, false);
            // ds.annotationLinksCountPerOwner, ds_c
            qb.where();
            qb.and("ds.id in (:ids)");
        } else if (Project.class.isAssignableFrom(klass)) {
            qb.join("img.datasetLinks", "dil", false, false);
            qb.join("dil.parent", "ds", false, false);
            qb.join("ds.projectLinks", "pdl", false, false);
            qb.join("pdl.parent", "prj", false, false);
            // "prj.annotationLinksCountPerOwner as prj_c "
            qb.where();
            qb.and("prj.id in (:ids)");
        } else if (Category.class.isAssignableFrom(klass)) {
            qb.join("img.categoryLinks", "cil", false, false);
            qb.join("cil.parent", "cat", false, false);
            // + "cat.annotationLinksCountPerOwner as cat_c ");
            qb.where();
            qb.and("cat.id in (:ids)");
        } else if (CategoryGroup.class.isAssignableFrom(klass)) {
            qb.join("img.categoryLinks", "cil", false, false);
            qb.join("cil.parent", "cat", false, false);
            qb.join("cat.categoryGroupLinks", "cgcl", false, false);
            qb.join("cgcl.parent", "cg", false, false);
            // + "cgcl.annotationLinksCountPerOwner as cgcl_c ");
            qb.where();
            qb.and("cg.id in (:ids)");
        } else {
            throw new ApiUsageException("Query not implemented for " + klass);
        }

        // if PojoOptions sets START_TIME and/or END_TIME
        if (po.getStartTime() != null) {
            qb.and("img.details.creationEvent.time > :starttime");
            qb.param("starttime", po.getStartTime());
        }
        if (po.getEndTime() != null) {
            qb.and("img.details.creationEvent.time < :endtime");
            qb.param("endtime", po.getEndTime());
        }

        qb.paramList("ids", ids);
        org.hibernate.Query q = qb.query(session);
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
