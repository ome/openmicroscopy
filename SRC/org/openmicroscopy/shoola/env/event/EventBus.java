package org.openmicroscopy.shoola.env.event;

/** Defines how client classes access the event bus service
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

public interface EventBus {
    

/** Add the subscriber to the List
 * An agent uses the register method in order to be notified  
 * of occuremces of the specified event types
 *
 *@param subscriber     AgentListener objet to add
 *@param events          list of classes
 */     
    public void register(AgentEventListener  subscriber, Class[] events);
    
/** Remove the subscriber to the List
 *
 *@param subscriber     AgentListener objet to remove
 *@param events          list of classes 
 */     
    public void remove(AgentEventListener  subscriber, Class[] events);
    
/** manages the event
 * i.e. fires an event and puts it on the EventBus so that it can be delivered to allsubscribers 
 *  that registered interestd in that type of event.
 * 
 *@param e  AgentEvent to post
 *
 */ 
    public void post(AgentEvent e);
    
    
}