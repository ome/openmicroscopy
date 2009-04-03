/*
 * org.openmicroscopy.shoola.env.rnd.TestImageFutureCacheSize0
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
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.util.concur.tasks.MockFuture;
import org.openmicroscopy.shoola.util.tests.common.FakeBufferedImage;

/** 
 * Unit test for {@link ImageFutureCache}.
 * Verifies operation of the cache when its size is <code>0</code>.
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
public class TestImageFutureCacheSize0
    extends TestCase
{

    private static final int    CACHE_SIZE = 10;  //Passed to target.
    private static final int    IMAGE_SIZE = 11;  //Passed to target.
    
    //private ImageFutureCache    target;  //Object under test.
    
    
    public void setUp()
    {
        //target = new ImageFutureCache(CACHE_SIZE, IMAGE_SIZE, 
        //                                new MockNavigationHistory());
    }
    
    public void testAdd()
    {
        /*
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        target.add(key, new FakeBufferedImage());
        assertEquals("Shouldn't cache if MAX_ENTRIES is 0.", 
                0, target.getCache().size());
                */
    }
    
    /*
    public void testAddFuture()
    {
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        target.add(key, new MockFuture());
        assertEquals("Shouldn't cache if MAX_ENTRIES is 0.", 
                0, target.getCache().size());
    }
    
    public void testContains()
    {
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        target.add(key, new FakeBufferedImage());
        assertFalse("Should never map a null plane definition.", 
                target.contains(null));
        assertFalse("Should contain no mappings if size is 0.", 
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
    */
}
