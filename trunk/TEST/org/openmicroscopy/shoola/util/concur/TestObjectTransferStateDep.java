/*
 * org.openmicroscopy.shoola.util.concur.TestObjectTransferStateDep
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
 * Verifies the the state-dependence constructs in {@link ObjectTransfer}.
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
public class TestObjectTransferStateDep
    extends TestCase
{

    private ObjectTransfer  target;  //Object under test.
    
    public void setUp()
    {
        target = new ObjectTransfer();
    }
    
    public void testCollectWhenEmpty_1()
    {
        final Throwable[] exc = new Throwable[1];
        Runnable doCollect = new Runnable() {
            public void run() {
                try {
                    target.collect();
                } catch (Throwable t) {
                    exc[0] = t;
                }
            }
        }; 
        ThreadSupport ts = new ThreadSupport(doCollect);
        ts.startAltFlow();
        ts.pauseMainFlow();  
        //The above makes it likely that alt flow is suspended on a wait().
        //This is when we want to interrupt.
        ts.interruptAltFlow();
        ts.awaitAltFlow();
        assertNotNull("collect() shouldn't have proceeded on an Empty state.", 
                exc[0]);
        assertTrue("collect() shouldn't have proceeded on an Empty state.", 
                exc[0] instanceof InterruptedException);
    }
    
    public void testCollectWhenEmpty_2()
        throws InterruptedException
    {
        assertNull("collect(100) should return null after 100ms if the state"+
                " is Empty.", target.collect(100));
        assertNull("collect(0) should return null straightaway if the state"+
                " is Empty.", target.collect(0));
    }
    
    public void testHandOffWhenFull()
        throws InterruptedException
    { 
        Runnable doCollect = new Runnable() {
            public void run() {
                try {
                    target.collect();
                } catch (InterruptedException ie) {
                    //Should never happen; don't bother b/c this would show up
                    //as a deadlock.
                }
            }
        }; 
        final ThreadSupport ts = new ThreadSupport(doCollect);
        final boolean[] handedOff = new boolean[] { true };
        target.register(new ControlFlowObserver() {
            public void update(int checkPointID) {
                if (checkPointID == ObjectTransfer.TRANSITION_TO_FULL) {
                    ts.startAltFlow();  //Calls collect().
                } else {  //We're in the collect() done by the alt flow.
                    try {
                        handedOff[0] = target.handOff(new Object(), 100);
                    } catch (InterruptedException ie) {
                        //Should never happen; don't bother b/c handedOff[0]
                        //will be true and the test fail anyway.
                    }
                }
            }
        });
        target.handOff(new Object());
        ts.awaitAltFlow();
        
        assertFalse("Timed hand-off was supposed to return false after timeout"+
                " because the state was Full.", handedOff[0]);
    }
    
}
