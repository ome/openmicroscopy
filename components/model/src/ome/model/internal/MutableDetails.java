/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model.internal;

// Java imports
import java.util.HashMap;

import javax.persistence.Embeddable;

import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.ExternalInfo;
import ome.util.Filter;

/**
 * value type for low-level (row-level) details for all
 * {@link ome.model.IObject} objects. Details instances are given special
 * treatment through the Omero system, especially during {@link ome.api.IUpdate 
 * update}.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 * @author josh
 * @see ome.api.IUpdate
 */
@Embeddable
public class MutableDetails extends Details {

    private static final long serialVersionUID = 1176546684904748976L;

    public final static String UPDATEEVENT = "Details_updateEvent";

    Event _update;

    /** default constructor. Leaves values null to save resources. */
    public MutableDetails() {
    }

    /** copy-constructor */
    public MutableDetails(MutableDetails copy) {
        super(copy);
        setUpdateEvent(copy.getUpdateEvent());
    }

    @Override
    public Details shallowCopy() {
        MutableDetails newDetails = new MutableDetails();
        newDetails.setOwner(this.getOwner() == null ? null : new Experimenter(
                this.getOwner().getId(), false));
        newDetails.setGroup(this.getGroup() == null ? null
                : new ExperimenterGroup(this.getGroup().getId(), false));
        newDetails.setCreationEvent(this.getCreationEvent() == null ? null
                : new Event(this.getCreationEvent().getId(), false));
        newDetails.setUpdateEvent(this.getUpdateEvent() == null ? null
                : new Event(this.getUpdateEvent().getId(), false));
        newDetails.setPermissions(this.getPermissions() == null ? null
                : new Permissions().revokeAll(this.getPermissions()));
        newDetails.setExternalInfo(this.getExternalInfo() == null ? null
                : new ExternalInfo(this.getExternalInfo().getId(), false));
        newDetails._filteredCollections = this.filteredSet();
        newDetails.setCounts(this.getCounts() == null ? null : new HashMap(this
                .getCounts()));
        return newDetails;
    }

    @Override
    public Details newInstance() {
        return new MutableDetails();
    }

    /**
     * simple view of the Details. Accesses only the ids of the contained
     * entities
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("MutableDetails:{");
        toString(sb);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void toString(StringBuilder sb) {
        super.toString(sb);
        sb.append(";update=");
        sb.append(_update == null ? null : _update.getId());
    }

    // Getters & Setters
    // ===========================================================

    public Event getUpdateEvent() {
        return _update;
    }

    public void setUpdateEvent(Event e) {
        _update = e;
    }

    @Override
    public boolean acceptFilter(Filter filter) {
        setUpdateEvent((Event) filter.filter(UPDATEEVENT, getUpdateEvent()));
        return super.acceptFilter(filter);
    }

    @Override
    public Object retrieve(String field) {
        if (field == null) {
            return null;
        } else if (field.equals(UPDATEEVENT)) {
            return getUpdateEvent();
        } else {
            return super.retrieve(field);
        }
    }

    @Override
    public void putAt(String field, Object value) {
        if (field == null) {
            return;
        } else if (field.equals(UPDATEEVENT)) {
            setUpdateEvent((Event) value);
        } else {
            super.putAt(field, value);
        }
    }

}
