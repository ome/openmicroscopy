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
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

import ome.model.core.Image;
import ome.parameters.Parameters;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.util.builders.PojoOptions;
import static ome.parameters.Parameters.*;

public class PojosGetImagesQueryDefinition extends AbstractClassIdsOptionsQuery {

    public PojosGetImagesQueryDefinition(Parameters parameters) {
        super(parameters);
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException,
            SQLException {
        Criteria c = session.createCriteria(Image.class);
        c.createAlias("details.creationEvent", "create");
        c.createAlias("details.updateEvent", "update");
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        Criteria pix = c.createCriteria("pixels", LEFT_JOIN);
        pix.createCriteria("pixelsType", LEFT_JOIN);
        pix.createCriteria("pixelsDimensions", LEFT_JOIN);

        // if PojoOptions sets START_TIME and/or END_TIME
        if (check(OPTIONS)) {
        	PojoOptions po = new PojoOptions((Map) value(OPTIONS));
        	
			if (po.getStartTime() != null) {
				c.add(Restrictions.gt("create.time", (Timestamp) po.getStartTime()));
			}
			if (po.getEndTime() != null)
				c.add(Restrictions.lt("create.time", (Timestamp) po.getEndTime()));
        }
        
        Class klass = (Class) value(CLASS);
        Collection ids = (Collection) value(IDS);

        // see https://trac.openmicroscopy.org.uk/omero/ticket/296
        if (Image.class.isAssignableFrom(klass)) {
            c.add(Restrictions.in("id", ids));
        } else {
            // Add restrictions to the most distant criteria
            Criteria[] hy = Hierarchy.fetchParents(c, klass, Integer.MAX_VALUE);
            hy[hy.length - 1].add(Restrictions.in("id", ids));
        }

        setCriteria(c);
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
