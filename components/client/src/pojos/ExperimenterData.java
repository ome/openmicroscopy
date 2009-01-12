/*
 * pojos.Experimenter
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package pojos;

// Java imports
import java.util.ArrayList;
import java.util.List;

// Third-party libraries

// Application-internal dependencies
import ome.model.IObject;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.util.CBlock;

/**
 * The data that makes up an <i>OME</i> Experimenter along with information
 * about the Group the Experimenter belongs in.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date$)
 *          </small>
 * @since OME2.2
 */
public class ExperimenterData extends DataObject {

    /** Identifies the {@link Experimenter#FIRSTNAME} field. */
    public final static String FIRSTNAME = Experimenter.FIRSTNAME;

    /** Identifies the {@link Experimenter#MIDDLENAME} field. */
    public final static String MIDDLENAME = Experimenter.MIDDLENAME;

    /** Identifies the {@link Experimenter#LASTNAME} field. */
    public final static String LASTNAME = Experimenter.LASTNAME;

    /** Identifies the {@link Experimenter#EMAIL} field. */
    public final static String EMAIL = Experimenter.EMAIL;

    /** Identifies the {@link Experimenter#OMENAME} field. */
    public final static String OMENAME = Experimenter.OMENAME;

    /** Identifies the {@link Experimenter#INSTITUTION} field. */
    public final static String INSTITUTION = Experimenter.INSTITUTION;

    /** Identifies the {@link Experimenter#GROUPEXPERIMENTERMAP} field. */
    public final static String GROUP_EXPERIMENTER_MAP = Experimenter.GROUPEXPERIMENTERMAP;

    /** The other Groups this Experimenter belongs in. */
    private List<GroupData> groups;

    /** Creates a new instance. */
    public ExperimenterData() {
        setDirty(true);
        setValue(new Experimenter());
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
        asExperimenter().setFirstName(firstName);
    }

    /**
     * Returns the first name of the experimenter.
     * 
     * @return see above.
     */
    public String getFirstName() {
        return asExperimenter().getFirstName();
    }

    /**
     * Sets the last name of the experimenter.
     * 
     * @param lastName
     *            The value to set.
     */
    public void setLastName(String lastName) {
        setDirty(true);
        asExperimenter().setLastName(lastName);
    }

    /**
     * Returns the last name of the experimenter.
     * 
     * @return see above.
     */
    public String getLastName() {
        return asExperimenter().getLastName();
    }

    /**
     * Sets the e-mail of the experimenter.
     * 
     * @param email
     *            The value to set.
     */
    public void setEmail(String email) {
        setDirty(true);
        asExperimenter().setEmail(email);
    }

    /**
     * Returns the e-mail of the experimenter.
     * 
     * @return see above.
     */
    public String getEmail() {
        return asExperimenter().getEmail();
    }

    /**
     * Sets the institution where the experimenter works.
     * 
     * @param institution
     *            The value to set.
     */
    public void setInstitution(String institution) {
        setDirty(true);
        asExperimenter().setInstitution(institution);
    }

    /**
     * Returns the institution where the experimenter works.
     * 
     * @return see above.
     */
    public String getInstitution() {
        return asExperimenter().getInstitution();
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
            groups = asExperimenter().eachLinkedExperimenterGroup(
                    new CBlock() {
                        public Object call(IObject object) {
                            return new GroupData((ExperimenterGroup) object);
                        }
                    });
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
        SetMutator<GroupData> m = new SetMutator<GroupData>(currentValue, newValue);

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
        return getGroups().get(0);
    }

}
