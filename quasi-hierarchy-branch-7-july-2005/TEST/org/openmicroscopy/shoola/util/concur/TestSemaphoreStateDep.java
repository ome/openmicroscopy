/*
 * org.openmicroscopy.shoola.util.concur.TestSemaphoreStateDep
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
 * Verifies the the state-dependence constructs in {@link Semaphore}.
 * A <code>down</code> call can only be executed when the count is positive.
 * Makes sure liveness is attained.
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
public class TestSemaphoreStateDep
    extends TestCase
{
    
    private Semaphore       sem;  //Object to test.
    private int             semCount;  //To transfer value from alternate flow.
    private ThreadSupport   threads;  //To manage main/alt flows.
    
    
    public void setUp()
    {
        sem = new Semaphore(-1);
        
        threads = new ThreadSupport(new Runnable() {  //Alternate flow.
            public void run() {  //Fire async up's.
                sem.up();
                semCount = sem.getCount();
                sem.up();
            }
        });
        
        sem.register(new ControlFlowObserver() {
            public void update(int checkPointID)
            {
                //If main thread acquired the lock during down below, then spawn
                //a thread to do two ups and to get count value in between ups.
                if (checkPointID == Semaphore.LOCK_ACQUIRED &&
                        threads.isMainFlow())
                    threads.startAltFlow();  //Fires async ups.
                
                //NOTE: The first up can't proceed until down in the main thread
                //releases the lock when entering wait.  Then, it's possible
                //that the main thread is resumed b/f getCount is called.  
                //However, the main thread should be suspended again as the
                //count is 0 after the first up.
            }
        });
    }
    
    public void testDownAcceptance() 
        throws InterruptedException
    {
        sem.down();
        threads.awaitAltFlow();
        assertEquals("Down shouldn't have proceeded on a zero count.", 
                0, semCount);
    }
    
    public void testDownAcceptance2() 
        throws InterruptedException
    {
        sem.down(10000);
        threads.awaitAltFlow();
        assertEquals("Down shouldn't have proceeded on a zero count.", 
                0, semCount);
    }
    
    public void testDownAcceptance3() 
        throws InterruptedException
    {
        sem.register(null);  //Disable, see setUp.
        String NEG_CNT = "Down shouldn't proceed on a negative count.";
        assertFalse(NEG_CNT, sem.down(-1));
        assertFalse(NEG_CNT, sem.down(0));
        assertFalse(NEG_CNT, sem.down(2000));
    }

}
