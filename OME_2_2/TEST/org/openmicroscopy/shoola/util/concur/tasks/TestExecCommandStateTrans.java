/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestExecCommandStateTrans
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
import org.openmicroscopy.shoola.util.concur.ThreadSupport;

/** 
 * Verifies that {@link ExecCommand} performs the correct state transitions
 * in a single-threaded environment.
 * Also verifies state-dependent actions and message acceptance.
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
public class TestExecCommandStateTrans
    extends TestCase
{

    private ExecCommand     target;  //Object under test.
    private int             tState;  //To transfer target's state;
    private boolean         cancel;  //Tells whether to cancel target's exec.
    private boolean         nestedRun;  //Tells whether to nest target.run().
    private class CancelTask //Instantiated as the task that target will exec.
        implements Runnable {
        public void run() { 
            tState = target.getState();
            if (cancel) target.cancel(); 
            if (nestedRun) target.run();
        }
    }
    
    private void transitionToCancelled()
    {
        cancel = true;
        /* The above will result in the interrupted status of the thread
         * running target.run() being set.  So if we invoke it w/in the  
         * JUnit thread, we'll have to clear the interrupted status upon
         * returning from this test method.  B/c implementation characteristics
         * of interruption-based methods are uncertain, we use a separate
         * thread to execute target.run() and then we simply discard that
         * thread.  This way we won't interrupt JUnit and we avoid any possible
         * side effect.
         */
        ThreadSupport ts = new ThreadSupport(new Runnable() {  //Alt flow.
            public void run() { target.run(); }
        });
        ts.startAltFlow();
        ts.awaitAltFlow();       
    }
    
    public void setUp()
    {
        Future future = new Future();//Not in a legal state yet (two-step init).
        target = new ExecCommand(new TaskAdapter(new CancelTask()), 
                new NullResultAssembler(), future, new NullExecMonitor());
        future.setCommand(target);  //OK, init completed now.
        cancel = false;  //Only set to true in transitionToCancelled().
        nestedRun = false;  //true causes an Error when calling target.run().
    }
    
    public void testNewReady()
    {
        assertEquals("State should be READY after creation.", 
                ExecCommand.READY, target.getState());
    }
    
    public void testReadyCancelled()
    {
        target.cancel();
        assertEquals("Should transition to CANCELLED if cancel() is dispatched"+
                " when the object is in the READY state.", 
                ExecCommand.CANCELLED, target.getState());
    }
    
    public void testReadyExecuting()
    {
        target.run();
        assertEquals("State should be EXECUTING if run() is dispatched "+
                "when the object is in the READY state.", 
                ExecCommand.EXECUTING, tState);  //Grabbed w/in run().
    }
    
    public void testExecutingFinished()
    {
        target.run();
        assertEquals("State should be FINISHED if run() terminates without "+
                "being cancelled.", 
                ExecCommand.FINISHED, target.getState());
    }
    
    public void testExecutingCancelled()
    {
        transitionToCancelled();
        assertEquals("State should be CANCELLED if run() terminates because "+
                "of cancellation.", 
                ExecCommand.CANCELLED, target.getState());
    }
    
    public void testCancelWhenCancelled()
    {
        transitionToCancelled();
        target.cancel();
        assertEquals("cancel() should do nothing if dispatched when the object"+
                " is in the CANCELLED state and the state shouldn't change.", 
                ExecCommand.CANCELLED, target.getState());
    }
    
    public void testRunWhenCancelled()
    {
        transitionToCancelled();
        tState = -1;
        target.run();  //Should do nothing (normally sets state = target.state).
        assertEquals("run() shouldn't change the state if dispatched when the "+
                "object is in the CANCELLED state.", 
                ExecCommand.CANCELLED, target.getState());
        assertEquals("Service shouldn't be executed when the object is in the "+
                "CANCELLED state.", 
                -1, tState);
    }
    
    public void testCancelWhenFinished()
    {
        target.run();
        target.cancel();
        assertEquals("cancel() should do nothing if dispatched when the object"+
                " is in the FINISHED state and the state shouldn't change.", 
                ExecCommand.FINISHED, target.getState());
    }
    
    public void testRunWhenFinished()
    {
        target.run();
        try {
            tState = -1;
            target.run();
            fail("run() should error if dispatched when the object is in "+
                    "the FINISHED state.");
        } catch (Error e) {
            //OK, expected, but check the state:
            assertEquals("run() shouldn't change the state if dispatched when "+
                    "the object is in the FINISHED state.", 
                    ExecCommand.FINISHED, target.getState());
            assertEquals("Service shouldn't be executed when the object is in"+
                    " the FINISHED state.", 
                    -1, tState);
        }
    }
    
    public void testRunWhenExecuting()
    {
        nestedRun = true;  //Enables nested target.run() call.
        try {
            target.run();  //Will call target.run() again.
            fail("run() should error if dispatched more than once when "+
                    "the object is in the EXECUTING state.");
        } catch (RuntimeException re) {
            //OK, expected.  The NullExecMonitor caught the Error when onAbort
            //was called and re-threw a RuntimeException to wrap it.  Let's
            //verify the original Error:
            assertTrue("run() should throw an Error if dispatched more than "+
                    "once when the object is in the EXECUTING state.", 
                    re.getCause() instanceof Error);
            
            //Now check the state to make sure exit action got dispatched:
            assertEquals("If an exception is thrown during run(), the exit "+
                    "action should be dispatched and the object eventually "+
                    "transitioned to the FINISHED state.", 
                    ExecCommand.FINISHED, target.getState());
        }
    }
    
}
