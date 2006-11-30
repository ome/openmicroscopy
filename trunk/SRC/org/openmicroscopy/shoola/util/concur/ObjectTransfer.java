/*
 * org.openmicroscopy.shoola.util.concur.ObjectTransfer
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

//Application-internal dependencies

/** 
 * Synchronously transfers an object from one thread to another.
 * A thread that {@link #handOff(Object) hands} an object off has to wait until
 * another thread {@link #collect() collects} it, and vice-versa.
 * This class can be considered a synchronous channel &#151; basically a 
 * degenerated synchronous bounded buffer with no internal capacity.
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
public class ObjectTransfer
{
/* IMPLEMENTATION NOTE: The implementation would be much easier if we used the
 * object's monitor instead of 3 semaphores.  However, giving each condition
 * (that a thread can await) a separate monitor leads to big savings in terms
 * of notifications -- also semaphores use notify() instead of notifyAll(), so
 * no extra notifications have to be sent. 
 */
    
    /** Holds the object while in transit. */
    private Object  objInTransit;
    
    /** 
     * Represents the Empty state.
     * This state is defined by the following constraints:
     * <code>empty: 1, full: 0, objInTransit: null</code>. 
     */
    private final Semaphore   empty;
    
    /** 
     * Represents the Full state. 
     * This state is defined by the following constraints:
     * <code>empty: 0, full: 1, objInTransit: !null</code>.
     */
    private final Semaphore   full;
    
    /**
     * Blocks a thread handing an object off until another thread collects it.
     */
    private final Semaphore   objTransferred;
    
    
    /**
     * Creates a new instance.
     */
    public ObjectTransfer()
    {
        //Set the Empty state.
        empty = new Semaphore(1);
        full = new Semaphore(0);
        objInTransit = null;
        
        //No hand-off can complete until collection is done.
        objTransferred = new Semaphore(0);
    }
    
    /**
     * Waits until the object in transit has been collected.
     * If the calling thread (producer) is interrupted, then we just record 
     * this event and we roll forward to guarantee that the producer eventually 
     * rendezvous with the thread that will collect the object (consumer).  If 
     * we din't do this, the signal sent by the consumer thread would be picked
     * by the next producer that does an hand-off &#151; assuming the caller was
     * interrupted before the {@link #collect() collect} method returns in the 
     * consumer thread.  In this case, the next hand-off is likely to proceed
     * before its object is actually collected. 
     * 
     * Only call this method after the hand-off procedure has completed.
     * 
     * @throws InterruptedException If the calling thread was interrupted.
     */
    private void waitForObjectToBeTransferred()
        throws InterruptedException
    {
        InterruptedException exc = null;
        while (true) {
            try {
                objTransferred.down();  //See NOTE below.
                break;
            } catch (InterruptedException ie) {  //Roll forward.
                exc = ie;
            }
        }
        if (exc != null) throw exc;  //Re-throw if needed.
    }
    /* NOTE: Here we rely on the fact that the interrupted status is cleared
     * upon throwing an InterruptedException.  If Semaphore didn't do that,
     * then we would end up in an infinite loop in the case of interruption.
     */
    
    /**
     * Convenience method to collapse together the implementations of the
     * hand-off methods.
     * 
     * @param x The object to transfer.
     * @param timeout <code>-1</code> for unbounded wait, <code>0</code> for
     *                  no wait, a positive value for timed wait.
     * @return <code>true</code> if <code>x</code> was delivered to a consumer
     *          thread, <code>false</code> if <code>x</code> couldn't be put
     *          into the inner channel for delivery.
     * @throws InterruptedException If the calling thread was interrupted.  
     *          Note that even if the caller is interrupted, we guarantee that
     *          it will eventually rendezvous with a consumer thread.
     */
    private boolean doHandOff(Object x, long timeout)
        throws InterruptedException
    {
        //Never accept null.  This way the timed version of collect can 
        //return null to mean that the timeout elapsed and no object is
        //available.  If we accepted null here, then returning null from
        //collect would be ambiguos.
        if (x == null) throw new NullPointerException("No object.");
        
        //Timed/Unbouded/No wait until we can exit the Empty state.  In order
        //to start a transition out of the Empty state, the object must be in
        //that state.  So if the state is Full, we wait or return false --
        //depending on the value of timeout.
        if (timeout == -1)  //-1 means unbounded wait. 
            empty.down();
        else  //Timed wait if timeout > 0 or no wait if 0.
            if (!empty.down(timeout)) return false;
        
        if (flowObs != null) flowObs.update(TRANSITION_TO_FULL); //Enable tests.
        objInTransit = x;
        full.up();  //Notify we've entered the Full state.
        
        //Now wait until the object has been transferred -- that is, until
        //the collect method returns.
        waitForObjectToBeTransferred();
        
        return true;
    }
    
    /**
     * Convenience method to collapse together the implementations of the
     * collect methods.
     * 
     * @param timeout <code>-1</code> for unbounded wait, <code>0</code> for
     *                  no wait, a positive value for timed wait.
     * @return An object that was handed-off by a producer or <code>null</code>
     *          if no object was collected.
     * @throws InterruptedException If the caller was interrupted.
     */
    private Object doCollect(long timeout)
        throws InterruptedException
    {
        //Timed/Unbouded/No wait until we can exit the Full state.  In order
        //to start a transition out of the Full state, the object must be in
        //that state.  So if the state is Empty, we wait or return null --
        //depending on the value of timeout.
        if (timeout == -1)  //-1 means unbounded wait. 
            full.down();
        else  //Timed wait if timeout > 0 or no wait if 0.
            if (!full.down(timeout)) return null;
        
        if (flowObs != null) flowObs.update(TRANSITION_TO_EMPTY);//Enable tests.
        Object x = objInTransit;  //Collect the object.
        objInTransit = null;
        
        empty.up();  //Notify we've entered the Empty state.
        objTransferred.up();  //Notify we've collected the object.
        
        return x;
    }
    
    /**
     * Attempts to transfer <code>x</code> to a consumer thread, waiting at most
     * <code>timeout</code> milliseconds.
     * This method does two things.  First, it tries to put <code>x</code> into
     * the inner delivery channel for later collection by a consumer thread.  As
     * only one object at a time can ever be in transit, concurrent hand-offs
     * and collections will have to comptete to gain exclusive access to the
     * channel.  This method will wait at most <code>timeout</code> milliseconds
     * to access the channel and then give up if it couldn't succeed.  If it
     * does succeed, then it waits until a consumer thread will collect the 
     * passed object.  Note that this second step is an unbounded wait, so you
     * have to be sure that a consumer will eventually collect the object or
     * the calling thread will block forever.
     * 
     * @param x The object to transfer.  Mustn't be <code>null</code>.
     * @param timeout The amount of milliseconds to wait for the channel to be
     *                  free.  A non positive value means no wait at all.
     * @return  <code>true</code> if <code>x</code> was delivered to a consumer
     *          thread, <code>false</code> if <code>x</code> couldn't be put
     *          into the inner channel for delivery. 
     * @throws InterruptedException If the calling thread was interrupted.  
     *          Note that even if the caller is interrupted, we guarantee that
     *          it will eventually rendezvous with a consumer thread.  That is,
     *          the second step detailed above is performed regardless of 
     *          interruption.
     * @see #handOff(Object)
     * @see #collect()
     * @see #collect(long)
     */
    public boolean handOff(Object x, long timeout)
        throws InterruptedException
    {
        if (timeout <= 0)  //Caller meant no wait. 
            timeout = 0;  //-1 is reserved for unbounded waits.
        return doHandOff(x, timeout);
    }
    
    /**
     * Transfers <code>x</code> to a consumer thread.
     * This method does two things.  First, it waits until it can put 
     * <code>x</code> into the inner delivery channel for later collection by a
     * consumer thread.  Then it waits until a consumer thread will collect the 
     * passed object.  As only one object at a time can ever be in transit, you 
     * have to be sure that there are consumer threads that will eventually
     * remove any object already in transit and then collect the object that
     * you're handing off.  Otherwise, the calling thread will block forever 
     * &#151; in fact, the two steps just described are unbounded waits.
     * 
     * @param x The object to transfer.  Mustn't be <code>null</code>.
     * @throws InterruptedException If the calling thread was interrupted.  
     *          Note that even if the caller is interrupted, we guarantee that
     *          it will eventually rendezvous with a consumer thread.  That is,
     *          the second step detailed above is performed regardless of 
     *          interruption.
     * @see #handOff(Object, long)
     * @see #collect()
     * @see #collect(long)
     */
    public void handOff(Object x)
        throws InterruptedException
    {
        doHandOff(x, -1);  //-1 means unbounded wait.
    }
 
    /**
     * Collects an object that was handed off by a producer thread.
     * This method blocks until an object is available, so you have to be sure
     * that there are producer threads around before you invoke this method.
     * Otherwise, the caller might block forever &#151; however, you can still
     * interrupt the thread.
     * 
     * @return  An object that was handed-off by a producer.
     * @throws InterruptedException If the caller was interrupted.
     * @see #collect(long)
     * @see #handOff(Object)
     * @see #handOff(Object, long)
     */
    public Object collect()
        throws InterruptedException
    {
        return doCollect(-1);  //-1 means unbounded wait.
    }
    
    /**
     * Collects an object that was handed off by a producer thread.
     * This method blocks until an object is available or <code>timeout</code>
     * elapses.
     * 
     * @param timeout The amount of milliseconds to wait for an object to be
     *                  handed off by a producer.  A non positive value means
     *                  no wait at all.
     * @return  An object that was handed-off by a producer or <code>null</code>
     *          if no object was collected.
     * @throws InterruptedException If the caller was interrupted.
     * @see #collect()
     * @see #handOff(Object)
     * @see #handOff(Object, long)
     */
    public Object collect(long timeout)
        throws InterruptedException
    {
        if (timeout <= 0)  //Caller meant no wait. 
            timeout = 0;  //-1 is reserved for unbounded waits.
        return doCollect(timeout);
    }
    
    
/* 
 * ==============================================================
 *              Follows code to enable testing.
 * ==============================================================
 */  
        
        static final int TRANSITION_TO_EMPTY = 1;
        static final int TRANSITION_TO_FULL = 2;
        
        private ControlFlowObserver flowObs;
        void register(ControlFlowObserver obs) { flowObs = obs; }
        Object getObjInTransit() { return objInTransit; }
    
}
