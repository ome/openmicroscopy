package ome;

import ome.model.meta.Event;
import ome.model.meta.Experimenter;

public interface NewModel {
	
	public Integer getId();
	public Integer getVersion();
	public byte[] getPermissions();
	public Experimenter getOwner();	
	public Event getCreationEvent();
	public Event getUpdateEvent();
	public void setPermissions(byte[] perms);
	public void setOwner(Experimenter exp);	
	public void setCreationEvent(Event e);
	public void setUpdateEvent(Event e);
	
}
