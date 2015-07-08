/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.query;

import static ome.parameters.Parameters.CLASS;
import static ome.parameters.Parameters.IDS;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import ome.conditions.ApiUsageException;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.tools.hibernate.QueryBuilder;

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

        QueryBuilder qb = new QueryBuilder();
        qb.select("img");
        qb.from("Image", "img");
        qb.join("img.details.creationEvent", "ce", true, true);
        qb.join("img.details.updateEvent", "ue", true, true);
        qb.join("img.pixels", "pix", true, true);
        qb.join("pix.timeIncrement", "increment", true, true);
        qb.join("pix.pixelsType", "pt", true, true);
        qb.join("img.format", "format", true, true);
        qb.join("img.annotationLinksCountPerOwner", "i_c_ann", true, true);
        qb.join("img.datasetLinksCountPerOwner", "i_c_ds", true, true);

        if (params.isAcquisitionData()) {
	        qb.join("img.stageLabel", "position", true, true);
	        qb.join("img.imagingEnvironment", "condition", true, true);
	        qb.join("img.objectiveSettings", "os", true, true);
	        qb.join("os.medium", "me", true, true);
	        qb.join("os.objective", "objective", true, true);
	        qb.join("objective.immersion", "im", true, true);
	        qb.join("objective.correction", "co", true, true);
        }
        
        // see http://trac.openmicroscopy.org.uk/ome/ticket/296
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
        } else {
            throw new ApiUsageException("Query not implemented for " + klass);
        }

        // if PojoOptions sets START_TIME and/or END_TIME
        if (params.getStartTime() != null) {
            qb.and("img.details.creationEvent.time > :starttime");
            qb.param("starttime", params.getStartTime());
        }
        if (params.getEndTime() != null) {
            qb.and("img.details.creationEvent.time < :endtime");
            qb.param("endtime", params.getEndTime());
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
