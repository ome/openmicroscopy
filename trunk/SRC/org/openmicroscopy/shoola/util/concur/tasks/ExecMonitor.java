/*
 * org.openmicroscopy.shoola.util.concur.tasks.ExecutionMonitor
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

//Application-internal dependencies

/** 
 * Defines the callbacks that the {@link CmdProcessor} uses to notify observers
 * about the state of a given computation.
 * The {@link CmdProcessor} allows to register an observer object (observers
 * have to implement this interface obviously) for monitoring the execution
 * of a computation when the computation is triggered by one of the 
 * <code>exec</code> methods.
 * <p>Notifications are dispatched <i>within the same thread that executes the
 * computation</i> and as follows:</p>
 * <ol>
 *   <li>The {@link #onStart() onStart} method is invoked at the beginning
 *   of the computation before performing the first step in the computation.
 *   </li>
 *   <li>The {@link #update(int) update} method is invoked after performing a 
 *   step in the computation.</li>
 *   <li>Finally when the computation is finished one out of the 
 *   {@link #onEnd(Object) onEnd}, {@link #onAbort(Throwable) onAbort}, or
 *   {@link #onCancel() onCancel} methods is called, depending on the outcome
 *   of the computation.  If the computation terminates normally, then the
 *   {@link #onEnd(Object) onEnd} method is called, passing in the result
 *   of the computation.  In the case of failure, the 
 *   {@link #onAbort(Throwable) onAbort} method is called instead, passing
 *   along the exception that caused the failure.  If the computation is
 *   cancelled, then {@link #onCancel() onCancel} method is called.</li>
 * </ol>
 * <p>If the {@link CmdProcessor} is executing a {@link java.lang.Runnable} or
 * an {@link Invocation}, the computation only consists of one step &#151; the 
 * execution of the <code>run</code> or <code>call</code> method, respectively.
 * In the case of an {@link Invocation} chain or {@link MultiStepTask} a step
 * correspond to an invocation of the <code>call</code> or <code>doStep</code>
 * method, respectively.</p>
 * <p>One thing to keep in mind is that cancelling execution <i>does not</i>
 * imply that the {@link #onCancel() onCancel} method will be called.  In fact,
 * execution could well complete before the cancel signal is detected, in which
 * case the {@link #onEnd(Object) onEnd} method would be called instead.</p>
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
public interface ExecMonitor
{
    /**
     * Called just before executing the first step in the computation.
     */
    public void onStart();
    
    /**
     * Called after each step in the computation.
     * The passed argument will be <code>1</code> after the first step,
     * <code>2</code> after the second step, and so on.
     * Note that if the {@link CmdProcessor} is executing a 
     * {@link java.lang.Runnable} or an {@link Invocation}, the computation
     * only consists of one step.  In this case, this method is only called
     * once with <code>1</code> as argument.
     * 
     * @param step  Which step has been executed.
     */
    public void update(int step);
    
    /**
     * Notifies cancellation.
     * The execution terminated because a cancellation signal was received or
     * has never started at all.
     */
    public void onCancel();
    
    /**
     * Notifies failure.
     * The execution terminated because of an error.
     * 
     * @param cause The cause of failure. 
     */
    public void onAbort(Throwable cause);
    
    /**
     * Notifies the end of the computation.
     * The execution terminated normally.  If a result was computed, it is
     * passed in &#151; this is the same object that can be retrieved via
     * the {@link Future}.  Otherwise, <code>null</code> is passed in &#151; 
     * in the case the computation was encoded as a {@link java.lang.Runnable}.
     * 
     * @param result    The result of the computation or <code>null</code> if
     *                  no result was produced.
     */
    public void onEnd(Object result);
    
}
