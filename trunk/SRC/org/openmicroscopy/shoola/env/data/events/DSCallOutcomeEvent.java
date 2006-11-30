/*
 * org.openmicroscopy.shoola.env.data.events.DSCallOutcomeEvent
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

package org.openmicroscopy.shoola.env.data.events;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.StateChangeEvent;

/** 
 * Notfies of the outcome of an asynchronous call to the data services.
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
public class DSCallOutcomeEvent
    extends StateChangeEvent
{
    
    /**
     * Outcome state flag to denote that the call was cancelled.
     * In this state both the {@link #getResult() getResult} and
     * {@link #getException() getException} methods will return
     * <code>null</code>. 
     */
    public static final int     CANCELLED = 0;
    
    /**
     * Outcome state flag to denote that the call raised an exception.
     * In this state the {@link #getException() getException} method 
     * will return the exception that was thrown.
     * The {@link #getResult() getResult} method will obviously return
     * <code>null</code>. 
     */
    public static final int     ERROR = 1;
    
    /**
     * Outcome state flag to denote that the call returned with a 
     * <code>null</code> result.
     * This can happen if the operation itself returns no value or
     * it does have a return value, but this specific invocation 
     * returned <code>null</code>.
     * In this state both the {@link #getResult() getResult} and
     * {@link #getException() getException} methods will return
     * <code>null</code>.
     */
    public static final int     NO_RESULT = 2;
    
    /**
     * Outcome state flag to denote that the call returned a result.
     * In this state the {@link #getResult() getResult} method will
     * return the produced result (which is never <code>null</code>
     * in this case).  The {@link #getException() getException} method 
     * method will obviously return <code>null</code>.
     */
    public static final int     HAS_RESULT = 3;
    
    
    /** Keeps track of the state. */
    private final int       state;
    
    /** The result (if any) of the call. */
    private final Object    result;
    
    /** Any exception raised by the call. */
    private final Throwable exception;

    
    /**
     * Constructor used in the case of cancellation.
     */
    public DSCallOutcomeEvent()
    {
        result = null;
        exception = null;
        state = CANCELLED;
        setStateChange(this);
    }
    
    /**
     * Constructor used in the case the call terminates regularly.
     * 
     * @param result The result of the call.  Pass <code>null</code> if the
     *                  call returns no results.
     */
    public DSCallOutcomeEvent(Object result)
    {
        exception = null;
        this.result = result;
        state = (result == null) ? NO_RESULT : HAS_RESULT;
        setStateChange(this);
    }
    
    /**
     * Constructor used in the case the call raises an exception.
     * 
     * @param exception     The raised error.  Mustn't be <code>null</code>.
     */
    public DSCallOutcomeEvent(Throwable exception)
    {
        if (exception == null)
            throw new NullPointerException("Must specify a Throwable.");
        this.exception = exception;
        result = null;
        state = ERROR;
        setStateChange(this);
    }
    
    /**
     * Returns the outcome state.
     * 
     * @return One of the static state flags defined by this class.
     */
    public int getState() { return state; }
    
    /**
     * Tells whether the call was cancelled.
     * 
     * @return <code>true</code> if cancelled, <code>false</code> otherwise.
     * @see #CANCELLED
     */
    public boolean wasCancelled() { return (state == CANCELLED); }
    
    /**
     * Tells whether an exeption was raised during the call.
     * 
     * @return <code>true</code> if an exeption was raised, <code>false</code>
     *          otherwise.
     * @see #ERROR
     */
    public boolean hasException() { return (state == ERROR); }
    
    /**
     * Tells whether the call produced a result.
     * If this method returns <code>true</code>, then the call completed
     * normally and produced a non-<code>null</code> result, which can be
     * retrieved through the {@link #getResult() getResult} method.  If
     * <code>false</code> is returned instead, then no result is available
     * and calling {@link #getResult()} would return <code>null</code>.
     * This can happen because one of the following:
     * <ul>
     *  <li>The call was cancelled.</li>
     *  <li>The call raised an exception.</li>
     *  <li>The operation itself returns no value or it does have a return
     *   value, but this specific invocation returned <code>null</code>.</li>
     * </ul>
     * 
     * @return <code>true</code> if there's a return value, <code>false</code>
     *          otherwise.
     * @see #HAS_RESULT
     * @see #NO_RESULT
     * @see #ERROR
     * @see #CANCELLED
     */
    public boolean hasResult() { return (state == HAS_RESULT); }
    
    /**
     * Returns any exception raised during the call.
     * Will always return <code>null</code> if the call was 
     * {@link #CANCELLED cancelled} or terminated regularly.
     * 
     * @return The exception that was thrown.  It's a non-<code>null</code>
     *          reference that you'll have to cast to a more suitable type as
     *          documented by the call.  However, keep in mind that other 
     *          exceptions are possible besides those documented by the call.
     *          In fact, <i>any</i> exception occurred during the computation
     *          will be caught and eventually packed in this object. 
     * @see #hasException()
     */
    public Throwable getException() { return exception; }
    
    /**
     * Returns the result (if any) of the call.
     * This method will always return <code>null</code> if the call was
     * {@link #wasCancelled() cancelled} or an exception
     * {@link #hasException() raised}.  However, even if the call returned
     * normally, a <code>null</code> value is still possible if the call
     * produces no results, or the actual result returned by the call is 
     * <code>null</code>.
     * 
     * @return The result of the call.  It's a non-<code>null</code> reference
     *          that you'll have to cast to a more suitable type as documented
     *          by the call.
     */
    public Object getResult() { return result; }
    
}
