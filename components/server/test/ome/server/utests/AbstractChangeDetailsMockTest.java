/*
 * ome.server.utests.AbstractChangeDetailsMockTest
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
package ome.server.utests;

// Java imports

// Third-party libraries
import org.springframework.orm.hibernate3.HibernateOperations;
import org.testng.annotations.*;

// Application-internal dependencies
import ome.model.core.Image;
import ome.model.meta.Event;
import ome.tools.hibernate.UpdateFilter;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since Omero 2.0
 */
@Test( groups = {"ticket:52","security", "broken"} )
public class AbstractChangeDetailsMockTest extends AbstractLoginMockTest
{

    protected Image i;
    
    // Factors: 
    //  1. new or managed
    //  2. root or user
    //  3. change to user or root
    //  5. TODO even laterer: allowing changes based on group privileges. 
    
    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception
    {
        super.tearDown();
        i = null;
    }
    
    boolean _MANAGED = false;
    boolean _NEW = true;
    boolean _ROOT = true;
    boolean _USER = false;
    
    /** this method should be called first to properly setup CurrentDetails */
    protected void userImageChmod( boolean root_p, boolean new_p, Long targetId )
    {
        userImage( root_p, new_p );
        chown( i, targetId );

    }
    
    /** this method should be called first to properly setup CurrentDetails */
    protected void userImageChgrp( boolean root_p, boolean new_p, Long targetId )
    {
        userImage( root_p, new_p );
        chgrp( i, targetId );

    }

    private void userImage( boolean root_p, boolean new_p)
    {
        if (root_p) {
            rootLogin();
        } else {
            userLogin( );
        }
        
        if (new_p) {
            i = new Image();
        } else {
            i = managedImage();
        }        
    }
}
