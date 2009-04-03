/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestFutureStateDep
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
 * Verifies the the state-dependence constructs in {@link Future}.
 * A <code>getResult</code> call can only be executed when the computation is
 * finished.
 * Makes sure liveness is attained.
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
public class TestFutureStateDep
        extends TestCase
{
    
    private Future          future;  //Object to test.
    private Object          result;  //To transfer value from alternate flow.
    private boolean         callSetResult;  //or setException in alt flow.
    private ThreadSupport   threads;  //To manage main/alt flows.
    
    
    public void setUp()
    {
        result = new Exception();  //So we can use it for setException too.
        
        threads = new ThreadSupport(new Runnable() {  //Alternate flow.
            public void run() { 
                if (callSetResult) future.setResult(result);
                else future.setException((Exception) result);
            }
        });
        
        future = new Future();//Not in a legal state yet (two-step init).
        ExecCommand command = new ExecCommand(new NullMultiStepTask(), 
                new NullResultAssembler(), future, new NullExecMonitor());
        future.setCommand(command);  //OK, init completed now.
        
        future.register(new ControlFlowObserver() {
            public void update(int checkPointID)
            {
                //If main thread acquired the lock during getResult, 
                //then spawn a thread to setResult/Exception.
                if (checkPointID == Future.LOCK_ACQUIRED &&
                        threads.isMainFlow())
                    threads.startAltFlow();  //Fires async setResult/Exception.
                
                //NOTE: setResult/Exception can't proceed until getResult in
                //the main thread releases the lock when entering wait.
            }
        });
    }
    
    public void testGetResultAcceptance() 
        throws ExecException, InterruptedException
    {
        callSetResult = true;
        Object r = future.getResult();
        threads.awaitAltFlow();
        assertEquals("getResult should wait when in WAITING_FOR_RESULT.", 
                result, r);
    }
    
    public void testGetResultAcceptance2()
        throws InterruptedException
    {
        callSetResult = false;
        Object exc;
        try {
            exc = future.getResult();
        } catch (ExecException ee) {
            //OK, expected.  Grab the exception:
            exc = ee.getCause(); 
        } 
        threads.awaitAltFlow();
        assertEquals("getResult should wait when in WAITING_FOR_RESULT.", 
                result, exc);
    }
    
    public void testGetResultAcceptance3()
        throws ExecException, InterruptedException
    {
        //Disable observer, see setUp().
        future.register(null);  
        
        //Test.
        Object result = null;
        future.cancelExecution();  //command should call setResult(null).
        result = future.getResult();  //Deadlock if above fails.
        assertNull("Null should be returned if the service is cancelled "+
                "before execution.", result);
    }

}
