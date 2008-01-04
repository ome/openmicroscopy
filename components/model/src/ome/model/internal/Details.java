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
// TODO #if(!$type.global)
// <many-to-one name="owner" class="ome.model.meta.Experimenter"
// column="owner_id" not-null="true" cascade="$cascade_settings"/>
// <many-to-one name="group" class="ome.model.meta.ExperimenterGroup"
// column="group_id" not-null="true" cascade="$cascade_settings"/>
// <!-- creationEvent is not updateable -->
// <many-to-one name="creationEvent" class="ome.model.meta.Event"
// column="creation_id" update="false"
// not-null="true" cascade="$cascade_settings"/>
// #if(!$type.immutable)
// <many-to-one name="updateEvent" class="ome.model.meta.Event"
// column="update_id" update="true"
// not-null="true" cascade="$cascade_settings"/>
// #end
// #end
public class Details extends GlobalDetails {

    private static final long serialVersionUID = 1176546684904748976L;

    public final static String CREATIONEVENT = "Details_creationEvent";

    public final static String OWNER = "Details_owner";

    public final static String GROUP = "Details_group";

    Event _creation;

    Experimenter _owner;

    ExperimenterGroup _group;

    /** default constructor. Leaves values null to save resources. */
    public Details() {
    }

    /** copy-constructor */
    public Details(Details copy) {
        super(copy);
        setCreationEvent(copy.getCreationEvent());
        setOwner(copy.getOwner());
        setGroup(copy.getGroup());
    }

    @Override
    public Details shallowCopy() {
        Details newDetails = new Details();
        newDetails.setOwner(this.getOwner() == null ? null : new Experimenter(
                this.getOwner().getId(), false));
        newDetails.setGroup(this.getGroup() == null ? null
                : new ExperimenterGroup(this.getGroup().getId(), false));
        newDetails.setCreationEvent(this.getCreationEvent() == null ? null
                : new Event(this.getCreationEvent().getId(), false));
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
        return new Details();
    }

    /**
     * simple view of the Details. Accesses only the ids of the contained
     * entities
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Details:{");
        toString(sb);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void toString(StringBuilder sb) {
        super.toString(sb);
        sb.append("user=");
        sb.append(_owner == null ? null : _owner.getId());
        sb.append(";group=");
        sb.append(_group == null ? null : _group.getId());
        sb.append(";create=");
        sb.append(_creation == null ? null : _creation.getId());
    }

    // Getters & Setters
    // ===========================================================

    public Experimenter getOwner() {
        return _owner;
    }

    public Event getCreationEvent() {
        return _creation;
    }

    public void setOwner(Experimenter exp) {
        _owner = exp;
    }

    public void setCreationEvent(Event e) {
        _creation = e;
    }

    public ExperimenterGroup getGroup() {
        return _group;
    }

    public void setGroup(ExperimenterGroup _group) {
        this._group = _group;
    }

    @Override
    public boolean acceptFilter(Filter filter) {
        setOwner((Experimenter) filter.filter(OWNER, getOwner()));
        setGroup((ExperimenterGroup) filter.filter(GROUP, getGroup()));
        setCreationEvent((Event) filter.filter(CREATIONEVENT,
                getCreationEvent()));
        return super.acceptFilter(filter);

    }

    @Override
    public Object retrieve(String field) {
        if (field == null) {
            return null;
        } else if (field.equals(OWNER)) {
            return getOwner();
        } else if (field.equals(GROUP)) {
            return getGroup();
        } else if (field.equals(CREATIONEVENT)) {
            return getCreationEvent();
        } else {
            return super.retrieve(field);
        }
    }

    @Override
    public void putAt(String field, Object value) {
        if (field == null) {
            return;
        } else if (field.equals(OWNER)) {
            setOwner((Experimenter) value);
        } else if (field.equals(GROUP)) {
            setGroup((ExperimenterGroup) value);
        } else if (field.equals(CREATIONEVENT)) {
            setCreationEvent((Event) value);
        } else {
            super.putAt(field, value);
        }
    }

}
