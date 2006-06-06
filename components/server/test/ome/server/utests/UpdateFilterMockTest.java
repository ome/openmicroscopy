/*
 * ome.server.utests.UpdateFilterMockTest
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
import java.util.Collection;
import java.util.Map;

// Third-party libraries
import org.testng.annotations.*;

// Application-internal dependencies
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.internal.Details;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since Omero 2.0
 */
public class UpdateFilterMockTest extends AbstractLoginMockTest
{


    // ~ NON GRAPHS (single elements)
    // =========================================================================
    
    @Test
    public void test_filter_null() throws Exception
    {
        filter.filter( null, (Object) null );
        filter.filter( null, (IObject) null );
        filter.filter( null, (Details) null );
        filter.filter( null, (Map) null );
    }
    
    @Test( groups = "todo" )
    @ExpectedExceptions( RuntimeException.class )
    public void test_filter_null_collection() throws Exception
    {
        filter.filter( null, (Collection) null );
    }
    
    @Test
    public void test_transient() throws Exception
    {
        willCheckRootDetails();
        
        Image i = new Image();
        Image withNewDetails = (Image) filter.filter( null, i );
        assertTrue( i == withNewDetails );
        assertDetails( withNewDetails  );
    }
    
    @Test
    @ExpectedExceptions(IllegalStateException.class)
    public void test_unloaded_without_id() throws Exception
    {
        Image i = new Image();
        i.unload();
        filter.filter( null, i );
        
        // this image has no id and is seen as transient.
    }
    
    @Test
    public void test_unloaded() throws Exception
    {
        willLoadImage( managedImage() );
        Image i = new Image( 0L );
        i.unload();
        Image reloaded = (Image) filter.filter( null, i );
        assertFalse( i == reloaded );
        
        // this image is unloaded with id 0, and the filter tries to load
        // it from the ops. 
        
    }

    @Test
    public void test_managed_with_replacement() throws Exception
    {
        Image i = managedImage();
        i.getGraphHolder().setReplacement( new Image( 0L ));
        filter.filter( null, i );
        filter.unloadReplacedObjects();
        
        assertFalse( "Image should be unloaded", i.isLoaded() );

        // if the image has a replacement, then it will come out of the 
        // filter.unloadedReplacedObjects() method unloaded.
        
    }
    
    @Test
    public void test_managed() throws Exception
    {
        Image i = managedImage();
        willLoadImage( managedImage() );
        willCheckRootDetails();
        
        filter.filter( null, i );
        filter.unloadReplacedObjects();
        
        assertTrue( "Image should be loaded", i.isLoaded() );

        // if the image doesn't have a replacement, then it should not 
        // be unloaded after filter.unloadedReplacedObjects() method.
        
    }

    // ~ GRAPHS (multiple levels)
    // =========================================================================

   
}
