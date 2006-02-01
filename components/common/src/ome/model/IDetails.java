package ome.model;

import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;

public interface IDetails {
	
	public Permissions getPermissions();
	public Experimenter getOwner();	
	public Event getCreationEvent();
	public void setPermissions(Permissions perms);
	public void setOwner(Experimenter exp);	
	public void setCreationEvent(Event e);
	
}
