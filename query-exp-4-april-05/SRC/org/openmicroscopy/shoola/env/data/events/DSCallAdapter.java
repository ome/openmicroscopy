/*
 * org.openmicroscopy.shoola.env.data.events.DSCallAdapter
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
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;

/** 
 * Adapter class for receiving {@link DSCallFeedbackEvent}s and 
 * {@link DSCallOutcomeEvent}s.
 * <p>The purpose of this class is to ease the job of creating observers for
 * asynchronous calls to the data services.  This class implements the
 * {@link AgentEventListener} interface and forwards every feedback event to the
 * {@link #update(DSCallFeedbackEvent) update} method.  When the notification of
 * the computation outcome is received (that is, a {@link DSCallFeedbackEvent}),
 * the {@link #onEnd() onEnd} method is called, then right after one of the
 * <code>handleXXX</code> methods is called depending on what was the
 * computation outcome &#151; note that the <code>handleXXX</code> methods are
 * mutually exclusive, only one will ever be invoked.</p>
 * <p>This class provides a default <i>no-op</i> implementation of the 
 * <code>update</code>, <code>onEnd</code>, and <code>handleXXX</code> methods
 * so that subclasses can override only those methods they care about.</p>
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
public abstract class DSCallAdapter
    implements AgentEventListener
{

    /**
     * Invokes one of the methods in this class accoding to the received 
     * <code>DSCall</code> event.
     * 
     * @see AgentEventListener#eventFired(AgentEvent)
     */
    public final void eventFired(AgentEvent ae)
    {
        if (ae instanceof DSCallFeedbackEvent) {  //Progress notification. 
            update((DSCallFeedbackEvent) ae);
        } else {  //Outcome notification.
            DSCallOutcomeEvent oe = (DSCallOutcomeEvent) ae;
            onEnd();
            switch (oe.getState()) {
                case DSCallOutcomeEvent.CANCELLED:
                    handleCancellation();
                    break;
                case DSCallOutcomeEvent.ERROR:
                    handleException(oe.getException());
                    break;
                case DSCallOutcomeEvent.NO_RESULT:
                    handleNullResult();
                    break;
                case DSCallOutcomeEvent.HAS_RESULT:
                    handleResult(oe.getResult());
            }
        }
        //Ignore any other event.
    }
    
    /**
     * Invoked when a progress notification is received.
     * 
     * @param progress Embodies the progress notification.
     */
    public void update(DSCallFeedbackEvent progress) {}
    
    /**
     * Invoked when the call returns.
     * This method is called upon receiving the {@link DSCallOutcomeEvent},
     * but before any of the <code>handleXXX</code> is invoked.
     */
    public void onEnd() {}
    
    /**
     * Invoked if the call returns normally with a non-<code>null</code>
     * return value.
     * This happens when the state of  the received {@link DSCallOutcomeEvent}
     * is {@link DSCallOutcomeEvent#HAS_RESULT HAS_RESULT}.
     * 
     * @param result The result of the call.  It's a non-<code>null</code>
     *               reference that you'll have to cast to a more suitable
     *               type as documented by the call.
     */
    public void handleResult(Object result) {}
    
    /**
     * Invoked if the call returns with a <code>null</code> value.
     * This happens when the state of  the received {@link DSCallOutcomeEvent}
     * is {@link DSCallOutcomeEvent#NO_RESULT NO_RESULT}.
     */
    public void handleNullResult() {}
    
    /**
     * Invoked if the call was cancelled.
     * This happens when the state of  the received {@link DSCallOutcomeEvent}
     * is {@link DSCallOutcomeEvent#CANCELLED CANCELLED}.
     *
     */
    public void handleCancellation() {}
    
    /**
     * Invoked if the call raised an exception.
     * This happens when the state of  the received {@link DSCallOutcomeEvent}
     * is {@link DSCallOutcomeEvent#ERROR ERROR}.
     * The <code>exc</code> argument is a non-<code>null</code> reference that
     * you'll have to cast to a more suitable type as documented by the call.  
     * However, keep in mind that other exceptions are possible besides those 
     * documented by the call.  In fact, <i>any</i> exception occurred during
     * the computation will be caught and eventually dispatched to this method. 
     * 
     * @param exc The exception that was thrown.
     */
    public void handleException(Throwable exc) {}

}
