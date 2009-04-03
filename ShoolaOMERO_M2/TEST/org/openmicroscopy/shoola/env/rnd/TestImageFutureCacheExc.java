/*
 * org.openmicroscopy.shoola.env.rnd.TestImageFutureCacheExc
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package org.openmicroscopy.shoola.env.rnd;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.data.DataSourceException;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.util.concur.tasks.MockFuture;
import org.openmicroscopy.shoola.util.tests.common.FakeBufferedImage;

/** 
 * Unit test for {@link ImageFutureCache}.
 * Verifies that constructor and public methods refuse bad arguments and
 * that legal state is maintained in the presence of exceptions. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TestImageFutureCacheExc
    extends TestCase
{

    private static final int    CACHE_SIZE = 1;  //Passed to target.
    private static final int    IMAGE_SIZE = 1;  //Passed to target.
    
    //private ImageFutureCache    target;  //Object under test.
    
    
    //Tests extract() when future throws exc.
    private void testExtract(Exception exc)
    {   
        /*
        //Create mock Future and set up expected calls.  Then add it to cache.
        MockFuture mf = new MockFuture();
        mf.getResult(null, exc);  //Throw exc when getResult() is invoked.
        mf.cancelExecution();
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        target.add(key, mf);  //Assume it works -- verified by other unit tests.
        
        //Transition mock to verification mode.
        mf.activate();
        
        //Test.
        try {
            target.extract(key);
        } catch (Exception e) {
            //Ok, expected.  Check state and verify exception.
            assertEquals("Should have removed the entry.", 
                    0, target.getCache().size());
            assertSame("Should have rethrown original exception.", 
                    exc, e);
        }
        
        //Make sure all expected calls were performed.
        mf.verify();
        */
    }
    
    public void setUp()
    {
        /*
        target = new ImageFutureCache(CACHE_SIZE, IMAGE_SIZE, 
                                        new MockNavigationHistory());
                                        */
    }
    
    public void testImageFutureCacheBadArgs()
    {
        /*
        try {
            new ImageFutureCache(0, 11, new MockNavigationHistory());
            fail("Should only accept positive cache size.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        try {
            new ImageFutureCache(-1, 1, new MockNavigationHistory());
            fail("Should only accept positive cache size.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        try {
            new ImageFutureCache(1, 0, new MockNavigationHistory());
            fail("Should only accept positive image size.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        try {
            new ImageFutureCache(1, -1, new MockNavigationHistory());
            fail("Should only accept positive image size.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        try {
            new ImageFutureCache(1, 1, null);
            fail("Shouldn't accept null NavigationHistory.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        */
    }
    
    public void testImageFutureCache()
    {
        /*
        assertNotNull("Didn't link navigation history.", 
                target.getNavigHistory());
        assertEquals("Shouldn't have changed the value passed to constructor.", 
                CACHE_SIZE, target.CACHE_SIZE);
        assertEquals("Shouldn't have changed the value passed to constructor.", 
                IMAGE_SIZE, target.IMAGE_SIZE);
        assertEquals("Cache should only allow one entry if IMAGE_SIZE is "+
                "equal to CACHE_SIZE.", 
                1, target.MAX_ENTRIES);
        
        target = new ImageFutureCache(1, 2, new MockNavigationHistory());
        assertEquals("Cache should allow no entry if IMAGE_SIZE is greater "+
                "than CACHE_SIZE.", 
                0, target.MAX_ENTRIES);
        target = new ImageFutureCache(7, 2, new MockNavigationHistory());
        assertEquals("Cache should allow as many entries as the greatest "+
                "integer N such that N*IMAGE_SIZE <= CACHE_SIZE.", 
                3, target.MAX_ENTRIES);
                */
    }
    /*
    public void testAddBufferedImage()
    {
        try {
            target.add(null, new FakeBufferedImage());
            fail("Shouldn't accept null plane def.");
        } catch (NullPointerException npe) {
            //Ok, expected.  Check state:
            assertEquals("Shouldn't have added an entry.", 
                    0, target.getCache().size());
        }
        try {
            target.add(new PlaneDef(PlaneDef.XZ, 0), new FakeBufferedImage());
            fail("Should only accept XY planes.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.  Check state:
            assertEquals("Shouldn't have added an entry.", 
                    0, target.getCache().size());
        }
        try {
            target.add(new PlaneDef(PlaneDef.ZY, 0), new FakeBufferedImage());
            fail("Should only accept XY planes.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.  Check state:
            assertEquals("Shouldn't have added an entry.", 
                    0, target.getCache().size());
        }
        try {
            target.add(new PlaneDef(PlaneDef.XY, 0), (FakeBufferedImage) null);
            fail("Shouldn't accept null image.");
        } catch (NullPointerException npe) {
            //Ok, expected.  Check state:
            assertEquals("Shouldn't have added an entry.", 
                    0, target.getCache().size());
        }
    }
    
    public void testAddFuture()
    {
        try {
            target.add(null, new MockFuture());
            fail("Shouldn't accept null plane def.");
        } catch (NullPointerException npe) {
            //Ok, expected.  Check state:
            assertEquals("Shouldn't have added an entry.", 
                    0, target.getCache().size());
        }
        try {
            target.add(new PlaneDef(PlaneDef.XZ, 0), new MockFuture());
            fail("Should only accept XY planes.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.  Check state:
            assertEquals("Shouldn't have added an entry.", 
                    0, target.getCache().size());
        }
        try {
            target.add(new PlaneDef(PlaneDef.ZY, 0), new MockFuture());
            fail("Should only accept XY planes.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.  Check state:
            assertEquals("Shouldn't have added an entry.", 
                    0, target.getCache().size());
        }
        try {
            target.add(new PlaneDef(PlaneDef.XY, 0), (MockFuture) null);
            fail("Shouldn't accept null image.");
        } catch (NullPointerException npe) {
            //Ok, expected.  Check state:
            assertEquals("Shouldn't have added an entry.", 
                    0, target.getCache().size());
        }
    }
    
    public void testExtractNullPlaneDef()
        throws Exception
    {
        try {
            target.extract(null);
            fail("Shouldn't accept null plane def.");
        } catch (NullPointerException npe) {
            //Ok, expected.  Check state:
            assertEquals("Shouldn't have added an entry.", 
                    0, target.getCache().size());
        }
    }
    
    public void testExtractDataSourceException()
        throws Exception
    {   
        testExtract(new DataSourceException());
    }
    
    public void testExtractQuantizationException()
        throws Exception
    {   
        testExtract(new QuantizationException());
    }
    
    public void testExtractRuntimeException()
        throws Exception
    {   
        testExtract(new RuntimeException());
    }
    */
}
