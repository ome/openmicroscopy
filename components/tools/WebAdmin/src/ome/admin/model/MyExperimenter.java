package ome.admin.model;

import java.util.List;

import ome.model.meta.Experimenter;

public class MyExperimenter extends Experimenter{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Optional field for lists. Used on:
	 *  -importing users from file
	 */
	private boolean selectBooleanCheckboxValue;
	
	/**
	 * Experimenter is in admin role (added to SYSTEM group)
	 */
	private boolean selectAdminCheckboxValue;
	
	/**
	 * Experimenter is active user (added to USER group)
	 */
	private boolean selectUserCheckboxValue;
	
	/**
	 * 
	 */
    private List<String> selectRoleValues;


    /**
     * 
     * @return
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
	 * @return
	 */
	public boolean isSelectAdminCheckboxValue() {
		return selectAdminCheckboxValue;
	}

	/**
	 * 
	 * @param selectAdminCheckboxValue
	 */
	public void setSelectAdminCheckboxValue(boolean selectAdminCheckboxValue) {
		this.selectAdminCheckboxValue = selectAdminCheckboxValue;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSelectUserCheckboxValue() {
		return selectUserCheckboxValue;
	}

	/**
	 * 
	 * @param selectUserCheckboxValue
	 */
	public void setSelectUserCheckboxValue(boolean selectUserCheckboxValue) {
		this.selectUserCheckboxValue = selectUserCheckboxValue;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<String> getSelectRoleValues() {
		return selectRoleValues;
	}

	/**
	 * 
	 * @param selectRoleValues
	 */
	public void setSelectRoleValues(List<String> selectRoleValues) {
		this.selectRoleValues = selectRoleValues;
	}

}
