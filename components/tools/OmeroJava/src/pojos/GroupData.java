/*
 * pojos.Experimenter
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package pojos;


//Java imports
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import static omero.rtypes.*;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.GroupExperimenterMap;

/**
 * The data that makes up an <i>OME</i> Group along with the various members of
 * the Group
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OME2.2
 */
public class GroupData extends DataObject {

    /** Identifies the {@link ExperimenterGroup#NAME} field. */
    public final static String NAME = ExperimenterGroupI.NAME;

    /** Identifies the {@link ExperimenterGroup#DESCRIPTION} field. */
    public final static String DESCRIPTION = ExperimenterGroupI.DESCRIPTION;

    /** Identifies the {@link ExperimenterGroup#GROUPEXPERIMENTERMAP} field. */
    public final static String GROUP_EXPERIMENTER_MAP = 
    	ExperimenterGroupI.GROUPEXPERIMENTERMAP;

    /** Identifies the <code>User</code> group. */
    public static final String	USER = "user";
    
	/** Identifies the <code>System</code> group. */
	public static final String	SYSTEM = "system";
	
	/** Identifies the <code>Guest</code> group. */
	public static final String  GUEST = "guest";
	
	/** Identifies the <code>default</code> group. */
	public static final String	DEFAULT = "default";
    
    /** All experimenters in this group */
    private Set experimenters;

    /** The leaders of the group. */
    private Set leaders;
    
    /** Creates a new instance. */
    public GroupData() {
        setDirty(true);
        setValue(new ExperimenterGroupI());
    }

    /**
     * Creates a new instance.
     * 
     * @param group
     *            Back pointer to the {@link ExperimenterGroup} model object.
     *            Mustn't be <code>null</code>.
     * @throws IllegalArgumentException
     *             If the object is <code>null</code>.
     */
    public GroupData(ExperimenterGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("Annotation cannot null.");
        }
        setValue(group);
    }

    // Immutables
    /**
     * Returns the name of the group.
     * 
     * @return See above.
     */
    public String getName() {
        omero.RString n = asGroup().getName();
        if (n == null || n.getValue() == null) {
            throw new IllegalStateException(
                    "The name should never have been be null");
        }
        return n.getValue();
    }

    /**
     * Sets the name of the group.
     * 
     * @param name
     *            The name of the group. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is <code>null</code>.
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name cannot be null.");
        }
        setDirty(true);
        asGroup().setName(rstring(name));
    }

    /**
     * Returns the description of the group.
     * 
     * @return See above.
     */
    public String getDescription() {
        omero.RString n = asGroup().getDescription();
        if (n == null || n.getValue() == null)  return "";
        return n.getValue();
    }

    /**
     * Sets the name of the group.
     * 
     * @param description
     *            The description of the group. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException
     *             If the name is <code>null</code>.
     */
    public void setDescription(String description) {
        if (description == null) return;
        setDirty(true);
        asGroup().setDescription(rstring(description));
    }
    
    // Lazy loaded links
    
    /**
     * Returns the experimenters contained in this group.
     * 
     * @return See above.
     */
    public Set getLeaders() {
        if (leaders == null
                && asGroup().sizeOfGroupExperimenterMap() >= 0) {
        	leaders = new HashSet<ExperimenterData>();
            List<GroupExperimenterMap> links = asGroup()
                    .copyGroupExperimenterMap();
            for (GroupExperimenterMap link : links) {
            	if (link.getOwner().getValue())
            		leaders.add(new ExperimenterData(link.getChild()));
            }
        }

        return leaders == null ? null : new HashSet(leaders);
    }
    
    /**
     * Returns the experimenters contained in this group.
     * 
     * @return See above.
     */
    public Set getExperimenters() {
        if (experimenters == null
                && asGroup().sizeOfGroupExperimenterMap() >= 0) {
            experimenters = new HashSet<ExperimenterData>();
            List<GroupExperimenterMap> links = asGroup()
                    .copyGroupExperimenterMap();
            for (GroupExperimenterMap link : links) {
                experimenters.add(new ExperimenterData(link.getChild()));
            }
        }

        return experimenters == null ? null : new HashSet(experimenters);
    }

    // Link mutations

    /**
     * Sets the experimenters contained in this group.
     * 
     * @param newValue
     *            The set of experimenters.
     */
    public void setExperimenters(Set<ExperimenterData> newValue) {
        Set<ExperimenterData> currentValue = getExperimenters();
        SetMutator<ExperimenterData> m = new SetMutator<ExperimenterData>(
                currentValue, newValue);

        while (m.moreDeletions()) {
            setDirty(true);
            asGroup().unlinkExperimenter(m.nextDeletion().asExperimenter());
        }

        while (m.moreAdditions()) {
            setDirty(true);
            asGroup().linkExperimenter(m.nextAddition().asExperimenter());
        }

        experimenters = new HashSet<ExperimenterData>(m.result());
    }

}
