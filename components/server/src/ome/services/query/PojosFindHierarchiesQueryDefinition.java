/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.services.query;

import java.sql.SQLException;
import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.parameters.Parameters;
import static ome.parameters.Parameters.*;

/**
 * walks up the hierarchy tree starting at {@link ome.model.core.Image} nodes
 * while fetching various information.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since OMERO 3.0
 * @see IContainer#findContainerHierarchies(java.lang.Class, java.util.Set,
 *      java.util.Map)
 */

public class PojosFindHierarchiesQueryDefinition extends
        AbstractClassIdsOptionsQuery {

    public PojosFindHierarchiesQueryDefinition(Parameters p) {
        super(p);
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException,
            SQLException {
        Criteria c = session.createCriteria(Image.class);
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        c.add(Restrictions.in("id", (Collection) value(IDS)));
        Hierarchy.fetchParents(c, (Class) value(CLASS), Integer.MAX_VALUE);
        setCriteria(c);
    }

    @Override
    protected void enableFilters(Session session) {
        ownerOrGroupFilters(session,
        // ticket:318
                // TODO this needs to be moved to Hierarchy.
                // TODO these are also not all needed. Need to simplify.
                new String[] { Project.OWNER_FILTER,
                        Project.OWNER_FILTER_DATASETLINKS,
                        Dataset.OWNER_FILTER, Dataset.OWNER_FILTER_IMAGELINKS,
                        Dataset.OWNER_FILTER_PROJECTLINKS,
                        Image.OWNER_FILTER_DATASETLINKS,
                        }, new String[] {
                        Project.GROUP_FILTER,
                        Project.GROUP_FILTER_DATASETLINKS,
                        Dataset.GROUP_FILTER, Dataset.GROUP_FILTER_IMAGELINKS,
                        Dataset.GROUP_FILTER_PROJECTLINKS,
                        Image.GROUP_FILTER_DATASETLINKS
                        });
    }

}
// select i from Image i
// #bottomUpHierarchy()
// where
// #imagelist()
// #filters()
// #typeExperimenter()
