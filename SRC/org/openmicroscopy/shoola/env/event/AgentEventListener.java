package org.openmicroscopy.shoola.env.event;

/**
 * The AgentEventListener interface represents a subscriber to the event bus. It has
 * to be implemented by all subscribers in order to register for event notifications
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */


public interface AgentEventListener {   

/* Call back method  that the event bus invokes in order to dispatch an event
 *
 *@param e      AgentEvent to be dispatched
 */
    public void eventFired(AgentEvent e);    
}

