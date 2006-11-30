/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestFutureSync
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
import org.openmicroscopy.shoola.util.concur.ControlFlowObserver;
import org.openmicroscopy.shoola.util.concur.ThreadSupport;

/** 
 * Verifies the safety of {@link Future}.
 * We test that <code>getResult</code>, <code>setResult</code>, and 
 * <code>setException</code> calls are serialized in the presence of multiple
 * threads acting on the object.
 *
 * @see org.openmicroscopy.shoola.util.concur.ThreadSupport
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
public class TestFutureSync
    extends TestCase
{
    
    private Future          future;  //Object to test.
    private int             fState;  //To transfer value from alternate flow.
    private ThreadSupport   threads;  //To manage main/alt flows.
   
    
    public void setUp()
    {
        threads = new ThreadSupport(new Runnable() {  //Alternate flow.
            public void run() { fState = future.getState(); }
        });
        //NOTE: getState() is sync.  So if it is called while get/set is being
        //executed in the main thread, then the alternate thread will have
        //to wait until the lock is released.  This is enough to test safety,
        //there's no need to use more than two threads and verify all
        //possible combinations of getResult/setResult/setException.
        
        future = new Future();  //Not in a legal state yet (two-step init).
        future.setCommand(new MockExecCommand());  //OK, init completed now.
        
        future.register(new ControlFlowObserver() {
            public void update(int checkPointID)
            {  //Called w/in main thread.
                if (checkPointID == Future.LOCK_ACQUIRED) {
                    //get/set in progress.  Spawn alt thread to retrieve state.
                    threads.startAltFlow();  //future.getState() will be called.
                    
                    threads.pauseMainFlow();
                    //NOTE: Even though pausing the main flow doesn't guarantee
                    //that getState() will be invoked in the mean time, the
                    //2-second delay used should in practice make this extremely
                    //likely to happen.
                }
            }
        });
        
        //NOTE: If getState() gets executed b/f get/set modifies the state,
        //then we would also get the wrong value for fState.  In this case,
        //we can deduce that locks were screwed up and thus fail the test.
        //The test would be optimal if we could make sure that getState() is
        //actually invoked while get/set is being executed (in practice this is
        //quite sure though).  In any case, pausing the main thread is the best
        //we can do as there's no way to tell whether a thread is waiting to
        //acquire a lock.
    }
    
    public void testSetResult()
    {
        future.setResult(null);
        threads.awaitAltFlow();
        assertEquals("Concurrent access to future state.", 
                Future.HAS_RESULT, fState);
    }
    
    public void testSetException()
    {
        future.setException(null);
        threads.awaitAltFlow();
        assertEquals("Concurrent access to future state.", 
                Future.HAS_EXCEPTION, fState);
    }
    
    public void testGetResult() 
        throws ExecException
    {
        //Keeps track of how many times update (see below) gets called.
        //In fact, we have the following nested sequence: future.getResult ->
        //observer.update -> future.setResult -> observer.update
        final int[] updateCount = {0};  
        
        future.register(new ControlFlowObserver() {
            public void update(int checkPointID)
            {  //Called w/in main thread.
                if (updateCount[0]++ != 0) return;  //setResult, see below.
                if (checkPointID == Future.LOCK_ACQUIRED) {
                    //getResult in progress. Spawn alt thread to retrieve state.
                    threads.startAltFlow();  //future.getState() will be called.
                    
                    threads.pauseMainFlow();
                    //NOTE: Even though pausing the main flow doesn't guarantee
                    //that getState() will be invoked in the mean time, the
                    //2-second delay used should in practice make this extremely
                    //likely to happen.
                    
                    future.setResult(null);  //Change state.
                }
            }
        });
        
        try {
            future.getResult();
        } catch (InterruptedException ie) {
            //OK, expected.
        }
        threads.awaitAltFlow();
        assertEquals("Concurrent access to future state.", 
                Future.HAS_RESULT, fState);
    }

}
