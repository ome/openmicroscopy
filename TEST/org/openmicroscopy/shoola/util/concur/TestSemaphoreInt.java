/*
 * org.openmicroscopy.shoola.util.concur.TestSemaphoreInt
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
 * Verifies that {@link Semaphore} correctly aborts in the case of interruption.
 * Each test spawns an ad-hoc thread which is interrupted to verify how 
 * {@link Semaphore} reacts to interruption.  The result of the test is passed
 * back into the main flow (JUnit) so to perform the needed assertions.  
 * Note that we never interrupt JUnit.  Spawning another thread might seem 
 * unecessary when we could just interrupt JUnit and then clear the interrupted
 * status after the test.  However implementation characteristics of 
 * interruption-based methods are quite uncertain, so we prefer using a 
 * separate thread (which is simply discarded at the end of the test) rather
 * than interrupting JUnit and making thus room for possible side effects.
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
public class TestSemaphoreInt
    extends TestCase
{

    private Semaphore       sem;  //Object to test.

    //Task performed in a separate thread, which is interrupted.
    private class DoDown implements Runnable
    {
        InterruptedException intExc;  //To transfer exc to the main thread.
        boolean callTimedDown;  //Tells which down() to call in run().
        
        DoDown(boolean callTimedDown) { this.callTimedDown = callTimedDown; }
        public void run() {
            try {
                if (callTimedDown) sem.down(2000);
                else sem.down();
            } catch (InterruptedException ie) {
                intExc = ie;
            }
        }
    }
    
    
    public void setUp()
    {
        sem = new Semaphore(-1);
        sem.register(new ControlFlowObserver() {
            public void update(int checkPointID)
            {  //Called w/in ad-hoc thread, see below.
                if (checkPointID == Semaphore.LOCK_ACQUIRED)
                    Thread.currentThread().interrupt();
            }
        });
    }
    
    public void testThreadInterruption()
    {
        //Create a task to call sem.down().
        DoDown doDown = new DoDown(false);
        
        //Run the task in a new interrupted thread and wait for it to finish
        //-- so its working memory will be flushed. 
        ThreadSupport.runInNewInterruptedThread(doDown);
        
        //Now check that an InterruptedException was thrown.
        assertNotNull("A down shouldn't proceed if the thread is interrupted.", 
                doDown.intExc);
    }
    
    public void testThreadInterruption2()
    {
        //Create a task to call sem.down(2000).
        DoDown doDown = new DoDown(true);  
        
        //Run the task in a new interrupted thread and wait for it to finish
        //-- so its working memory will be flushed. 
        ThreadSupport.runInNewInterruptedThread(doDown);
        
        //Now check that an InterruptedException was thrown.
        assertNotNull("A down shouldn't proceed if the thread is interrupted.", 
                doDown.intExc);
    }
    
    public void testThreadInterruptionWhileWaiting()
    {
        //Create a task to call sem.down().
        DoDown doDown = new DoDown(false);  
        
        //Run the task in a new thread which will be interrupted just after
        //sem's lock is acquired (see setUp) and wait for it to finish --
        //so its working memory will be flushed. 
        ThreadSupport.runInNewThread(doDown);
        
        //Now check that an InterruptedException was thrown.
        assertNotNull("Down shouldn't have proceeded on a negative count.", 
                doDown.intExc);
    }
    
    public void testThreadInterruptionWhileWaiting2()
    {
        //Create a task to call sem.down(2000).
        DoDown doDown = new DoDown(true);  
        
        //Run the task in a new thread which will be interrupted just after
        //sem's lock is acquired (see setUp) and wait for it to finish --
        //so its working memory will be flushed.
        ThreadSupport.runInNewThread(doDown);
        
        //Now check that an InterruptedException was thrown.
        assertNotNull("Down shouldn't have proceeded on a negative count.", 
                doDown.intExc);
    }
    
}
