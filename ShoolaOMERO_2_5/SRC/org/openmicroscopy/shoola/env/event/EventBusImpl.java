/*
 * org.openmicroscopy.shoola.env.event.EventBusImpl
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * Implements the<code>EventBus</code> interface, it is the pumping heart 
 * of the event propagation system. 
 * It maintains a de-multiplex table to 
 * keep track of what events have to be dispatched to which subscribers
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
class EventBusImpl
    implements EventBus
{
    
    /** Identifies the <code>IDLE</code> state. */
	private static final int	IDLE = 0;   
	
    /** Identifies the <code>DISPATCHING</code> state. */
	private static final int	DISPATCHING = 1;
	
	/** Sequence of events to be dispatched. */
    private LinkedList		eventQueue;
    
    /**
     * Keeps track of what events have to be dispatched to which subscribers. 
     * This is a map in which each key is an event class and the corresponding
     * value is a linked lists containing all the subscribers for that event
     * type.
     */
    private Map             deMultiplexTable;
    
    /** Marks the current state. */
    private int             state;
    
    /** Dispatches the next event. */
    private void dispatch()
    {
        //Grab the oldest event on the queue.
        AgentEvent e = (AgentEvent) eventQueue.removeLast();
        Class eventType = e.getClass();
        LinkedList evNotifList = (LinkedList) deMultiplexTable.get(eventType);
        if (evNotifList != null) {
            Iterator i = evNotifList.iterator();
            AgentEventListener listener;
            while (i.hasNext()) {
                listener = (AgentEventListener) i.next();
                if (!listener.equals(e.getSource())) listener.eventFired(e);
            }
        }  //else nobody registered for this event type.
    }
    
    /** 
     * Tells whether a given class inherits from {@link AgentEvent}.
     *
     * @param eventClass    The class to verify.
     * @return <code>true</code> if {@link AgentEvent} is an ancestor of 
     *          <code>eventClass</code>, <code>false</code> otherwise.
     */
    private boolean verifyInheritance(Class eventClass)
    {
        Class agtEvent = AgentEvent.class;
        boolean b = false;
        //Percolate inheritance hierarchy.
        while (eventClass != null) {   
            if (eventClass == agtEvent) {
                b = true;
                break;
            }
            eventClass = eventClass.getSuperclass();
        } 
        return b;
    }
    
	/** Creates a new instance. */
    EventBusImpl()
    {
        eventQueue = new LinkedList();
        deMultiplexTable = new HashMap();
        state = IDLE;
    }    
    
	/** 
     * Implemented as specified by {@link EventBus}. 
     * @see EventBus#register(AgentEventListener, Class[])
     */    
    public void register(AgentEventListener subscriber, Class[] eventTypes)
    {
		if (eventTypes == null)
			throw new NullPointerException("No event types.");
		for (int i = 0; i < eventTypes.length; ++i)
			register(subscriber, eventTypes[i]);
    }
    
	/** 
     * Implemented as specified by {@link EventBus}. 
     * @see EventBus#register(AgentEventListener, Class)
     */    
    public void register(AgentEventListener subscriber, Class eventType)
    {
    	if (subscriber == null)	
    		throw new NullPointerException("No subscriber.");
    	if (eventType == null)
			throw new NullPointerException("No event type.");
    	if (verifyInheritance(eventType)) { 
        	LinkedList evNotifList = 
        						(LinkedList) deMultiplexTable.get(eventType);
            if (evNotifList == null) {	
            	evNotifList = new LinkedList();
				deMultiplexTable.put(eventType, evNotifList);
            } 
            if (!evNotifList.contains(subscriber))	evNotifList.add(subscriber);
        }     
    } 
    
	/** 
     * Implemented as specified by {@link EventBus}. 
     * @see EventBus#remove(AgentEventListener, Class)
     */ 
	public void remove(AgentEventListener subscriber, Class eventType)
	{
		if (subscriber == null)	
			throw new NullPointerException("No subscriber.");
		if (eventType == null)
			throw new NullPointerException("No event type.");
		LinkedList evNotifList = (LinkedList) deMultiplexTable.get(eventType);
		if (evNotifList != null) {
			evNotifList.remove(subscriber);
			if (evNotifList.isEmpty())	deMultiplexTable.remove(eventType);
		}     
	}
	
    /** 
     * Implemented as specified by {@link EventBus}. 
     * @see EventBus#remove(AgentEventListener, Class[])
     */    
    public void remove(AgentEventListener subscriber, Class[] eventTypes)
    {
		if (eventTypes == null)
			throw new NullPointerException("No event types.");
		for (int i = 0; i < eventTypes.length; ++i)
			remove(subscriber, eventTypes[i]);   
    }

    /** 
     * Implemented as specified by {@link EventBus}. 
     * @see EventBus#remove(AgentEventListener)
     */ 
    public void remove(AgentEventListener subscriber)
    {
        Iterator e = deMultiplexTable.keySet().iterator();
        while (e.hasNext())	remove(subscriber, (Class) e.next());
    }
    
    /** 
     * Implemented as specified by {@link EventBus}. 
     * @see EventBus#hasListenerFor(Class)
     */ 
    public boolean hasListenerFor(Class eventType)
    {
    		return (deMultiplexTable.get(eventType) != null);
    }
    
    /** 
     * Implemented as specified by {@link EventBus}. 
     * @see EventBus#post(AgentEvent)
     */ 
    public void post(AgentEvent e)
    {
        if (e == null)	throw new NullPointerException("No event.");
        switch (state) {
            case IDLE:
                state = DISPATCHING;
                eventQueue.addFirst(e);
                while (!eventQueue.isEmpty())	dispatch();
                state = IDLE;
                break;
            case DISPATCHING:               
                eventQueue.addFirst(e);                
        }
    }

        
}
