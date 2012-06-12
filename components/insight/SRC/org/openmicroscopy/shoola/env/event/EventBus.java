/*
 * org.openmicroscopy.shoola.env.event.EventBus
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
	 * Adds the subscriber to the List.
	 * An agent uses the register method in order to be notified  
	 * of occurrences of the specified event types.
	 *
	 * @param subscriber	AgentListener object to add.
	 * @param events		list of classes that the subscriber registers for.
	 */     
    public void register(AgentEventListener  subscriber, Class<?>[] events);
    
	/** 
	 * Adds the subscriber to the List.
	 * An agent uses the register method in order to be notified  
	 * of occurrences of the specified event types.
	 *
	 * @param subscriber    AgentListener object to add.
	 * @param event			a class. 
	 */     
    public void register(AgentEventListener  subscriber, Class<?> event);
    
	/** 
	 * Removes the subscriber from the List.
	 *
	 * @param subscriber	AgentListener object to remove.
	 */    
    public void remove(AgentEventListener  subscriber);
    
	/** 
	 * Removes the subscriber from the List.
	 *
	 * @param subscriber     AgentListener object to remove.
	 * @param event          a class.
	 */     
    public void remove(AgentEventListener  subscriber, Class<?> event);
    
	/** 
	 * Removes the subscriber from the List.
	 *
	 * @param subscriber	AgentListener object to remove.
	 * @param events		list of classes. 
	 */     
    public void remove(AgentEventListener  subscriber, Class<?>[] events);
    
    /**
     * Indicates whether or not there are any listeners for a type of event.
     * 
     * @param event a class.
     * @return  <code>true</code> if there is a listener for that event,
     *          <code>false</code> otherwise.
     */
    public boolean hasListenerFor(Class<?> event);
    
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