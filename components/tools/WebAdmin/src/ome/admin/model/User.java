/*
 * ome.admin.model.User
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.model;

// Java imports
import java.util.Collections;
import java.util.List;

// Application-internal dependencies
import ome.model.meta.Experimenter;

/**
 * It's model for {@link ome.admin.controller.IAdminExperimenterController}
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */

public class User {

    /**
     * {@link ome.model.meta.Experimenter}
     */
    private Experimenter experimenter = new Experimenter();

    /**
     * String provides defult group
     */
    private String defaultGroup = "-1";

    /**
     * List of Strings provides groups where user belonges to.
     */
    private List<String> selectedGroups = Collections.EMPTY_LIST;

    /**
     * String provides distinguished name from Ldap.
     */
    private String dn;

    /**
     * boolean Provides Admin role in database, which gives admin permission.
     */
    private boolean adminRole = false;

    /**
     * boolean Provides User role in database, which gives login permission.
     */
    private boolean userRole = false;

    /**
     * Optional field for lists. Used on: -importing users from file
     */
    private boolean selectBooleanCheckboxValue;

    /**
     * Checks Admin role
     * 
     * @return boolean
     */
    public boolean isAdminRole() {
        return adminRole;
    }

    /**
     * Sets Admin role
     * 
     * @param adminRole
     */
    public void setAdminRole(boolean adminRole) {
        this.adminRole = adminRole;
    }

    /**
     * Gets default group
     * 
     * @return String
     */
    public String getDefaultGroup() {
        return defaultGroup;
    }

    /**
     * Sets default group
     * 
     * @param defaultGroup
     *            {@link ome.model.meta.ExperimenterGroup}
     */
    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    /**
     * Gets object {@link ome.model.meta.Experimenter}
     * 
     * @return {@link ome.model.meta.Experimenter}
     */
    public Experimenter getExperimenter() {
        return experimenter;
    }

    /**
     * Sets object {@link ome.model.meta.Experimenter}
     * 
     * @param experimenter
     *            {@link ome.model.meta.Experimenter}
     */
    public void setExperimenter(Experimenter experimenter) {
        this.experimenter = experimenter;
    }

    /**
     * 
     * @return boolean
     */
    public boolean isSelectBooleanCheckboxValue() {
        return selectBooleanCheckboxValue;
    }

    /**
     * 
     * @param selectBooleanCheckboxValue
     */
    public void setSelectBooleanCheckboxValue(boolean selectBooleanCheckboxValue) {
        this.selectBooleanCheckboxValue = selectBooleanCheckboxValue;
    }

    /**
     * Gets selected groups
     * 
     * @return List<String>
     */
    public List<String> getSelectedGroups() {
        return selectedGroups;
    }

    /**
     * Sets selected groups
     * 
     * @param selectedGroups
     *            List<String>
     */
    public void setSelectedGroups(List<String> selectedGroups) {
        this.selectedGroups = selectedGroups;
    }

    /**
     * Checks User role
     * 
     * @return boolean
     */
    public boolean isUserRole() {
        return userRole;
    }

    /**
     * Sets user role
     * 
     * @param userRole
     */
    public void setUserRole(boolean userRole) {
        this.userRole = userRole;
    }

    /**
     * Gets distinguished name
     * 
     * @return {@link java.lang.String}
     */
    public String getDn() {
        return dn;
    }

    /**
     * Sets distinguished name
     * 
     * @param dn
     */
    public void setDn(String dn) {
        this.dn = dn;
    }

}
