/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestFuture
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

package org.openmicroscopy.shoola.util.concur.tasks;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Tests the operation of {@link Future} in a single-threaded
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
public class TestFuture
    extends TestCase
{

    //Object under test.
    private Future  future;
    
    
    public void setUp()
    {
        future = new Future();  //Not in a legal state yet (two-step init).
        future.setCommand(new MockExecCommand());  //OK, init completed now.
    }
    
    public void testFuture() 
    {
        assertEquals(
                "After initialization, state should be WAITING_FOR_RESULT.", 
                Future.WAITING_FOR_RESULT, future.getState());
    }
    
    public void testSetResult()
        throws ExecException, InterruptedException
    {
        Object result = new Object();
        future.setResult(result);
        assertEquals("After setResult(), state should be HAS_RESULT.", 
                Future.HAS_RESULT, future.getState());
        try {
            assertEquals("getResult() returned the wrong object.", 
                    result, future.getResult());
            future.setResult(result);
            fail("A Future shouldn't be reused for another result.");
        } catch (Error e) {
            //OK, expected.
        }
        try {
            future.setException(new Exception());
            fail("setException() should error if called after setResult().");
        } catch (Error e) {
            //OK, expected.
        }
    }
    
    public void testSetNullResult()
        throws ExecException, InterruptedException
    {
        future.setResult(null);
        assertEquals("After setResult(), state should be HAS_RESULT.", 
                Future.HAS_RESULT, future.getState());
        assertEquals("getResult() should have returned null.", 
                null, future.getResult()); 
    }
    
    public void testSetException()
        throws InterruptedException
    {
        Exception srvFailure = new Exception();
        future.setException(srvFailure);
        assertEquals("After setException(), state should be HAS_EXCEPTION.", 
                Future.HAS_EXCEPTION, future.getState());
        try {
            future.getResult();
            fail("getResult() should have thrown an ExecException.");
        } catch (ExecException ee) {
            //OK expected, but check the original exception:
            assertEquals("ExecException should wrap the original exception.", 
                    srvFailure, ee.getCause());
        } 
        try {
            future.setException(srvFailure);
            fail("A Future shouldn't be reused for another exception.");
        } catch (Error e) {
            //OK, expected.
        }
        try {
            future.setResult(null);
            fail("setResult() should error if called after setException().");
        } catch (Error e) {
            //OK, expected.
        }
    }

    public void testSetNullException()
        throws InterruptedException
    {
        future.setException(null);
        assertEquals("After setException(), state should be HAS_EXCEPTION.", 
                Future.HAS_EXCEPTION, future.getState());
        try {
            future.getResult();
            fail("getResult() should have thrown an ExecException.");
        } catch (ExecException ee) {
            //OK expected, but check the original exception:
            assertTrue(
                    "setException(null) should result in an Error being set.", 
                    ee.getCause() instanceof Error);
        }
    }
    
    public void testUnimplementedGetResult() 
        throws ExecException, InterruptedException
    {
        try {
            future.getResult(0);
            fail("Method has been implemented, remove this test.");
        } catch (NoSuchMethodError nsme) {
            //OK, expected.
        }
    }
    
}
