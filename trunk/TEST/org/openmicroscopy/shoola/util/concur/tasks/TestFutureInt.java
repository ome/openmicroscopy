/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestFutureInt
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
 * Verifies that {@link Future} correctly aborts in the case of interruption.
 * Each test spawns an ad-hoc thread which is interrupted to verify how 
 * {@link Future} reacts to interruption.  The result of the test is passed
 * back into the main flow (JUnit) so to perform the needed assertions.  
 * Note that we never interrupt JUnit.  Spawning another thread might seem 
 * unecessary when we could just interrupt JUnit and then clear the interrupted
 * status after the test.  However implementation characteristics of 
 * interruption-based methods are quite uncertain, so we prefer using a 
 * separate thread (which is simply discarded at the end of the test) rather
 * than interrupting JUnit and making thus room for possible side effects.
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
public class TestFutureInt
    extends TestCase
{

    private Future target;  //Object to test.
    private InterruptedException intExc;  //To transfer exc to the main thread.
    
    
    public void setUp()
    {
        target = new Future();  //Not in a legal state yet (two-step init).
        target.setCommand(
                new ExecCommand(new NullMultiStepTask(), 
                        new NullResultAssembler(), target,
                        new NullExecMonitor()));  //OK, init completed now.
    }
 
    public void testThreadInterruption()
    {
        //Create a task to call target.getResult().
        Runnable doGetResult = new Runnable() {
            public void run() {
                try {
                    target.getResult();
                } catch (InterruptedException ie) {
                    intExc = ie;
                } catch (ExecException ee) {}
            }
        };
        
        //Run the task in a new interrupted thread and wait for it to finish.
        ThreadSupport.runInNewInterruptedThread(doGetResult);
        
        //Now check that an InterruptedException was thrown.
        assertNotNull("A getResult shouldn't proceed if the thread is "+
                "interrupted.", intExc);
    }
    
}
