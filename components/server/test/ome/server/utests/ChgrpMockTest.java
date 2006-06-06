/*
 * ome.server.utests.ChgrpMockTest
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
import org.testng.annotations.*;

// Application-internal dependencies
import ome.conditions.SecurityViolation;
import ome.model.core.Image;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since Omero 2.0
 */
public class ChgrpMockTest extends AbstractChangeDetailsMockTest
{

    // Factors: 
    //  1. new or managed
    //  2. root or user
    //  3. change to user or root
    //  5. TODO even laterer: allowing changes based on group privileges. 
    
    // ~ Nonroot / New Image
    // =========================================================================
    
    @Test
    @ExpectedExceptions( SecurityViolation.class )
    public void test_non_root_new_image_chgrp_to_other_group() throws Exception
    {
        userImageChgrp( _USER, _NEW, 2L);
        filter.filter( null, i );
        super.verify();
    }

    @Test
    @ExpectedExceptions( SecurityViolation.class )
    public void test_non_root_new_image_chgrp_to_system() throws Exception
    {
        userImageChgrp( _USER, _NEW, SYS_GROUP_ID );
        filter.filter( null, i );
        super.verify();
    }

    // ~ Nonroot / Managed image
    // =========================================================================

    @Test
    @ExpectedExceptions( SecurityViolation.class )
    public void test_managed_image_non_root_chgrp_to_other_group() throws Exception
    {
        userImageChgrp( _USER, _MANAGED, 2L);
        willLoadImage( managedImage() );
        filter.filter( null, i );
        super.verify();
    }

    @Test
    @ExpectedExceptions( SecurityViolation.class )
    public void test_managed_image_non_root_chgrp_to_system() throws Exception
    {
        userImageChgrp( _USER, _MANAGED, SYS_GROUP_ID);
        willLoadImage( managedImage() );
        filter.filter( null, i );
        super.verify();
    }

    // ~ Root / new image
    // =========================================================================
    @Test
    public void test_root_new_image_chgrp_to_other_group() throws Exception
    {
        userImageChgrp( _ROOT, _NEW, 2L);
        willLoadUser( 0L );
        willLoadGroup( 2L );
        willLoadEvent( 0L );
        filter.filter( null, i );
        super.verify();
    }

    // ~ Root / managed image
    // =========================================================================
    @Test
    public void test_root_managed_image_chgrp_to_other_group() throws Exception
    {
        userImageChgrp( _ROOT, _MANAGED, 2L);
        willLoadImage( managedImage() );
        willLoadUser( 0L );
        willLoadGroup( 2L );
        willLoadEvent( 0L );
        filter.filter( null, i );
        super.verify();
    }

}
