/*
 * org.openmicroscopy.shoola.env.data.events.TestDSCallOutcomeEvent
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

package org.openmicroscopy.shoola.env.data.events;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Routine unit test for <code>DSCallOutcomeEvent</code>.
 * We verify behavior in all states defined by the class.
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
public class TestDSCallOutcomeEvent
    extends TestCase
{

    public void testCancelled()
    {
        DSCallOutcomeEvent target = new DSCallOutcomeEvent();
        assertEquals("State should be CANCELLED.", 
                DSCallOutcomeEvent.CANCELLED, target.getState());
        assertTrue("Was cancelled.", target.wasCancelled());
        assertFalse("Shoud have no exception.", target.hasException());
        assertNull("Shoud have no exception.", target.getException());
        assertFalse("Shoud have no result.", target.hasResult());
        assertNull("Shoud have no result.", target.getResult());
    }
    
    public void testError()
    {
        Exception e = new Exception();
        DSCallOutcomeEvent target = new DSCallOutcomeEvent(e);
        assertEquals("State should be ERROR.", 
                DSCallOutcomeEvent.ERROR, target.getState());
        assertFalse("Wasn't cancelled.", target.wasCancelled());
        assertTrue("Shoud have an exception.", target.hasException());
        assertSame("Returned wrong exception.", e, target.getException());
        assertFalse("Shoud have no result.", target.hasResult());
        assertNull("Shoud have no result.", target.getResult());
    }
    
    public void testError1()
    {
        Exception e = null;
        try {
            new DSCallOutcomeEvent(e);
            fail("Should accept a null exception.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
    }
    
    public void testNoResult()
    {
        Object result = null;
        DSCallOutcomeEvent target = new DSCallOutcomeEvent(result);
        assertEquals("State should be NO_RESULT.", 
                DSCallOutcomeEvent.NO_RESULT, target.getState());
        assertFalse("Wasn't cancelled.", target.wasCancelled());
        assertFalse("Shoud have no exception.", target.hasException());
        assertNull("Shoud have no exception.", target.getException());
        assertFalse("Shoud have no result.", target.hasResult());
        assertNull("Shoud have no result.", target.getResult());
    }
    
    public void testHasResult()
    {
        Object result = new Object();
        DSCallOutcomeEvent target = new DSCallOutcomeEvent(result);
        assertEquals("State should be HAS_RESULT.", 
                DSCallOutcomeEvent.HAS_RESULT, target.getState());
        assertFalse("Wasn't cancelled.", target.wasCancelled());
        assertFalse("Shoud have no exception.", target.hasException());
        assertNull("Shoud have no exception.", target.getException());
        assertTrue("Shoud have a result.", target.hasResult());
        assertSame("Returned wrong result.", result, target.getResult());
    }

}
