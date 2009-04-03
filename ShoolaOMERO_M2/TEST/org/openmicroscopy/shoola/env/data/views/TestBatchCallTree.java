/*
 * org.openmicroscopy.shoola.env.data.views.TestBatchCallTree
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

package org.openmicroscopy.shoola.env.data.views;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.events.DSCallOutcomeEvent;
import org.openmicroscopy.shoola.util.concur.tasks.FakeCmdProcessor;

/** 
 * Verifies the execution of a {@link BatchCallTree} is carried out as
 * expected.
 * This test is performed with the help of a {@link FakeBatchCallTree} and
 * a {@link SyncBatchCallMonitor}.  The first provides a concrete subclass
 * of {@link BatchCallTree} and builds the actual call tree.  Moreover, it
 * replaces the default 
 * {@link org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor} with a
 * {@link org.openmicroscopy.shoola.util.concur.tasks.SyncProcessor} (so the
 * tree will be executed within the JUnit thread) and the default call monitor
 * (a {@link BatchCallMonitor}) with a {@link SyncBatchCallMonitor} (which will
 * dispach the tree's execution events in the JUnit thread).
 * Because all the {@link SyncBatchCallMonitor} does is overriding the 
 * <code>deliver</code> method, this test implicitly verifies the behavior of
 * {@link BatchCallMonitor}.  In turn, an instance of this latter class would
 * not work properly if the {@link BatchCallTree} didn't delegate calls to
 * its root node, so we also implicitly verify delegation.
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
public class TestBatchCallTree
    extends TestCase
{
    
    //Object under test -- it's concrete BatchCallTree that allows us to
    //verify the code in BatchCallTree (which is abstract).
    private FakeBatchCallTree       target; 
    
    //Used to verify the operation of target.
    private MockDSCallEventListener observer;
    
    
    private void setUpFeedbackEvents(int howMany)
    {
        int leafs = target.getActualLeavesCount();
        int m = Math.min(leafs, howMany);
        for (int i = 0, perc = 0; i < m; ++i) {
            perc = i*100/leafs;
            observer.eventFired(
                    new DSCallFeedbackEvent(perc, 
                                            target.getLeafDescription(i+1),
                                            new Integer(i+1)),
                    null);
        }    
        if (m < howMany)  //Normal outcome, set last feedback event.
            observer.eventFired(new DSCallFeedbackEvent(100, null, null), null);
    }
    
    private void setUpNormalOutcome()
    {
        observer.eventFired(
                new DSCallOutcomeEvent(target.getExpectedResult()), null);
    }
    
    private void setUpExceptionOutcome(int faultyLeaf)
    {
        Exception exc = new Exception("L"+faultyLeaf);
        target.setFaultyLeaf(faultyLeaf, exc);
        observer.eventFired(new DSCallOutcomeEvent(exc), null);
    }
    
    private void verifySingleExecution()
    {
        try {
            target.exec(observer);
            fail("A tree can't be executed more than once.");
        } catch (IllegalStateException ise) {
            //Ok, expected.
        }
    }
    
    protected void setUp()
    {
        target = new FakeBatchCallTree();
        observer = new MockDSCallEventListener();
    }    
    
    public void testNormalOutcome()
    {
        //Set up observer's expectations for feedback events and outcome
        //event to be received during target.exec().
        setUpFeedbackEvents(target.getActualLeavesCount()+1);
        setUpNormalOutcome();
        
        observer.activate();  //Transition mock to verification mode.
        target.exec(observer);  //Test.
        observer.verify();  //Make sure all expected calls were performed.
        verifySingleExecution();  //Make sure won't allow re-exec.
    }
    
    public void testException1()
    {
        //Set up observer's expectations for feedback events and outcome
        //event to be received during target.exec().
        setUpFeedbackEvents(1);  //FE[perc: 0, status: L1] b/f L1 is exec'd.
        setUpExceptionOutcome(1);  //L1 will throw an exc when exec'd.
        
        observer.activate();  //Transition mock to verification mode.
        target.exec(observer);  //Test.
        observer.verify();  //Make sure all expected calls were performed.
        verifySingleExecution();  //Make sure won't allow re-exec.
    }
    
    public void testException2()
    {
        //Set up observer's expectations for feedback events and outcome
        //event to be received during target.exec().
        setUpFeedbackEvents(2);  //FE[perc: 0, status: L1] b/f L1 is exec'd.
                                 //FE[perc: 20, status: L2] b/f L2 is exec'd.
        setUpExceptionOutcome(2);  //L2 will throw an exc when exec'd.
        
        observer.activate();  //Transition mock to verification mode.
        target.exec(observer);  //Test.
        observer.verify();  //Make sure all expected calls were performed.
        verifySingleExecution();  //Make sure won't allow re-exec.
    }
    
    public void testException3()
    {
        //Set up observer's expectations for feedback events and outcome
        //event to be received during target.exec().
        setUpFeedbackEvents(3);  //FE[perc: 0, status: L1] b/f L1 is exec'd.
                                 //FE[perc: 20, status: L2] b/f L2 is exec'd.
                                 //FE[perc: 40, status: L3] b/f L3 is exec'd.
        setUpExceptionOutcome(3);  //L3 will throw an exc when exec'd.
        
        observer.activate();  //Transition mock to verification mode.
        target.exec(observer);  //Test.
        observer.verify();  //Make sure all expected calls were performed.
        verifySingleExecution();  //Make sure won't allow re-exec.
    }
    
    public void testException4()
    {
        //Set up observer's expectations for feedback events and outcome
        //event to be received during target.exec().
        setUpFeedbackEvents(4);  //FE[perc: 0, status: L1] b/f L1 is exec'd.
                                 //FE[perc: 20, status: L2] b/f L2 is exec'd.
                                 //FE[perc: 40, status: L3] b/f L3 is exec'd.
                                 //FE[perc: 60, status: L4] b/f L4 is exec'd.
        setUpExceptionOutcome(4);  //L4 will throw an exc when exec'd.
        
        observer.activate();  //Transition mock to verification mode.
        target.exec(observer);  //Test.
        observer.verify();  //Make sure all expected calls were performed.
        verifySingleExecution();  //Make sure won't allow re-exec.
    }
    
    public void testException5()
    {
        //Set up observer's expectations for feedback events and outcome
        //event to be received during target.exec().
        setUpFeedbackEvents(5);  //FE[perc: 0, status: L1] b/f L1 is exec'd.
                                 //FE[perc: 20, status: L2] b/f L2 is exec'd.
                                 //FE[perc: 40, status: L3] b/f L3 is exec'd.
                                 //FE[perc: 60, status: L4] b/f L4 is exec'd.
                                 //FE[perc: 80, status: L5] b/f L5 is exec'd.
        setUpExceptionOutcome(5);  //L5 will throw an exc when exec'd.
        
        observer.activate();  //Transition mock to verification mode.
        target.exec(observer);  //Test.
        observer.verify();  //Make sure all expected calls were performed.
        verifySingleExecution();  //Make sure won't allow re-exec.
    }

    //This tests verifies what happens if the computation is cancelled
    //before the executor thread has a chance to run it.
    public void testCancellation()
    {
        //Set up observer's expectations for outcome event.
        observer.eventFired(new DSCallOutcomeEvent(), null);
        
        //We need to replace the default SyncProcessor with
        //one that does nothing.
        target.setProcessor(new FakeCmdProcessor());
        
        //Transition mock to verification mode.
        observer.activate();  
        
        //Test.
        CallHandle handle = target.exec(observer);
        //B/c the processor does nothing, the internal ExecCommand is not run.
        //So it's still sitting in the READY state.
        
        //We can now cancel.  This will eventually result in a state transition
        //READY->CANCELLED within the internal ExecCommand.  At the end of this
        //transition onCancel() is called, which will have to result in a call
        //to observer.eventFired().  Note that this transition *never* causes
        //an interruption, so we can safely run this test w/ the JUnit thread.
        handle.cancel();
        
        //Make sure all expected calls were performed.
        observer.verify();  
        
        verifySingleExecution();  //Make sure won't allow re-exec.
    }
    //NOTE: what about the other cancellation paths?  If cancellation is
    //detected after the computation has finished, then we fall back to either
    //the normal outcome or exception paths -- all of which are already covered 
    //by the other tests in this class.  So we would only have to verify what 
    //happens if cancellation is detected in the midst of execution.  We should
    //tests that feedback events are received up to the cancellation point and
    //then an outcome event is received that informs of cancellation.  However,
    //this is basically what we've been doing in the exception tests.  The only
    //difference with cancellation is that onCancel() should be invoked instead
    //of onAbort().  But we've already verified above what happens if onCancel()
    //is called.  In conclusion, we can skip the test to verify cancellation in
    //the midst of execution.
    
}
