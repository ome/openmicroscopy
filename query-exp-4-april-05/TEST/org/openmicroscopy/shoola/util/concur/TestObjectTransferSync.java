/*
 * org.openmicroscopy.shoola.util.concur.TestObjectTransferSync
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
 * Verifies the safety of {@link ObjectTransfer}.
 * Makes sure that state transitions are correctly serialized.
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
public class TestObjectTransferSync
    extends TestCase
{

    private ObjectTransfer  target;  //Object under test.
    
    private ThreadSupport   collect_1,  //Alt flow to do a collect().
                            collect_2,  //Another collect().
                            handOff_2;  //An handOff() - other one in main flow.
    
    private class CheckPoint {
        long transitionTimestamp;  //Taken by observer.
        Object objInTransit;  //Grabbed by observer.
        Object handOffObj;  //Passed to handOff.  Null for collect threads.
        Object collectObj;  //Returned by collect.  Null for hand-off threads.
        Throwable exc;  //To transfer any unexpected error to main flow.
    }
    //4 check points: 
    // Empty->Full transition caused by first handOff() in main flow.
    // Full->Empty transition caused by collect() collect_1 alt flow.
    // Empty->Full transition caused by handOff() in handOff_2 alt flow.
    // Full->Empty transition caused by collect() in collect_2 alt flow.
    private CheckPoint  handOff_1_cp, collect_1_cp, handOff_2_cp, collect_2_cp;
    
    //Will be handOff_2_cp for handOff_2 alt flow, collect_1_cp for collect_1 
    //alt flow, and collect_2_cp for collect_2 alt flow.
    private static ThreadLocal  checkPoint; 
    
    //Called within each thread from observer.update().
    private void takeTimestampAndObjInTransit()
    {
        CheckPoint cp = (CheckPoint) checkPoint.get();
        cp.transitionTimestamp = System.currentTimeMillis();
        cp.objInTransit = target.getObjInTransit();
    }
    
    private class DoHandOff implements Runnable {
        CheckPoint cp;
        DoHandOff(CheckPoint cp) { this.cp = cp; }
        public void run() {
            try {
                checkPoint.set(cp);  //Now it's visible from within update().
                target.handOff(cp.handOffObj); 
            } catch (Throwable interruptedException) {
                //Should never happen, as we don't interrupt.  Moreover, we're
                //not expecting anything different from IE.  But just in case:
                cp.exc = interruptedException;
            } 
        }
    }
    private class DoCollect implements Runnable {
        CheckPoint cp;
        DoCollect(CheckPoint cp) { this.cp = cp; }
        public void run() {
            try {
                checkPoint.set(cp);  //Now it's visible from within update().
                cp.collectObj = target.collect();
            } catch (Throwable interruptedException) {
                //Should never happen, as we don't interrupt.  Moreover, we're
                //not expecting anything different from IE.  But just in case:
                cp.exc = interruptedException;
            } 
        }
    }
    
    public void setUp()
    {
        //Create new target.
        target = new ObjectTransfer();
        
        //Create check points.
        handOff_1_cp = new CheckPoint();
        handOff_1_cp.handOffObj = new Object();
        collect_1_cp = new CheckPoint();
        handOff_2_cp = new CheckPoint();
        handOff_2_cp.handOffObj = new Object();
        collect_2_cp = new CheckPoint();
        
        //Create the thread local var to hold the check point for each thread.
        checkPoint = new ThreadLocal();
        
        //Link handOff_1_cp to main flow.
        checkPoint.set(handOff_1_cp);
        
        //Create alternate flows and links them to their check point.
        collect_1 = new ThreadSupport(new DoCollect(collect_1_cp));
        collect_2 = new ThreadSupport(new DoCollect(collect_2_cp));
        handOff_2 = new ThreadSupport(new DoHandOff(handOff_2_cp));
        
        //Create observer.
        target.register(new ControlFlowObserver() {
            public void update(int checkPointID) {
                if (collect_1.isMainFlow()) {
                    //Start alternate flows.
                    collect_1.startAltFlow();
                    collect_2.startAltFlow();
                    handOff_2.startAltFlow();
                    
                    //Pause to make (almost) sure we get concurrent access.
                    collect_1.pauseMainFlow();
                } 
                takeTimestampAndObjInTransit();
            }
        });
    }
    
    public void test()
        throws InterruptedException
    {
        //Do the first hand-off.  This will spawn the alternate threads.
        target.handOff(handOff_1_cp.handOffObj);
        
        //Wait for all threads to die and their working memory to be flushed.
        collect_1.awaitAltFlow();
        collect_2.awaitAltFlow();
        handOff_2.awaitAltFlow();
        
        //Now order the check point objects.  If transitions were serialized
        //correctly, then the order is either
        //  handOff_1_cp -> collect_1_cp -> handOff_2_cp -> collect_2_cp
        //or
        //  handOff_1_cp -> collect_2_cp -> handOff_2_cp -> collect_1_cp
        //(We don't know which collect happened first.)
        CheckPoint[] cps = new CheckPoint[] {handOff_1_cp, collect_1_cp,
                                                handOff_2_cp, collect_2_cp};
        if (collect_2_cp.transitionTimestamp < 
                collect_1_cp.transitionTimestamp) {
            cps[1] = collect_2_cp;
            cps[3] = collect_1_cp;
        }
        
        //Test.
        assertTrue("First Empty->Full transition wasn't correctly serialized.", 
                cps[0].transitionTimestamp <= cps[1].transitionTimestamp);
        assertNull("First Empty->Full transition wasn't correctly serialized.", 
                cps[0].objInTransit);
        
        assertTrue("First Full->Empty transition wasn't correctly serialized.", 
                cps[1].transitionTimestamp <= cps[2].transitionTimestamp);
        assertNotNull("First Full->Empty transition wasn't correctly " +
                "serialized.", cps[1].objInTransit);
        assertSame("First Full->Empty transition wasn't correctly serialized.", 
                cps[0].handOffObj, cps[1].collectObj);
        
        assertTrue("Second Empty->Full transition wasn't correctly serialized.", 
                cps[2].transitionTimestamp <= cps[3].transitionTimestamp);
        assertNull("Second Empty->Full transition wasn't correctly serialized.", 
                cps[2].objInTransit);
        
        assertNotNull("Second Full->Empty transition wasn't correctly " +
                "serialized.", cps[3].objInTransit);
        assertSame("Second Full->Empty transition wasn't correctly serialized.", 
                cps[2].handOffObj, cps[3].collectObj);
    }
    /* NOTE: correct serialization => what asserted above
     * However, <= is not, in general, true -- it could happen b/c of a lucky
     * combination of events, but timeouts we chose make that combination quite
     * unlikely.
     */
    
}
