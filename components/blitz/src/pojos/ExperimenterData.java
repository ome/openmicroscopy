/*
 * pojos.Experimenter
 *
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package pojos;

//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries


//Application-internal dependencies
import static omero.rtypes.*;
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
    
    /** Identifies the {@link Experimenter#FIRSTNAME} field. */
    public final static String FIRSTNAME = ExperimenterI.FIRSTNAME;

    /** Identifies the {@link Experimenter#MIDDLENAME} field. */
    public final static String MIDDLENAME = ExperimenterI.MIDDLENAME;

    /** Identifies the {@link Experimenter#LASTNAME} field. */
    public final static String LASTNAME = ExperimenterI.LASTNAME;

    /** Identifies the {@link Experimenter#EMAIL} field. */
    public final static String EMAIL = ExperimenterI.EMAIL;

    /** Identifies the {@link Experimenter#OMENAME} field. */
    public final static String OMENAME = ExperimenterI.OMENAME;

    /** Identifies the {@link Experimenter#INSTITUTION} field. */
    public final static String INSTITUTION = ExperimenterI.INSTITUTION;

    /** Identifies the {@link Experimenter#GROUPEXPERIMENTERMAP} field. */
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
        asExperimenter().setFirstName(rstring(firstName));
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
        asExperimenter().setLastName(rstring(lastName));
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
        asExperimenter().setEmail(rstring(email));
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
                institution == null ? null : rstring(institution));
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
    	if (groups == null || groups.size() == 0) return null;
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
        asExperimenter().setMiddleName(rstring(middleName));
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
		if (groups == null || groups.size() == 0) return false;
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
     * @param long groupId
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

    @Override
    public String toString() {
        return "ExperimenterData [getUserName()=" + getUserName()
                + ", getDefaultGroup()=" + getDefaultGroup() + ", getGroups()="
                + getGroups() + "]";
    }

    
}
