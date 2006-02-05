package ome.model.internal;

import java.util.HashSet;
import java.util.Set;

import ome.model.IDetails;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;


public class Details implements IDetails
{

    Permissions _perms;
    Event _creation;
    Event _update;
    Experimenter _owner;
    ExperimenterGroup _group;
    Set _filteredCollections;

    // Loaded&Filtering methods
    // ===========================================================
    public void addFiltered(String collectionName)
    {
        if (_filteredCollections == null)
            _filteredCollections = new HashSet();
        
        _filteredCollections.add(collectionName);
    }
    
    public boolean isFiltered(String collectionName)
    {
        if (_filteredCollections == null) return false;
        if (_filteredCollections.contains(collectionName)) return true;
        return false;
    }
    
    public void clearFiltered(){
        _filteredCollections = null;
    }
    
    public Set filteredSet(){
        if (_filteredCollections == null) return new HashSet();
        return new HashSet(_filteredCollections);
    }
    
    // Getters & Setters
    // ===========================================================
    
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
    
    public ExperimenterGroup getGroup()
    {
        return _group;
    }

    public void setGroup(ExperimenterGroup _group)
    {
        this._group = _group;
    }

}
