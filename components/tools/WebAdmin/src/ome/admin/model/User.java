package ome.admin.model;

import java.util.Collections;
import java.util.List;

import ome.model.meta.Experimenter;

public class User {

	private Experimenter experimenter = new Experimenter();
	
	private String defaultGroup = "-1";
	
	private List<String> selectedGroups = Collections.EMPTY_LIST;
	
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
	 * Optional field for lists. Used on:
	 *  -importing users from file
	 */
	private boolean selectBooleanCheckboxValue;
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isAdminRole() {
		return adminRole;
	}

	/**
	 * 
	 * @param adminRole
	 */
	public void setAdminRole(boolean adminRole) {
		this.adminRole = adminRole;
	}

	/**
	 * 
	 * @return String
	 */
	public String getDefaultGroup() {
		return defaultGroup;
	}

	/**
	 * 
	 * @param defaultGroup {@link ome.model.meta.ExperimenterGroup}
	 */
	public void setDefaultGroup(String defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	/**
	 * 
	 * @return {@link ome.model.meta.Experimenter}
	 */
	public Experimenter getExperimenter() {
		return experimenter;
	}

	/**
	 * 
	 * @param experimenter {@link ome.model.meta.Experimenter}
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
	 * 
	 * @return List<String>
	 */
	public List<String> getSelectedGroups() {
		return selectedGroups;
	}

	/**
	 * 
	 * @param selectedGroups List<String>
	 */
	public void setSelectedGroups(List<String> selectedGroups) {
		this.selectedGroups = selectedGroups;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isUserRole() {
		return userRole;
	}

	/**
	 * 
	 * @param userRole
	 */
	public void setUserRole(boolean userRole) {
		this.userRole = userRole;
	}

	/**
	 * 
	 * @return
	 */
	public String getDn() {
		return dn;
	}

	/**
	 * 
	 * @param dn
	 */
	public void setDn(String dn) {
		this.dn = dn;
	}


}
