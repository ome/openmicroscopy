/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2017 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 *
 */

package omero.gateway.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import omero.RBool;
import omero.model.Experimenter;
import omero.model.ExperimenterI;
import omero.model.GroupExperimenterMap;

/**
 * The data that makes up an <i>OME</i> Experimenter along with information
 * about the Group the Experimenter belongs in.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OME2.2
 */
public class ExperimenterData extends DataObject {

    /** Identifies the {@link ExperimenterI#FIRSTNAME} field. */
    public final static String FIRSTNAME = ExperimenterI.FIRSTNAME;

    /** Identifies the {@link ExperimenterI#MIDDLENAME} field. */
    public final static String MIDDLENAME = ExperimenterI.MIDDLENAME;

    /** Identifies the {@link ExperimenterI#LASTNAME} field. */
    public final static String LASTNAME = ExperimenterI.LASTNAME;

    /** Identifies the {@link ExperimenterI#EMAIL} field. */
    public final static String EMAIL = ExperimenterI.EMAIL;

    /** Identifies the {@link ExperimenterI#OMENAME} field. */
    public final static String OMENAME = ExperimenterI.OMENAME;

    /** Identifies the {@link ExperimenterI#INSTITUTION} field. */
    public final static String INSTITUTION = ExperimenterI.INSTITUTION;

    /** Identifies the {@link ExperimenterI#GROUPEXPERIMENTERMAP} field. */
    public final static String GROUP_EXPERIMENTER_MAP = ExperimenterI.GROUPEXPERIMENTERMAP;

    /** The other Groups this Experimenter belongs in. */
    private List<GroupData> groups;

    /** Creates a new instance. */
    public ExperimenterData() {
        setDirty(true);
        setValue(new ExperimenterI());
    }

    /**
     * Creates a new instance.
     *
     * @param experimenter
     *            Back pointer to the {@link Experimenter} model object. Mustn't
     *            be <code>null</code>.
     * @throws IllegalArgumentException
     *             If the object is <code>null</code>.
     */
    public ExperimenterData(Experimenter experimenter) {
        if (experimenter == null) {
            throw new IllegalArgumentException("Object cannot null.");
        }
        setValue(experimenter);
    }

    // Immutables

    /**
     * Sets the first name of the experimenter.
     *
     * @param firstName
     *            The value to set.
     */
    public void setFirstName(String firstName) {
        setDirty(true);
        asExperimenter().setFirstName(omero.rtypes.rstring(firstName));
    }

    /**
     * Returns the first name of the experimenter.
     *
     * @return see above.
     */
    public String getFirstName() {
        omero.RString n = asExperimenter().getFirstName();
        if (n == null || n.getValue() == null) return "";
        return n.getValue();
    }

    /**
     * Sets the last name of the experimenter.
     *
     * @param lastName
     *            The value to set.
     */
    public void setLastName(String lastName) {
        setDirty(true);
        asExperimenter().setLastName(omero.rtypes.rstring(lastName));
    }

    /**
     * Returns the last name of the experimenter.
     *
     * @return see above.
     */
    public String getLastName() {
        omero.RString n = asExperimenter().getLastName();
        if (n == null || n.getValue() == null) return "";
        return n.getValue();
    }

    /**
     * Returns the last name of the experimenter.
     *
     * @return see above.
     */
    public String getUserName() {
        omero.RString n = asExperimenter().getOmeName();
        if (n == null || n.getValue() == null) {
            throw new IllegalStateException(
                    "The name should never have been null");
        }
        return n.getValue();
    }

    /**
     * Sets the e-mail of the experimenter.
     *
     * @param email
     *            The value to set.
     */
    public void setEmail(String email) {
        setDirty(true);
        asExperimenter().setEmail(omero.rtypes.rstring(email));
    }

    /**
     * Returns the e-mail of the experimenter.
     * 
     * @return see above.
     */
    public String getEmail() {
        omero.RString e = asExperimenter().getEmail();
        return e == null ? null : e.getValue();

    }

    /**
     * Sets the institution where the experimenter works.
     *
     * @param institution
     *            The value to set.
     */
    public void setInstitution(String institution) {
        setDirty(true);
        asExperimenter().setInstitution(
                institution == null ? null : omero.rtypes.rstring(institution));
    }

    /**
     * Returns the institution where the experimenter works.
     *
     * @return see above.
     */
    public String getInstitution() {
        omero.RString i = asExperimenter().getInstitution();
        return i == null ? null : i.getValue();
    }

    // Lazy loaded links

    /**
     * Returns the groups the experimenter is a member of.
     *
     * @return See above.
     */
    public List<GroupData> getGroups() {

        if (groups == null
                && asExperimenter().sizeOfGroupExperimenterMap() >= 0) {
            groups = new ArrayList<GroupData>();
            List<GroupExperimenterMap> links = asExperimenter()
                    .copyGroupExperimenterMap();
            for (GroupExperimenterMap link : links) {
                // if you somehow managed to delete a user's default group
                // link can be null!
                if (link != null) {
                    groups.add(new GroupData(link.getParent()));
                }
            }
        }

        return groups == null ? null : new ArrayList<GroupData>(groups);
    }

    // Link mutations

    /**
     * Sets the groups the experimenter is a member of.
     *
     * @param newValue
     *            The set of groups.
     */
    public void setGroups(List<GroupData> newValue) {
        List<GroupData> currentValue = new ArrayList<GroupData>(getGroups());
        SetMutator<GroupData> m = new SetMutator<GroupData>(currentValue,
                newValue);

        while (m.moreDeletions()) {
            setDirty(true);
            asExperimenter()
            .unlinkExperimenterGroup(m.nextDeletion().asGroup());
        }

        while (m.moreAdditions()) {
            setDirty(true);
            asExperimenter().linkExperimenterGroup(m.nextAddition().asGroup());
        }

        groups = m.result();
    }

    /**
     * Returns the default Group for this Experimenter
     *
     * @return See above.
     */
    public GroupData getDefaultGroup() {
        List<GroupData> groups = getGroups();
        if (CollectionUtils.isEmpty(groups)) return null;
        return groups.get(0);
    }

    /**
     * Returns the middle name of the experimenter.
     * 
     * @return see above.
     */
    public String getMiddleName() {
        omero.RString n = asExperimenter().getMiddleName();
        if (n == null || n.getValue() == null) {
            throw new IllegalStateException(
                    "The name should never have been null");
        }
        return n.getValue();
    }

    /**
     * Sets the middle name of the experimenter.
     *
     * @param middleName
     *            The value to set.
     */
    public void setMiddleName(String middleName) {
        setDirty(true);
        asExperimenter().setMiddleName(omero.rtypes.rstring(middleName));
    }

    /**
     * Returns <code>true</code> if the experimenter is active,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isActive()
    {
        List<GroupData> groups = getGroups();
        if (CollectionUtils.isEmpty(groups)) return false;
        Iterator<GroupData> i = groups.iterator();
        GroupData g;
        while (i.hasNext()) {
            g = i.next();
            if (GroupData.USER.equals(g.getName())) return true;
        }
        return false;
    }

    /**
     * Overridden to return the id of the default group.
     * @see DataObject#getGroupId()
     */
    public long getGroupId() {
        GroupData g = getDefaultGroup();
        if (g == null) return -1;
        return g.getId();
    }

    /**
     * Checks if supplied group id matches any group to which the current
     * experimenter belongs to.
     * @param groupId The id of the group.
     * @return boolean <code>true</code>/<code>false</code> depending if matching
     *                id found
     */
    public boolean isMemberOfGroup(long groupId) {
        for (GroupData group : this.getGroups()) {
            if (group.getId() == groupId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the user is connected via LDAP.
     *
     * @return See above.
     */
    public boolean isLDAP()
    {
        RBool ldap = asExperimenter().getLdap();
        if (ldap == null) return false;
        return ldap.getValue();
    }

}
