/*
 * org.openmicroscopy.shoola.env.data.views.BatchCallMonitor
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

package org.openmicroscopy.shoola.env.data.views;


//Java imports
import javax.swing.SwingUtilities;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.events.DSCallOutcomeEvent;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.util.concur.tasks.ExecMonitor;

/** 
 * An implementation of {@link ExecMonitor} that works as an adapter to notify
 * a given {@link AgentEventListener} of a call {@link BatchCallTree tree}'s 
 * execution events. 
 * <p>Each instance is bound to a given {@link AgentEventListener} adaptee and
 * a specific tree, a {@link BatchCallTree} object.  This adapter will notify
 * its adaptee of the tree's execution progress and of the eventual outcome of
 * the computation.</p>
 * <p>Specifically, before each leaf {@link BatchCall} is executed, a 
 * {@link DSCallFeedbackEvent} is delivered to the adaptee.  The event's 
 * {@link DSCallFeedbackEvent#getStatus() status} is set to the call's
 * {@link BatchCall#getDescription() description} and the event's progress
 * {@link DSCallFeedbackEvent#getPercentDone() indicator} is set to the percent
 * number of the leaf {@link BatchCall}s that have been executed so far.  So the
 * indicator will be <code>0</code> within the first feedback event and, if the 
 * computation runs to completion, <code>100</code> within the last feedback 
 * event, which will always have its status field set to <code>null</code> as no
 * call is to be executed next &#151; note that a <code>null</code> status is 
 * also possible for the previous events if the corresponding call has no 
 * description.</p>
 * <p>It's important to keep in mind that the tree's computation may not run
 * to completion &#151; either because a leaf call raises an exception or
 * or because the client {@link CallHandle#cancel() cancels} execution.  In 
 * both cases, the feedback notification won't run to completion either.  
 * However, in any case a final {@link DSCallOutcomeEvent} is delivered to the
 * adaptee to notify of the computation outcome.  In particular if the 
 * computation is not cancelled or no exception is raised, then the result of 
 * the whole computation, as returned by the 
 * {@link BatchCallTree#getResult() tree}, is made available through the
 * {@link DSCallOutcomeEvent#getResult() DSCallOutcomeEvent} object.</p>
 * <p>Finally, all events are delivered <i>sequentially</i> and wihin the 
 * <i>Swing</i> event dispatching thread.  This means the adaptee can run 
 * synchronously with respect to the UI and won't need to worry about 
 * concurrency issues.  Also, subsquent feedback events imply computation 
 * progress and the {@link DSCallOutcomeEvent} is always the last event to
 * be delivered in order of time.</p>
 *
 * @see BatchCallTree
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
class BatchCallMonitor
    implements ExecMonitor
{

    /** The call tree. */
    private BatchCallTree       tree;
    
    /** The adaptee to notify. */
    private AgentEventListener  adaptee;
    
    /**
     * Creates a new instance.
     * 
     * @param tree The call tree.
     * @param observer The adaptee to notify of execution events in the tree.
     */
    BatchCallMonitor(BatchCallTree tree, AgentEventListener observer)
    {
        if (tree == null) throw new NullPointerException("No tree.");
        if (observer == null) throw new NullPointerException("No observer.");
        this.tree = tree;
        this.adaptee = observer;
    }

    /**
     * Delivers the specified event to the {@link #adaptee}.
     * The event is dispatches within the <i>Swing</i> dispatching thread.
     *  
     * @param ae The event to dispatch.
     */
    protected void deliver(final AgentEvent ae)
    {
        Runnable notification = new Runnable() {
            public void run() { adaptee.eventFired(ae); }
        };
        SwingUtilities.invokeLater(notification);
    }
    //NOTE: This method is protected so that subclasses can be used for 
    //      testing that dispatch in the test driver's thread. 
    
    /**
     * Issues the first feedback event when the computation starts.
     * 
     * @see ExecMonitor#onStart()
     */
    public void onStart()
    {
        update(0); //Will produce a percent indicator of 0 (no call exec'd yet).
    }

    /**
     * Issues the next feedback event.
     * 
     * @see ExecMonitor#update(int)
     */
    public void update(int step)
    {
        //Obtain the number of calls every time.  We need to do this b/c the
        //tree supports dynamic addition of nodes as it's been executed.
        int calls = tree.countCalls();
        
        //Figure out the percent value and status.  Then create a feedback
        //event to hold those values and deliver it to the adaptee.
        int perc = (0 < calls ? (step*100)/calls : -1);
        BatchCall curCall = tree.getCurCall();
        DSCallFeedbackEvent feedback;
        if (curCall != null)
            feedback = new DSCallFeedbackEvent(perc, curCall.getDescription());
        else
            //Computation has finished regularly.  B/c update is always called
            //*after* doStep, we have no curCall.
            feedback = new DSCallFeedbackEvent(100, null);
        deliver(feedback);
    }

    /**
     * Notifies the {@link #adaptee} that the computation has been cancelled.
     * 
     * @see ExecMonitor#onCancel()
     */
    public void onCancel()
    {
        DSCallOutcomeEvent cancelEvent = new DSCallOutcomeEvent();
        deliver(cancelEvent);
    }

    /**
     * Notifies the {@link #adaptee} that the computation stopped because of
     * an exception.
     * 
     * @see ExecMonitor#onAbort(java.lang.Throwable)
     */
    public void onAbort(Throwable cause)
    {
        DSCallOutcomeEvent errorEvent = new DSCallOutcomeEvent(cause);
        deliver(errorEvent);
    }

    /**
     * Notifies the {@link #adaptee} that the computation has finished
     * regularly and delivers the result.
     * 
     * @see ExecMonitor#onEnd(java.lang.Object)
     */
    public void onEnd(Object result)
    {
        //The result object has to be ignored in our case.  In fact, this is
        //a List which contains all values returned by the leaf calls --
        //all nulls because a BatchCall#doCall returns no value.  It's up to
        //the BatchCallTree to assemble and return the final result.
        
        Object finalResult = tree.getResult();
        DSCallOutcomeEvent resultEvent = new DSCallOutcomeEvent(finalResult);
        deliver(resultEvent);
    }

}
