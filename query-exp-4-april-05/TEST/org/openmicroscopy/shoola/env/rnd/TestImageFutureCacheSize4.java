/*
 * org.openmicroscopy.shoola.env.rnd.TestImageFutureCacheSize4
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
import org.openmicroscopy.shoola.util.math.geom2D.Line;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint;
import org.openmicroscopy.shoola.util.tests.common.FakeBufferedImage;

/** 
 * Unit test for {@link ImageFutureCache}.
 * Verifies operation of the cache when its size is <code>4</code>.
 * These tests concentrate on the verification of the cache removal
 * algorithm.
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
public class TestImageFutureCacheSize4
    extends TestCase
{

    private static final int    CACHE_SIZE = 4;  //Passed to target.
    private static final int    IMAGE_SIZE = 1;  //Passed to target.
    
    
    private MockNavigationHistory navigHistory;  //Mock linked to target.
    private ImageFutureCache    target;  //Object under test.
    
    //Identify the images that will be added to the cache b/f start testing.
    //Correspond to the points O, a(0, 1), b(0, 2), c(0, 3) in the zOt plane.
    private PlaneDef   O, a, b, c;
    
    //The images associated to the above plane defs.
    private BufferedImage imgO, imgA, imgB, imgC;
    
    
    public void setUp()
    {
        //Create the cache and link it to a new mock navigation history.
        navigHistory = new MockNavigationHistory();
        target = new ImageFutureCache(CACHE_SIZE, IMAGE_SIZE, navigHistory);
        
        //Creates keys.
        O = new PlaneDef(PlaneDef.XY, 0);
        a = new PlaneDef(PlaneDef.XY, 1);
        b = new PlaneDef(PlaneDef.XY, 2);
        c = new PlaneDef(PlaneDef.XY, 3);
        
        //Create corresponding images.
        imgO = new FakeBufferedImage();
        imgA = new FakeBufferedImage();
        imgB = new FakeBufferedImage();
        imgC = new FakeBufferedImage();
        
        //Add to cache.
        target.add(c, imgC);
        target.add(a, imgA);
        target.add(O, imgO);
        target.add(b, imgB);
    }
    
    private void doTestAdd(Line currentDirection)
    {
        //Set up expected calls. 
        navigHistory.currentDirection(currentDirection);
        
        //Transition mock to verification mode.
        navigHistory.activate();
        
        //Test.
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        key.setZ(2);  //(z=2, t=0)
        BufferedImage img = new FakeBufferedImage();
        //Cache's already been filled up.  Now add a new entry to have
        //the removal algorithm kick in.
        target.add(key, img);  
        assertEquals("Should never cache more than MAX_ENTRIES.", 
                4, target.getCache().size());
        assertFalse("Should have purged imgC.", 
                target.getCache().containsValue(imgC));
        assertTrue("Should have cached the new image.", 
                target.getCache().containsValue(img));
        
        //Make sure all expected calls were performed.
        navigHistory.verify();
    }
    
    //Test add when:
    //   there exist x in C, x doesn't lie on D 
    //(C cache, D current direction)
    public void testAddCase1()
    {
        //Set the current direction D to be the z axis.  This will result
        //in c being the first element of C' that doesn't lie on D.
        Line dir = new Line(new PlanePoint(0, 0), new PlanePoint(1, 0));  
        doTestAdd(dir);
    }
    
    //Test add when:
    //   for each x in C, x lies on D
    //   and
    //   there exist x in C, x lies on negative half of D
    //(C cache, D current direction)
    public void testAddCase2()
    {
        //Set the current direction D to have unit vector aO.  This will result
        //in O, a, b, c lying on D and in c being the first element of C' that
        //falls on the negative half of D.
        Line dir = new Line(new PlanePoint(0, 1), new PlanePoint(0, 0));
        doTestAdd(dir);
    }
    
    //Test add when:
    //   for each x in C, x lies on the non-negative half of D
    //(C cache, D current direction)
    public void testAddCase3()
    {
        //Set the current direction D to be the t axis.  This will result in
        //in O, a, b, c lying on the non-negative half of D.   As c is the
        //farthest point away from p, this is also the default candidate.
        //(p is the point (2, 0) used in doTestAdd.)
        Line dir = new Line(new PlanePoint(0, 0), new PlanePoint(0, 1));  //t axis.
        doTestAdd(dir);
    }
    
    //Test add when:
    //   current direction is undefined
    public void testAddCase4()
    {
        //Set the current direction D to be undefined.  As c is the
        //farthest point away from p, this is also the default 
        //candidate.
        //(p is the point (2, 0) used in doTestAdd.)
        Line dir = null;  //Undefined.
        doTestAdd(dir);
    }
    
}
