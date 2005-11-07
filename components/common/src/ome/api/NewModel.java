package ome.api;

import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.util.Filterable;

public interface NewModel extends Filterable{
	
	public Integer getId();
	public Integer getVersion();
	public Permissions getPermissions();
	public Experimenter getOwner();	
	public Event getCreationEvent();
	public Event getUpdateEvent();
	public void setPermissions(Permissions perms);
	public void setOwner(Experimenter exp);	
	public void setCreationEvent(Event e);
	public void setUpdateEvent(Event e);

	
}
