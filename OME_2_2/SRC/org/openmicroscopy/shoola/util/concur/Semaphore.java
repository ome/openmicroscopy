/*
 * org.openmicroscopy.shoola.util.concur.Semaphore
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
 * Emulates the functionality of a basic counting semaphore.
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
public class Semaphore 
{
    
	/** 
	 * Counts the permissions to proceed that are currently available.
	 * A non-positive count signifies that no permission is available.
	 * The count can be initialized to a negative value <code>n</code>,
	 * in which case a down blocks until <code>-n+1</code> up are made. 
	 * After the count has reached a non-negative value, it will never
	 * fall under zero again.
	 */
	private int		count;
	
	
    /**
     * Attempts to decrease the permissions {@link #count} by one and
     * optionally waits <code>timeout</code> milliseconds if not successful.
     * This method assumes the caller already owns the object's monitor &#151;
     * that is, call this method only within a <code>synchronized</code>
     * block.
     * 
     * @param timeout  The amount of milliseconds the calling thread should be
     *              suspended if the {@link #count} couldn't be decreased.
     *              Pass <code>0</code> for an unbounded wait or <code>-1</code>
     *              to not wait at all. 
     * @return  <code>true</code> if the {@link #count} was decreased, 
     *          <code>false</code> otherwise.
     * @throws InterruptedException If the current thread has been interrupted.
     */
    private boolean decreaseCount(long timeout)
        throws InterruptedException
    {  //We own the object's monitor.
        if (0 < count) {
            --count;
            return true;
        }
        if (0 <= timeout) {  //Don't wait if < 0.
            try {
                wait(timeout);  //Wait for unbounded time if 0.
            } catch (InterruptedException ie) {
                //Rely signal in the case interruption and notification happened
                //in the wrong order. (JVM makes no guarantees on which action
                //takes precedence if both notification and interruption happen
                //at about the same time when the thread is suspended.)
                notify();
                throw ie;
            }
        }
        return false;
    }
    
	/**
	 * Creates a new instance with the specified initial number of permisions
	 * to proceed.
	 * You can specify this initial number to be <code>1</code> in order to
	 * emulate a mutex.  A negative value <code>n</code> is also possible,
	 * in which case a {@link #down() down} blocks until <code>-n+1</code>
	 * {@link #up() up}'s are made.  After the count has reached a non-negative
	 * value, it will never fall under zero again.
	 * 
	 * @param initialCount	Initial number of permissions. 
	 */
	public Semaphore(int initialCount) 
	{
		count = initialCount;
	}
    
    /**
     * Atomically acquires a permission to proceed.
     * If no permission is currently available, the caller is suspended until
     * one is made available by some other thread calling the {@link #up() up}
     * method.
     * 
     * @throws InterruptedException If the current thread has been interrupted.
     * @see #down(long)
     */
    public void down()
        throws InterruptedException
    {
        //Don't acquire the lock if the current thread has been interrupted.
        //Pointless to have this thread compete to acquire the lock and then
        //possibly suspend when it should stop its activity instead.
        if (Thread.interrupted()) throw new InterruptedException();
        
        //Get the lock, but release it and suspend if not allowed to proceed.
        synchronized (this) {
            if (flowObs != null) flowObs.update(LOCK_ACQUIRED);
            while (!decreaseCount(0)) ;  //Wait until we can decrease count.
        }
    }
    
    /**
     * Atomically acquires a permission to proceed.
     * If no permission is currently available, the caller is suspended until
     * one is made available by some other thread calling the {@link #up() up}
     * method or until the specified <code>timeout</code> elapses.
     * 
     * @param timeout The amount of milliseconds the calling thread should be
     *              suspended if no permission can be acquired.  If the passed
     *              value is not positive, then the caller is not suspended
     *              at all. 
     * @return <code>true</code> if a permission was acquired, 
     *          <code>false</code> otherwise.
     * @throws InterruptedException If the current thread has been interrupted.
     * @see #down()
     */
    public boolean down(long timeout)
        throws InterruptedException
    {
        //Don't acquire the lock if the current thread has been interrupted.
        //Pointless to have this thread compete to acquire the lock and then
        //possibly suspend when it should stop its activity instead.
        if (Thread.interrupted()) throw new InterruptedException();
        
        //Get the lock.
        synchronized (this) {
            if (flowObs != null) flowObs.update(LOCK_ACQUIRED);
            if (decreaseCount(-1))  //Attempt to decrease without waiting. 
                return true;
            if (timeout <= 0)  //No wait.  We couldn't decrease so return false. 
                return false;
            
            //Release lock and suspend until allowed to proceed or timed-out. 
            long start = System.currentTimeMillis(), delta = timeout;
            while (true) {
                if (decreaseCount(delta)) return true;
                delta = (start+timeout) - System.currentTimeMillis();
                if (delta <= 0) return false;
            }
        }
    }
	
	/**
	 * Atomically releases a permission to proceed.
	 * If any thread is currently suspended in a {@link #down() down}, then 
     * on is awaken and allowed to proceed.
	 */
	public void up()
	{
		synchronized (this) {
            if (flowObs != null) flowObs.update(LOCK_ACQUIRED);
			++count;
			
			//As we added just one to the count, there can be at most one
			//waiting thread that may proceed, so it's pointless (and far
			//more expensive) to notify all threads in the wait set.
			notify();
		}
	}
    
    
/* 
 * ==============================================================
 *              Follows code to enable testing.
 * ==============================================================
 */  
    
    static final int LOCK_ACQUIRED = 1;
    
    private ControlFlowObserver flowObs;
    synchronized int getCount() { return count; }
    void register(ControlFlowObserver obs) { flowObs = obs; }
    
}
