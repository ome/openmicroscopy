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

/*------------------------------------------------------------------------------
 *
 * Written by:     Jean-Marie Burel     <j.burel@dundee.ac.uk>
 *                      Andrea Falconi          <a.falconi@dundee.ac.uk>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.event;

//Java import
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;


/** Implements the<code>EventBus</code> interface and is the pumping heart
 * or the event propagation system. It maintains a de-multiplex table to keep track of
 * what events have to be dispatched to which subscribers
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */
public class EventBusImpl
    implements EventBus {
    
    private LinkedList  eventQueue;
    private HashMap     deMultiplexTable;
    private int state;
    private static final int IDLE = 0;
    private static final int DISPATCHING = 1;   
    
/** Creates a new instance of EventBusImpl */
    public EventBusImpl() {
        eventQueue = new LinkedList();
        deMultiplexTable = new HashMap();
        state = IDLE;
    }    
/** Implemented as specified by {@linkEventBus}.
 */    
    public void register(AgentEventListener  subscriber, Class[] events){
        for (int j=0; j<events.length; ++j) {
            Class eventClass = events[j];
            if (verifyInheritance(eventClass)) { // check inheritance
                LinkedList list = (LinkedList)deMultiplexTable.get(eventClass);
                if (list != null) {
                    ListIterator i = list.listIterator();
                    while (i.hasNext()) {
                        AgentEventListener listener = (AgentEventListener)i.next();
                        if (listener != subscriber) list.addLast(subscriber);
                    }
                } else {
                    list = new LinkedList();
                    list.addFirst(subscriber);
                }
                deMultiplexTable.put(eventClass, list);
            }     
        }
    }
    
/** Implemented as specified by {@linkEventBus}.
 */    
    public void register(AgentEventListener  subscriber, Class event){
        if (verifyInheritance(event)) { // check inheritance
            LinkedList list = (LinkedList)deMultiplexTable.get(event);
            if (list != null) {
                ListIterator i = list.listIterator();
                while (i.hasNext()) {
                    AgentEventListener listener = (AgentEventListener)i.next();
                    if (listener != subscriber) list.addLast(subscriber);
                }
            } else {
                list = new LinkedList();
                list.addFirst(subscriber);
            }
            deMultiplexTable.put(event, list);
        }     
    }    
/** Implemented as specified by {@linkEventBus}.
 */    
    public void remove(AgentEventListener  subscriber, Class[] events) {
        for (int j=0; j<events.length; ++j) {
            Class eventClass = events[j];
            if (verifyInheritance(eventClass)) { // check inheritance
                LinkedList list = (LinkedList)deMultiplexTable.get(eventClass);
                ListIterator i = list.listIterator();
                while (i.hasNext()) {
                    AgentEventListener listener = (AgentEventListener)i.next();
                    if (listener.equals(subscriber)) list.remove(subscriber);
                }
                if (list != null) deMultiplexTable.put(eventClass, list);
                else deMultiplexTable.remove(eventClass);
            }     
        }   
    }
/** Implemented as specified by {@linkEventBus}.
 */ 
    public void remove(AgentEventListener  subscriber, Class event) {
        if (verifyInheritance(event)) { // check inheritance
            LinkedList list = (LinkedList)deMultiplexTable.get(event);
            ListIterator i = list.listIterator();
            while (i.hasNext()) {
                AgentEventListener listener = (AgentEventListener)i.next();
                if (listener.equals(subscriber)) list.remove(subscriber);
            }
            if (list != null) deMultiplexTable.put(event, list);
            else deMultiplexTable.remove(event);
        }     
    }
    
/** Implemented as specified by {@linkEventBus}.
 */ 
    public void remove(AgentEventListener  subscriber) {
        Iterator   e = deMultiplexTable.keySet().iterator();
        while (e.hasNext()) {
            Class event = (Class)e.next();
            if (verifyInheritance(event)) { // check inheritance
                LinkedList list = (LinkedList)deMultiplexTable.get(event);
                ListIterator i = list.listIterator();
                while(i.hasNext()){
                    AgentEventListener listener = (AgentEventListener)i.next();
                    if (listener.equals(subscriber)) list.remove(subscriber);
                }
                if (list != null) deMultiplexTable.put(event, list);
                else deMultiplexTable.remove(event);
            } 
        }
    }    
/** Implemented as specified by {@linkEventBus}.
 */  
    public void post(AgentEvent e) {
        switch(state){
            case IDLE:
                state = DISPATCHING;
                eventQueue.addFirst(e);
                while (!eventQueue.isEmpty()) {
                    dispatch();
                }
                state = IDLE;
                break;
            case DISPATCHING:               
                eventQueue.addFirst(e);                
        }
    }
    
/** Dispatch the event */
    private void dispatch() {
        // grab the first event posted
        AgentEvent e = (AgentEvent)eventQueue.removeLast();
        Class eventClass = e.getClass();
        LinkedList list = (LinkedList)deMultiplexTable.get(eventClass);
        ListIterator i = list.listIterator();
        while (i.hasNext()) {
            AgentEventListener listener = (AgentEventListener)i.next();
            if (e.getSource() != listener) listener.eventFired(e);
        }   
    }
    
/** verify the inheritance of the class
 *
 *@param eventClass      class 
 */
    private boolean verifyInheritance(Class eventClass) {
        Class agtEvent = AgentEvent.class;
        boolean b = false;
        while (eventClass != null) {  //find first class, if any, in inheritance hierarchy 
            if (eventClass == agtEvent) {
                b = true ;
                eventClass = null;
            } else {
                eventClass = eventClass.getSuperclass();
            }
        } 
        return b;
    }
    
    
}
