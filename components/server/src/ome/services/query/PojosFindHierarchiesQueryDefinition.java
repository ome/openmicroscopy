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

}
//select i from Image i
//#bottomUpHierarchy()
//    where 
//#imagelist()
//#filters()
//#typeExperimenter()
