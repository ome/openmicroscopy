/*
 * org.openmicroscopy.shoola.util.concur.TestSemaphore
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

package org.openmicroscopy.shoola.util.concur;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Tests the operation of {@link Semaphore} in a single-threaded
 * environment.
 * Makes sure that state-transitions are correct.
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
public class TestSemaphore
    extends TestCase
{

    public void testSemaphore()
    {
        String NO_MOD = "Constructor shouldn't modify initial count.";
        Semaphore s = new Semaphore(0);
        assertEquals(NO_MOD, 0, s.getCount());
        s = new Semaphore(-1);
        assertEquals(NO_MOD, -1, s.getCount());
        s = new Semaphore(1);
        assertEquals(NO_MOD, 1, s.getCount());
    }

    public void testDown()
    {
        String NOT_1 = "A down should decrease the count of 1.";
        Semaphore s = new Semaphore(2);
        try {
            s.down();
            assertEquals(NOT_1, 1, s.getCount());
            s.down();
            assertEquals(NOT_1, 0, s.getCount());
        } catch (InterruptedException ie) {
            fail("Unexpected, the thread was not interrupted.");
        }
    }
    
    public void testDownTimeout()
    {
        String NOT_1 = "A down should decrease the count of 1.",
            NOT_TRUE = "A down should return true after decreasing the count.";
        Semaphore s = new Semaphore(2);
        try {
            assertTrue(NOT_TRUE, s.down(0));
            assertEquals(NOT_1, 1, s.getCount());
            assertTrue(NOT_TRUE, 
                    s.down(5000));  //Timeout should be ignored b/c count is 1.
            assertEquals(NOT_1, 0, s.getCount());
            assertFalse("A down should return false if it doesn't decrease "+
                    "the count.", s.down(-1));
            assertEquals("A down shouldn't decrease the count if already zero.", 
                    0, s.getCount());
        } catch (InterruptedException ie) {
            fail("Unexpected, the thread was not interrupted.");
        }
    }

    public void testUp()
    {
        String NOT_1 = "An up should increase the count of 1.";
        Semaphore s = new Semaphore(-1);
        s.up();
        assertEquals(NOT_1, 0, s.getCount());
        s.up();
        assertEquals(NOT_1, 1, s.getCount());
        s.up();
        assertEquals(NOT_1, 2, s.getCount());
    }

}
