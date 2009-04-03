/*
 * org.openmicroscopy.shoola.env.data.views.MockDSCallEventListener
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.events.DSCallOutcomeEvent;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.MockAgentEventListener;

/** 
 * Extends {@link MockAgentEventListener} to have <code>DSCall</code> events
 * considered equal if they hold the same state.
 * When testing the execution of a {@link BatchCallTree}, we want to use a
 * mock listener to verify that execution events are properly delivered.
 * However a problem arises: those events are generated internally by the
 * {@link BatchCallMonitor}, so the mock mustn't rely on object identity to
 * verify that the expected events (specified in set up mode) correspond to
 * the actual ones delivered by the {@link BatchCallMonitor} during execution.
 * An immediate solution would be to override the <code>equals</code> method
 * of {@link DSCallFeedbackEvent} and {@link DSCallOutcomeEvent}, but this is
 * not desirable because, in general, identity must be maintained for events 
 * &#151; even though two event object may have exaclty the same state, they
 * represent different occurrences.  
 * This general rule doesn't apply in our test set up though &#151; an event
 * specified in set up mode should be considered the same as one received in
 * verification mode if they hold the same state.  For this reason, this class
 * wraps the events specified in set up mode and those received in verification 
 * mode with equality wrappers that consider two objects equal if they hold the
 * same state.  Obviously enough for this to be sound, the test must be 
 * designed such that no two events specified in set up mode carry the same
 * state.  
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
public class MockDSCallEventListener
    extends MockAgentEventListener
{

    /**
     * Tells whether <code>p1</code> and <code>p2</code> may be considered to
     * be the same object.
     * 
     * @param p1 The first object.
     * @param p2 The second object.
     * @return <code>true</code> if the passed objects hold the same state,
     *          <code>false</code> otherwise.
     */
    private static boolean isSameObject(Object p1, Object p2)
    {
        boolean b = (p1 == p2);
        if (!b) {
            if (p1 == null)  //Then p2 can't be null.
                b = false;
            else  //Both p1 and p2 are not null.
                b = p2.equals(p1);
        }
        return b;
    }
    
    //Equality wrapper for DSCallFeedbackEvent.
    private class FeedbackWrapper 
        extends AgentEvent
    {
        DSCallFeedbackEvent event;
        FeedbackWrapper(DSCallFeedbackEvent event) { this.event = event; }
        public boolean equals(Object o)
        {  
            if (o == null || !(o instanceof FeedbackWrapper)) return false;
            DSCallFeedbackEvent x = ((FeedbackWrapper) o).event;
            if (x.getPercentDone() == event.getPercentDone() &&
                    isSameObject(x.getStatus(), event.getStatus()))
                return true;
            return false;
        }
        public String toString()
        {
            return "FE[perc: "+event.getPercentDone()+", status: "+
                    event.getStatus()+"]";
        }
    }
    
    //Equality wrapper for DSCallOutcomeEvent.
    private class OutcomeWrapper
        extends AgentEvent
    {
        DSCallOutcomeEvent event;
        OutcomeWrapper(DSCallOutcomeEvent event) { this.event = event; }
        public boolean equals(Object o)
        {  
            if (o == null || !(o instanceof OutcomeWrapper)) return false;
            DSCallOutcomeEvent x = ((OutcomeWrapper) o).event;
            if (x.wasCancelled() == event.wasCancelled() &&
                    isSameObject(x.getException(), event.getException()) &&
                    isSameObject(x.getResult(), event.getResult()))
                return true;
            return false;
        }
        public String toString()
        {
            return "OE[cancelled: "+event.wasCancelled()+", exc: "+
                    event.getException()+", result: "+
                    event.getResult()+"]";
        }
    }
    
    private AgentEvent convert(AgentEvent ae)
    {
        if (ae != null) {
            if (ae instanceof DSCallFeedbackEvent) 
                ae = new FeedbackWrapper((DSCallFeedbackEvent) ae);
            else if (ae instanceof DSCallOutcomeEvent)
                ae = new OutcomeWrapper((DSCallOutcomeEvent) ae);
        }
        return ae;
    }
    
    //Used in set up mode.  If re != null, then it will be
    //thrown in verification mode.
    public void eventFired(AgentEvent ae, RuntimeException re)
    {
        super.eventFired(convert(ae), re);
    }
    
    //Used in verification mode.
    public void eventFired(AgentEvent ae)
    {
        super.eventFired(convert(ae));
    }
    
}
