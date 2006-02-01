package ome.model.internal;

import ome.model.IDetails;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;


public class Details implements IDetails
{

    Permissions _perms;
    Event _creation;
    Event _update;
    Experimenter _owner;
    
    public Permissions getPermissions()
    {
        return _perms;
    }

    public Experimenter getOwner()
    {
        return _owner;
    }

    public Event getCreationEvent()
    {
        return _creation;
    }
    
    public Event getUpdateEvent()
    {
        return _update;
    }

    public void setPermissions(Permissions perms)
    {
        _perms = perms;
    }

    public void setOwner(Experimenter exp)
    {
        _owner = exp;
    }

    public void setCreationEvent(Event e)
    {
        _creation = e;
    }

    public void setUpdateEvent(Event e)
    {
        _update = e;
    }

}
