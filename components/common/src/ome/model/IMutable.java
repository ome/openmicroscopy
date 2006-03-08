package ome.model;

import ome.model.meta.Event;

public interface IMutable { // TODO extends IObject? fix mapping.vm then.
	
	public Integer getVersion();
    public void setVersion( Integer version );
	// TODO public Event getUpdateEvent();
	// TODO public void setUpdateEvent(Event e);
	
}
