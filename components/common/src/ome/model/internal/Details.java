package ome.model.internal;

import java.util.Arrays;
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
 
    // ~ Other
    // ===========================================================
    public String toString()
    {
        StringBuffer sb = new StringBuffer(128);
        sb.append("Details:{");
        sb.append("user=");sb.append(_owner.getId());
        sb.append(";group=");sb.append(_group.getId());
        sb.append(";perm=");sb.append(Arrays.toString(_perms.getBytes()));
        sb.append(";create=");sb.append(_creation.getId());
        sb.append(";update=");sb.append(_update.getId());
        sb.append("}");
        return sb.toString();
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
