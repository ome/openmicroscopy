/*
 * ome.services.query.PojosFindHierarchiesQueryDefinition
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.services.query;

// Java imports
import java.sql.SQLException;
import java.util.Collection;

// Third-party libraries
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

// Application-internal dependencies
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
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
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 * @see IPojos#findContainerHierarchies(java.lang.Class, java.util.Set, java.util.Map)
 */

public class PojosFindHierarchiesQueryDefinition 
    extends AbstractClassIdsOptionsQuery 
{
    
    public PojosFindHierarchiesQueryDefinition( Parameters p )
    {
        super(p);
    }

    @Override
    protected void buildQuery(Session session) 
    throws HibernateException, SQLException
    {
        Criteria c = session.createCriteria(Image.class);
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        c.add(Restrictions.in("id",(Collection) value(IDS)));
        Hierarchy.fetchParents(c,(Class) value(CLASS),Integer.MAX_VALUE);
        setCriteria( c );
    }

    @Override
    protected void enableFilters(Session session)
    {
        ownerOrGroupFilters(session, 
//              TODO this needs to be moved to Hierarchy.
                new String[]{
        			CategoryGroup.OWNER_FILTER,
        			CategoryGroup.OWNER_FILTER_CATEGORYLINKS,
        			Category.OWNER_FILTER,
        			Category.OWNER_FILTER_CATEGORYGROUPLINKS,
        			Category.OWNER_FILTER_IMAGELINKS,
        			Project.OWNER_FILTER,
        			Project.OWNER_FILTER_DATASETLINKS,
        			Dataset.OWNER_FILTER,
        			Dataset.OWNER_FILTER_IMAGELINKS,
        			Dataset.OWNER_FILTER_PROJECTLINKS },
        		new String[]{
    				CategoryGroup.GROUP_FILTER,
    				CategoryGroup.GROUP_FILTER_CATEGORYLINKS,
    				Category.GROUP_FILTER,
    				Category.GROUP_FILTER_CATEGORYGROUPLINKS,
    				Category.GROUP_FILTER_IMAGELINKS,
    				Project.GROUP_FILTER,
    				Project.GROUP_FILTER_DATASETLINKS,
    				Dataset.GROUP_FILTER,
    				Dataset.GROUP_FILTER_IMAGELINKS,
    				Dataset.GROUP_FILTER_PROJECTLINKS }
                );
    }
    
}
//select i from Image i
//#bottomUpHierarchy()
//    where 
//#imagelist()
//#filters()
//#typeExperimenter()
