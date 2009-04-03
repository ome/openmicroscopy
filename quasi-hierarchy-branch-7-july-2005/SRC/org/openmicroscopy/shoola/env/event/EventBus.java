/*
 * org.openmicroscopy.shoola.env.event.EventBus
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

package org.openmicroscopy.shoola.env.event;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines how client classes access the event bus service.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public interface EventBus
{
    

	/** 
	 * Add the subscriber to the List.
	 * An agent uses the register method in order to be notified  
	 * of occurences of the specified event types.
	 *
	 * @param subscriber	AgentListener objet to add.
	 * @param events		list of classes that the subscriber registers for.
	 */     
    public void register(AgentEventListener  subscriber, Class[] events);
    
	/** 
	 * Add the subscriber to the List.
	 * An agent uses the register method in order to be notified  
	 * of occurences of the specified event types.
	 *
	 * @param subscriber    AgentListener objet to add.
	 * @param event			a class. 
	 */     
    public void register(AgentEventListener  subscriber, Class event);
    
	/** 
	 * Remove the subscriber to the List.
	 *
	 * @param subscriber	AgentListener objet to remove.
	 * @param events		list of classes. 
	 */    
    public void remove(AgentEventListener  subscriber);
    
	/** 
	 * Remove the subscriber to the List.
	 *
	 * @param subscriber     AgentListener objet to remove.
	 * @param event          a class.
	 */     
    public void remove(AgentEventListener  subscriber, Class event);
    
	/** 
	 * Remove the subscriber to the List.
	 *
	 * @param subscriber	AgentListener objet to remove.
	 * @param events		list of classes. 
	 */     
    public void remove(AgentEventListener  subscriber, Class[] events);
    
    /**
     * Indicate whether or not there are any listeners for a type of event
     * @param e
     */
    public boolean hasListenerFor(Class event);
    
	/** 
	 * Manages the event i.e. fires an event and puts it on the EventBus. 
	 * The event can be delivered to all subscribers that registered  in 
	 * that type of event.
	 * 
	 * @param e  AgentEvent to post.
	 *
	 */ 
    public void post(AgentEvent e);
      
}