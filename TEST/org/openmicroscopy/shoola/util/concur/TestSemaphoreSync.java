/*
 * org.openmicroscopy.shoola.util.concur.TestSemaphoreSync
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
 * Verifies the safety of {@link Semaphore}.
 * We test that <code>up</code> and <code>down</code> calls are serialized in
 * the presence of multiple threads acting on the object.
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
public class TestSemaphoreSync 
    extends TestCase
{
    
    private Semaphore       sem;  //Object to test.
    private int             semCount;  //To transfer value from alternate flow.
    private ThreadSupport   threads;  //To manage main/alt flows.
   
    
    public void setUp()
    {
        threads = new ThreadSupport(new Runnable() {  //Alternate flow.
            public void run() { semCount = sem.getCount(); }
        });
        //NOTE: getCount() is sync.  So if it is called while down/up is being
        //executed in the main thread, then the alternate thread will have
        //to wait until the lock is released.  This is enough to test safety,
        //there's no need to use more than two threads and verify all
        //possible combinations of down and up.
        
        sem = new Semaphore(1);
        sem.register(new ControlFlowObserver() {
            public void update(int checkPointID)
            {  //Called w/in main thread.
                if (checkPointID == Semaphore.LOCK_ACQUIRED) {
                    //down/up in progress.  Spawn alt thread to retrieve count.
                    threads.startAltFlow();  //sem.getCount() will be called.
                    
                    threads.pauseMainFlow();
                    //NOTE: Even though pausing the main flow doesn't guarantee
                    //that getCount() will be invoked in the mean time, the
                    //2-second delay used should in practice make this extremely
                    //likely to happen.
                }
            }
        });
        
        //NOTE: If getCount() gets executed b/f down/up modifies the counter,
        //then we would also get the wrong value for semCount.  In this case,
        //we can deduce that locks were screwed up and thus fail the test.
        //The test would be optimal if we could make sure that getCount() is
        //actually invoked while down/up is being executed (in practice this is
        //quite sure though).  In any case, pausing the main thread is the best
        //we can do as there's no way to tell whether a thread is waiting to
        //acquire a lock.
    }
    
    public void testDown()
        throws InterruptedException
    {
        sem.down();
        threads.awaitAltFlow();
        assertEquals("Concurrent access to count.", 0, semCount);
    }
    
    public void testDownTimeout()
        throws InterruptedException
    {
        boolean decreased = sem.down(5000);  //Timeout ignored b/c count is 1.
        threads.awaitAltFlow();
        assertTrue("Down should have returned true.", decreased);
        assertEquals("Concurrent access to count.", 0, semCount);
    }
    
    public void testUp()
    {
        sem.up();
        threads.awaitAltFlow();
        assertEquals("Concurrent access to count.", 2, semCount);
    }
    
}
