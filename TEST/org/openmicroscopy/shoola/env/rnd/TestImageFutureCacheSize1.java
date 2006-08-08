/*
 * org.openmicroscopy.shoola.env.rnd.TestImageFutureCacheSize1
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
import java.awt.image.BufferedImage;

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.util.concur.tasks.Future;
import org.openmicroscopy.shoola.util.concur.tasks.MockFuture;
import org.openmicroscopy.shoola.util.math.geom2D.Line;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint;
import org.openmicroscopy.shoola.util.tests.common.FakeBufferedImage;

/** 
 * Unit test for {@link ImageFutureCache}.
 * Verifies operation of the cache when its size is <code>1</code>.
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
public class TestImageFutureCacheSize1
    extends TestCase
{


    private static final int    CACHE_SIZE = 1;  //Passed to target.
    private static final int    IMAGE_SIZE = 1;  //Passed to target.
    
    private MockNavigationHistory navigHistory;  //Mock linked to target.
    //private ImageFutureCache    target;  //Object under test.
    
    
    public void setUp()
    {
        navigHistory = new MockNavigationHistory();
        //target = new ImageFutureCache(CACHE_SIZE, IMAGE_SIZE, navigHistory);
    }
    
    public void testAdd()
    {
        /*
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        BufferedImage img = new FakeBufferedImage();
        target.add(key, img);
        assertTrue("Should have cached the image.", 
                target.getCache().containsValue(img));
                */
    }
    
    /*
    public void testAddFuture()
    {
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        Future f = new MockFuture();
        target.add(key, f);
        assertTrue("Should have cached the image future.", 
                target.getCache().containsValue(f));
    }
    
    public void testAddWhenMaxEntries()
    {
        //Create mock Future and set up expected calls.
        MockFuture mf = new MockFuture(); 
        navigHistory.currentDirection(
                new Line(new PlanePoint(0, 0), new PlanePoint(1, 0)));
        
        //Fill cache up.
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        target.add(key, new FakeBufferedImage());  //Now cache is full.
        
        //Transition mock to verification mode.
        mf.activate();
        navigHistory.activate();
        
        //Test.
        target.add(key, mf);
        assertEquals("Should never cache more than MAX_ENTRIES.", 
                1, target.getCache().size());
        assertTrue("Should have cached the image future.", 
                target.getCache().containsValue(mf));
        
        //Make sure all expected calls were performed.
        mf.verify();
        navigHistory.verify();
    }
    
    public void testAddWhenMaxEntriesAndNoDirection()
    {
        //Create mock Future and set up expected calls.  Then add it to cache.
        MockFuture mf = new MockFuture();
        mf.cancelExecution();  //Removal should cancel any future in cache.
        navigHistory.currentDirection(null);  //No current direction.
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        target.add(key, mf);  //Now cache is full.
        
        //Transition mocks to verification mode.
        mf.activate();
        navigHistory.activate();
        
        //Test.
        BufferedImage img = new FakeBufferedImage();
        target.add(key, img);
        assertEquals("Should never cache more than MAX_ENTRIES.", 
                1, target.getCache().size());
        assertTrue("Should have cached the image.", 
                target.getCache().containsValue(img));
        
        //Make sure all expected calls were performed.
        mf.verify();
        navigHistory.verify();
    }
    
    public void testContains()
    {
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        target.add(key, new FakeBufferedImage());
        assertFalse("Should never map a null plane definition.", 
                target.contains(null));
        assertTrue("Should have added the mapping.", 
                target.contains(key));
        key = new PlaneDef(PlaneDef.XY, 1);
        assertFalse("No such a mapping was ever added.", 
                target.contains(key));
    }
    
    public void testClear()
    {
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        target.add(key, new FakeBufferedImage());
        target.clear();
        assertEquals("Should have cleared the cache.", 
                0, target.getCache().size());
    }
    
    public void testClearFuture()
    {
        //Create mock Future and set up expected calls.  Then add it to cache.
        MockFuture mf = new MockFuture();
        mf.cancelExecution();  //Clearing should cancel any future in cache.
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        target.add(key, mf);
        
        //Transition mock to verification mode.
        mf.activate();
        
        //Test.
        target.clear();
        assertEquals("Should have cleared the cache.", 
                0, target.getCache().size());
        
        //Make sure all expected calls were performed.
        mf.verify();
    }
    */
}
