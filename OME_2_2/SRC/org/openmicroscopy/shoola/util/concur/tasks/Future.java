/*
 * org.openmicroscopy.shoola.util.concur.tasks.Future
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

import org.openmicroscopy.shoola.util.concur.ControlFlowObserver;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Extends {@link ExecHandle} to allow clients to retrieve the result of a
 * computation peformed by a service.
 * After triggering the asynchronous execution of a service that computes a
 * result, the invoker (client) is given back a <code>Future</code> object.
 * The client calls one of the <code>getResult</code> methods to retrieve the
 * computed result, possibly waiting until the computation is finished or until
 * a given timeout elapses.
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
public class Future
    extends ExecHandle
{

    /** State flag. */
    static final int    WAITING_FOR_RESULT = 0; 
    
    /** State flag. */
    static final int    HAS_RESULT = 1;
    
    /** State flag. */
    static final int    HAS_EXCEPTION = 2;
    
    //NOTE: the above state flags have package visibility to enable testing.
    
    
    /** The result of the computation, if any. */
    private Object          result;
    
    /** Wraps any exception occurred during the computation. */
    private ExecException   failureWrapper;
    
    /** Keeps track of the current state.*/
    private int             state;
    
    
    /**
     * Sets the result or makes a new {@link ExecException} to wrap the service
     * exception.
     * Also wakes up any thread blocked on one of the <code>getResult</code>
     * methods.
     * At least one of the passed arguments must be <code>null</code>.
     * 
     * @param result The result of the computation.
     * @param t Any exception encountered by the service.
     */
    private void set(Object result, Throwable t)
    {
        if (state != WAITING_FOR_RESULT) 
            throw new Error("Can't reuse a future.");
        this.result = result;
        if (t != null) failureWrapper = new ExecException(t);
        notifyAll();
    }
    
    /**
     * Creates a new instance.
     * Recall that this class extends {@link ExecHandle}, so the state is
     * invalid until you call {@link ExecHandle#setCommand(ExecCommand)}.
     */
    Future()
    {
        result = null;
        failureWrapper = null;
        state = WAITING_FOR_RESULT;
    }
    
    /**
     * Stores the result of the service.
     * Can only be invoked once and only if {@link #setException(Throwable)}
     * hasn't already been invoked.
     * 
     * @param r The service result.
     */
    synchronized void setResult(Object r) 
    { 
        if (flowObs != null) flowObs.update(LOCK_ACQUIRED);
        set(r, null);
        state = HAS_RESULT;
    }
    
    /**
     * Stores any exception occurred during the service execution.
     * Can only be invoked once and only if {@link #setResult(Object)}
     * hasn't already been invoked.
     * 
     * @param t Any exception occurred during the service execution.  Mustn't
     *          be <code>null</code>.
     */
    synchronized void setException(Throwable t) 
    { 
        if (t == null)  //See NOTE below.
            t = new Error("Future.setException(null) is not a valid call."+
                    "(Bug in the execution workflow code.)");
        if (flowObs != null) flowObs.update(LOCK_ACQUIRED);
        set(null, t); 
        state = HAS_EXCEPTION;
    }
    
/* NOTE: This can’t happen if the execution workflow is correctly implemented; 
 * in fact, setException() is only called after catching an exception and 
 * passing along that exception.  However, in the case t is null, this is the 
 * best strategy: if a client is blocked on getResult(), it will be woken up 
 * and no deadlock is possible.  The alternative would be to throw a 
 * NullPointerException which is likely to percolate all the way up in the 
 * executor thread’s call stack and eventually appear on stderr.  In this case, 
 * no call to setException() (or setResult() for that matter) would follow and 
 * the state would still be WAITING_FOR_RESULT, which could result in a 
 * deadlock.  
 */    
    
    /**
     * Retrieves the result computed by the service.
     * This method blocks until the computation is finished.  If the service
     * executed normally then the result of the computation is returned.
     * Otherwise this method throws an <code>ExecException</code> that wraps
     * the original exception which caused the service to terminate abnormally.
     * 
     * @return The result computed by the service.
     * @throws ExecException Wraps any exception encountered by the service.
     * @throws InterruptedException If you interrupt a thread blocked on this
     *                              method.  Bear in mind that this has nothing
     *                              to do with service cancellation.
     */
    public Object getResult()
        throws ExecException, InterruptedException
    {
        synchronized (this) {
            if (flowObs != null) flowObs.update(LOCK_ACQUIRED);
            while (state == WAITING_FOR_RESULT) wait();
            if (state == HAS_EXCEPTION) throw failureWrapper;
            return result;
        }
    }
    
    /**
     * Not implemented yet.
     * @param msec
     * @return
     * @throws ExecException
     * @throws InterruptedException
     */
    public Object getResult(long msec)
        throws ExecException, InterruptedException
    {
        if (true) throw new NoSuchMethodError();

        //TODO: implement.  The following code is just to avoid compilers
        //warnings.
        if (msec == 0) msec = 0;
        if (false) throw new ExecException(null);
        if (false) throw new InterruptedException();
        return null;
    }
    
    
/* 
 * ==============================================================
 *              Follows code to enable testing.
 * ==============================================================
 */
    
    static final int LOCK_ACQUIRED = 100;
    
    private ControlFlowObserver flowObs;
    void register(ControlFlowObserver obs) { flowObs = obs; }
    synchronized int getState() { return state; }
    
}
