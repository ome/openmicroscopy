/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestExecCommandWorkflow2
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
 * Verifies the failure and cancellation flows within the execution workflow.
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
public class TestExecCommandWorkflow2
    extends TestCase
{

    private ExecCommand         target;  //Object under test.
    private MockMultiStepTask   task;  //Mock object.
    private MockResultAssembler assembler;  //Mock object.
    private MockExecMonitor     observer;  //Mock object.
    private Future              future;
    
    
    private void transitionMocksToVerificationMode()
    {
        task.activate();
        assembler.activate();
        observer.activate();
    }
    
    private void ensureAllExpectedCallsWerePerformed()
    {
        task.verify();
        assembler.verify();
        observer.verify();
    }
    
    private void doTestFailure(Throwable expected)
        throws InterruptedException
    {
        transitionMocksToVerificationMode();
        
        //Test.
        target.run();
        try {
            future.getResult();
        } catch (ExecException ee) {
            //OK expected, but check original exception:
            assertEquals("Wrong exception set into the future.",
                    expected, ee.getCause());
        }
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void setUp()
    {
        task = new MockMultiStepTask();
        assembler = new MockResultAssembler();
        observer = new MockExecMonitor();
        future = new Future();  //Not in a legal state yet (two-step init).
        target = new ExecCommand(task, assembler, future, observer);
        future.setCommand(target);  //OK, init completed now.
    }
    
    public void testFailAtOnStart() 
        throws InterruptedException
    {
        //Set up expected calls.
        RuntimeException exc = new RuntimeException();
        observer.onStart(exc);
        observer.onAbort(exc, null);
        
        doTestFailure(exc);
    }
    
    public void testFailAtIsDone() 
        throws InterruptedException
    {
        //Set up expected calls.
        RuntimeException exc = new RuntimeException();
        observer.onStart(null);
        task.isDone(true, exc);
        observer.onAbort(exc, null);
        
        doTestFailure(exc);
    }
    
    public void testFailAtDoStep()
        throws InterruptedException
    {
        //Set up expected calls.
        RuntimeException exc = new RuntimeException();
        observer.onStart(null);
        task.isDone(false, null);
        task.doStep(null, exc);
        observer.onAbort(exc, null);
        
        doTestFailure(exc);
    }
    
    public void testFailAtAdd()
        throws InterruptedException
    {
        //Set up expected calls.
        RuntimeException exc = new RuntimeException();
        observer.onStart(null);
        task.isDone(false, null);
        task.doStep(null, null);
        assembler.add(null, exc);
        observer.onAbort(exc, null);
        
        doTestFailure(exc);
    }
    
    public void testFailAtUpdate()
        throws InterruptedException
    {
        //Set up expected calls.
        RuntimeException exc = new RuntimeException();
        observer.onStart(null);
        task.isDone(false, null);
        task.doStep(null, null);
        assembler.add(null, null);
        observer.update(1, exc);
        observer.onAbort(exc, null);
        
        doTestFailure(exc);
    }
    
    public void testFailAtAssemble()
        throws InterruptedException
    {
        //Set up expected calls.
        RuntimeException exc = new RuntimeException();
        observer.onStart(null);
        task.isDone(true, null);
        assembler.assemble(null, exc);
        observer.onAbort(exc, null);
        
        doTestFailure(exc);
    }
    
    public void testFailAtOnEnd()
        throws InterruptedException
    {
        //Set up expected calls.
        RuntimeException exc = new RuntimeException();
        observer.onStart(null);
        task.isDone(true, null);
        assembler.assemble(null, null);
        observer.onEnd(null, exc);
        observer.onAbort(exc, null);
        
        doTestFailure(exc);
    }
    
    public void testFailAtOnCancel()
        throws ExecException, InterruptedException
    {  //NOTE: This test also verifies the cancellation flow.
        
        //Set up expected calls.
        RuntimeException exc = new RuntimeException();
        observer.onStart(null);
        task.isDone(false, null);
        task.doStep(null, new InterruptedException());  //Cancellation.
        observer.onCancel(exc);
        
        transitionMocksToVerificationMode();
        
        //Test.
        try {
            target.run();
        } catch (RuntimeException re) {
            //OK expected, but check original exception:
            assertEquals("Exceptions thrown by onCancel() should just go "+
                    "up in the call stack.",
                    exc, re);
        }
        assertNull("If an exception is thrown by onCancel(), then the future's"+
                " result should be set to null.",
                future.getResult());
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testFailAtOnAbort()
        throws InterruptedException
    {
        //Set up expected calls.
        RuntimeException onStartExc = new RuntimeException(), 
                            onAbortExc = new RuntimeException();
        observer.onStart(onStartExc);
        observer.onAbort(onStartExc, onAbortExc);
        
        transitionMocksToVerificationMode();
        
        //Test.
        try {
            target.run();
        } catch (RuntimeException re) {
            //OK expected, but check original exception:
            assertEquals("Exceptions thrown by onAbort() should just go "+
                    "up in the call stack.",
                    onAbortExc, re);
        }
        try {
            future.getResult();
        } catch (ExecException ee) {
            //OK expected, but check original exception:
            assertEquals("If an exception is thrown by onAbort(), then the "+
                    "original exception that caused onAbort() to be called "+
                    "should be set into the future.",
                    onStartExc, ee.getCause());
        }
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testCancelWhenReady() 
        throws ExecException, InterruptedException
    {  //NOTE: cancellation when Executing verified by testFailAtOnCancel().
        
        //Set up expected calls.
        observer.onCancel(null);
        
        transitionMocksToVerificationMode();
        
        //Test.
        target.cancel();  //Should transition from READY to CANCELLED.
        assertNull("No result was ever produced.", future.getResult());
        /* 
         * NOTE: the above will deadlock if the post-condition that 
         * future.setResult(null) is invoked doesn't hold.
         */
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void testCancelWithExcWhenReady() 
        throws ExecException, InterruptedException
    {  //NOTE: cancellation when Executing verified by testFailAtOnCancel().
        
        //Set up expected calls.
        RuntimeException exc = new RuntimeException();
        observer.onCancel(exc);
        
        transitionMocksToVerificationMode();
        
        //Test.
        try {
            target.cancel();  //Should transition from READY to CANCELLED.
            fail("Any runtime exception thrown by observer.onCancel() during "+
                 "cancellation while in the Ready state should just be "+
                 "propagated up in the call stack.");
        } catch (RuntimeException re) {
            //OK, expected.  Just double-check:
            assertSame("Wrong exception was propagated up in the call stack.", 
                    exc, re);
        }
        
        assertNull("No result was ever produced.", future.getResult());
        /* 
         * NOTE: the above will deadlock if the post-condition that 
         * future.setResult(null) is invoked doesn't hold.
         */
        
        ensureAllExpectedCallsWerePerformed();
    }
    
}
