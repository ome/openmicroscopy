/*
 * org.openmicroscopy.shoola.env.rnd.TestImageFutureCacheExtract
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
import org.openmicroscopy.shoola.util.concur.tasks.MockFuture;

/** 
 * Unit test for {@link ImageFutureCache}.
 * Verifies that images are correctly extracted from cache and from futures
 * in the absence of exceptions. 
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
public class TestImageFutureCacheExtract
    extends TestCase
{

    private static final int    CACHE_SIZE = 1;  //Passed to target.
    private static final int    IMAGE_SIZE = 1;  //Passed to target.
    
    private ImageFutureCache    target;  //Object under test.
    
    
    public void setUp()
    {
        target = new ImageFutureCache(CACHE_SIZE, IMAGE_SIZE, 
                                        new MockNavigationHistory());
    }
    
    public void testExtract()
        throws Exception
    {
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        BufferedImage img = new FakeBufferedImage();
        target.add(key, img); //Assume it works -- verified by other unit tests.
        assertSame("Wrong entry was associated to the passed key.", 
                img, target.extract(key));
        assertTrue("Should never discard an entry after extracting.", 
                target.getCache().containsValue(img));
    }
    
    public void testExtractNoValue()
        throws Exception
    {
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        BufferedImage img = new FakeBufferedImage();
        target.add(key, img); //Assume it works -- verified by other unit tests.
        key.setZ(1);
        assertNull("No entry was associated to the passed key.", 
                target.extract(key));
        assertTrue("Should never discard an entry after extracting.", 
                target.getCache().containsValue(img));
    }
    
    public void testExtractFromFuture()
        throws Exception
    {   
        //Create mock Future and set up expected calls.  Then add it to cache.
        MockFuture mf = new MockFuture();
        BufferedImage img = new FakeBufferedImage();
        mf.getResult(img, null);
        mf.cancelExecution();
        PlaneDef key = new PlaneDef(PlaneDef.XY, 0);
        target.add(key, mf);  //Assume it works -- verified by other unit tests.
        
        //Transition mock to verification mode.
        mf.activate();
        
        //Test.
        assertSame("Should have extracted the image returned by the future.", 
                img, target.extract(key));
        assertEquals("Shouldn't have removed the entry.", 
                1, target.getCache().size());
        assertTrue("Should have replaced the future with the extracted image.", 
                target.getCache().containsValue(img));
        
        //Make sure all expected calls were performed.
        mf.verify();
    }
    
}
