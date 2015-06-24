/*
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.query;

import static ome.parameters.Parameters.CLASS;
import static ome.parameters.Parameters.IDS;

import java.sql.SQLException;
import java.util.Collection;

import ome.conditions.ApiUsageException;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.screen.Plate;
import ome.model.screen.Screen;
import ome.parameters.Parameters;

import org.hibernate.HibernateException;
import org.hibernate.Session;

public class PojosLoadHierarchyQueryDefinition extends Query {

    static Definitions defs = new Definitions(new CollectionQueryParameterDef(
            Parameters.IDS, true, Long.class),
            new ClassQueryParameterDef());

    public PojosLoadHierarchyQueryDefinition(Parameters parameters) {
        super(defs, parameters);
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException,
            SQLException {

        Class klass = (Class) value(CLASS);

        StringBuilder sb = new StringBuilder();
        if (Project.class.isAssignableFrom(klass)) {
            sb.append("select this from Project this ");
            sb.append("left outer join fetch this.details.creationEvent ");
            sb.append("left outer join fetch this.datasetLinks pdl ");
            sb.append("left outer join fetch pdl.child ds ");
            if (params.isLeaves()) {
                sb.append("left outer join fetch ds.imageLinks dil ");
                sb.append("left outer join fetch dil.child img ");
            }
            sb.append("left outer join fetch "
                    + "this.annotationLinksCountPerOwner this_a_c ");
        } else if (Dataset.class.isAssignableFrom(klass)) {
            sb.append("select this from Dataset this ");
            sb.append("left outer join fetch this.details.creationEvent ");
            if (params.isLeaves()) {
                sb.append("left outer join fetch this.imageLinks dil ");
                sb.append("left outer join fetch dil.child img ");
            }
            sb.append("left outer join fetch "
                    + "this.annotationLinksCountPerOwner this_a_c ");
            sb.append("left outer join fetch "
                    + "this.imageLinksCountPerOwner this_i_c ");
        } else if (Screen.class.isAssignableFrom(klass)) {
        	sb.append("select this from Screen this ");
        	sb.append("left outer join fetch this.details.creationEvent ");
            sb.append("left outer join fetch this.plateLinks pdl ");
            sb.append("left outer join fetch pdl.child ds ");
            sb.append("left outer join fetch ds.details.creationEvent ");
            sb.append("left outer join fetch ds.details.updateEvent ");
            sb.append("left outer join fetch ds.plateAcquisitions sa ");
            sb.append("left outer join fetch "
                    + "this.annotationLinksCountPerOwner this_a_c ");
            sb.append("left outer join fetch sa.annotationLinksCountPerOwner sa_a_c ");
        } else if (Plate.class.isAssignableFrom(klass)) {
        	sb.append("select this from Plate this ");
        	sb.append("left outer join fetch this.details.creationEvent ");
        	sb.append("left outer join fetch this.plateAcquisitions sa ");
            sb.append("left outer join fetch "
                    + "this.annotationLinksCountPerOwner this_a_c ");
            sb.append("left outer join fetch sa.annotationLinksCountPerOwner sa_a_c ");
        } else {
            throw new ApiUsageException("Unknown container class: "
                    + klass.getName());
        }

        if (params.isLeaves()) {
            if (Screen.class.isAssignableFrom(klass) || Plate.class.isAssignableFrom(klass)) {
                sb.append("left outer join fetch sa.wellSample ws ");
                sb.append("left outer join fetch ws.image img ");
            }
            sb.append("left outer join fetch img.details.creationEvent as cre ");
            sb.append("left outer join fetch img.details.updateEvent as evt ");
            sb.append("left outer join fetch img.pixels as pix ");
            sb.append("left outer join fetch img.format as format ");
            sb.append("left outer join fetch pix.pixelsType as pt ");
            if (params.isAcquisitionData()) {
	            sb.append("left outer join fetch img.stageLabel as position ");
	            sb.append("left outer join fetch img.imagingEnvironment" +
	            		" as condition ");
	            sb.append("left outer join fetch img.objectiveSettings as os ");
	            sb.append("left outer join fetch os.medium as me ");
	            sb.append("left outer join fetch os.objective as objective ");
	            sb.append("left outer join fetch objective.immersion as im ");
	            sb.append("left outer join fetch objective.correction as co ");
            }
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
                new String[] { 
        				Project.OWNER_FILTER, Dataset.OWNER_FILTER, 
        				Screen.OWNER_FILTER, Plate.OWNER_FILTER }, 
                new String[] { 
        				Project.GROUP_FILTER, Dataset.GROUP_FILTER, 
        		        Screen.GROUP_FILTER, Plate.GROUP_FILTER });
    }

}
