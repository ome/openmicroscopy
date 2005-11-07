package ome.security;

import ome.model.meta.Event;

public class CurrentEvent {

	    private static ThreadLocal contextHolder = new ThreadLocal();

	    //~ Methods ================================================================

	    public static void setEvent(Event e) {
	        contextHolder.set(e);
	    }

	    public static Event getEvent() {
	        Event e = (Event) contextHolder.get();
	        if (e == null){
	        	e = new Event();
	        	e.setName("Null:t"+System.currentTimeMillis()+":h"+e.hashCode());
	        	setEvent(e);
	        }
	    	return e;
	    	
	    }
	}

