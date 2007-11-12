/*
 *   $Id: PojosGetImagesQueryDefinition.java 1931 2007-11-12 10:39:50Z ola $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.query;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.util.builders.PojoOptions;
import static ome.parameters.Parameters.*;

public class PojosGetImagesByOptionsQueryDefinition extends Query {

    static Definitions defs = new Definitions(new OptionsQueryParameterDef());

    public PojosGetImagesByOptionsQueryDefinition(Parameters parameters) {
        super(defs, parameters);
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException,
            SQLException {
        Criteria c = session.createCriteria(Image.class);
        c.createAlias("details.creationEvent", "create");
        c.createAlias("details.updateEvent", "update");
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        Criteria pix = c.createCriteria("defaultPixels", LEFT_JOIN);
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
