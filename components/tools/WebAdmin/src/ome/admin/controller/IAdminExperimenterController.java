/*
* ome.admin.controller
*
*   Copyright 2007 University of Dundee. All rights reserved.
*   Use is subject to license terms supplied in LICENSE.txt
*/

package ome.admin.controller;

// Java imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.component.UIInput;
import javax.faces.component.UIComponent;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.event.ActionEvent;

// Third-party libraries

// Application-internal dependencies
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.admin.logic.IAdminExperimenterManagerDelegate;

/**
 * It's the Java bean with attributes and setter/getter and actions methods. The bean captures login params entered by a user after the user clicks the submit button. This way the bean provides a bridge between the JSP page and the application logic.
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class IAdminExperimenterController implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    /**
     * {@link ome.model.meta.Experimenter}.
     */
	private Experimenter experimenter;

    /**
     * {@link javax.faces.model.ListDataModel} The data collection wrapped by this {@link javax.faces.model.DataModel}.
     */
	private DataModel experimenterModel = new ListDataModel();

    /**
     * {@link ome.admin.logic.IAdminExperimenterManagerDelegate}.
     */
	private IAdminExperimenterManagerDelegate iadmin = new IAdminExperimenterManagerDelegate();

    /**
     * boolean value for providing Add/Edit form in one JSP.
     */
	private boolean editMode = false;

    /**
     * Sort item default value "lastName".
     */
	private String sortItem = "lastName";

    /**
     * Sort attribute default value is "asc".
     */
	private String sort = "asc";

    /**
     * {@link ome.model.meta.Experimenter#getDefaultGroup().getId()}.
     */
	private Long defaultGroup = -1L;

    /**
     * {@link java.util.List}<{@link java.lang.String}> List of {@link ome.connection.ConnectionDB#containedGroups()} for chosen {@link ome.model.meta.Experimenter#getId()}
     */
	private List<String> selectedGroup = Collections.EMPTY_LIST;

    /**
     * boolean Provides Admin role in database, which gives admin permission.
     */
	private boolean adminRole = false;

    /**
     * boolean Provides User role in database, which gives login permission.
     */
	private boolean userRole = false;

    /**
     * String not-null. Might must pass validation in the security sub-system.
     */
	private String password;

    /**
     * String not-null. Might must pass validation in the security sub-system. Confirmation of password.
     */
	private String password2;

    /** 
     * Creates a new instance of IAdminExperimenterController.
     */
	public IAdminExperimenterController() {
		this.experimenterModel.setWrappedData(iadmin.sortItems(sortItem, sort));
	}

    /**
     * Gets attribute from {@link javax.faces.component.UIComponent}.
     * @param event {@link javax.faces.event.ActionEvent} object from the specified source component and action command.
     * @param name name of attribute as {@link java.lang.String}.
     * @return {@link java.lang.String}
     */
	private static String getAttribute(ActionEvent event, String name) {
		return (String) event.getComponent().getAttributes().get(name);
	}

    /**
     * Sorts items on the data collection of {@link ome.model.meta.ExperimenterGroup} wrapped by this {@link javax.faces.model.DataModel}.
     * @param event {@link javax.faces.event.ActionEvent} object from the specified source component and action command.
     * @return {@link ome.admin.controller.IAdminExperimenterController#sort}
     */
	public String sortItems(ActionEvent event) {
		this.sortItem = getAttribute(event, "sortItem");
		this.sort = getAttribute(event, "sort");
		this.experimenterModel.setWrappedData(iadmin.sortItems(sortItem, sort));
		this.editMode = false;
		return this.sort;
	}

    /**
     * Provides action for navigation rule "changePassword" what is described in the faces-config.xml file. Changes the password for {@link ome.model.meta.Experimenter}.
     * @return String "success" or "false".
     */
	public String changePassword() {
		try {
			this.experimenter = (Experimenter) this.iadmin
					.getExperimenterById(this.experimenter.getId());
			this.editMode = true;
			return "success";
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage(
					"You cannot change password");
			context.addMessage("experimenterForm", message);
			return "false";
		}
	}

    /**
     * Provides action for navigation rule "updatePassword" what is described in the faces-config.xml file. Updates the password for {@link ome.model.meta.Experimenter}.
     * @return String "success" or "false".
     */
	public String updatePassword() {
		try {
			if (!password2.equals(this.password)) {
				FacesContext context = FacesContext.getCurrentInstance();
				FacesMessage message = new FacesMessage(
						"Confirmation has to be the same as password.");
				context.addMessage("changePassword", message);
				return "false";
			} else {
				iadmin.changePassword(this.experimenter.getOmeName(),
						this.password);
				return "success";
			}
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage(
					"You cannot update password");
			context.addMessage("experimenterForm", message);
			return "false";
		}
	}

    /**
     * Sets {@link ome.admin.controller.IAdminExperimenterController#password}.
     * @param password String not-null. Might must pass validation in the security sub-system.
     */
	public void setPassword(String password) {
		this.password = password;
	}

    /**
     * Gets {@link ome.admin.controller.IAdminExperimenterController#password}.
     * @return String not-null. Might must pass validation in the security sub-system.
     */
	public String getPassword() {
		return this.password;
	}

    /**
     * Sets confirmation of password {@link ome.admin.controller.IAdminExperimenterController#password2}
     * @param password2 String not-null. Might must pass validation in the security sub-system. Confirmation of password.
     */
	public void setPassword2(String password2) {
		this.password2 = password2;
	}

    /**
     * Gets confirmation of password {@link ome.admin.controller.IAdminExperimenterController#password2}
     * @return String not-null. Might must pass validation in the security sub-system. Confirmation of password.
     */
	public String getPassword2() {
		return this.password2;
	}

    /**
     * Sets {@link ome.admin.controller.IAdminExperimenterController#defaultGroup}
     * @param id Long not-null.
     */
	public void setDefaultGroup(Long id) {
		this.defaultGroup = id;
	}

    /**
     * Gets {@link ome.model.meta.ExperimenterGroup#getId()} for choosing {@link ome.model.meta.Experimenter}
     * @return Long not-null.
     */
	public Long getDefaultGroup() {
		try {
			if (this.editMode) {
				ExperimenterGroup exg = iadmin
						.getDefaultGroup(this.experimenter.getId());
				this.defaultGroup = exg.getId();
			}
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage(
					"You cannot get default group");
			context.addMessage("experimenterForm", message);
		}
		return this.defaultGroup;
	}

    /**
     * Gets default wrapped {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup}
     * @return {@link java.util.List} never-null.
     */
	public List<SelectItem> getdefaultGroups() {
		List<ExperimenterGroup> groups = iadmin.getGroupsAdd();
		return wrapAsGUIList(groups);
	}

    /**
     * Wraps original {@link java.util.List} as GUI List {@link javax.faces.model.SelectItem}
     * @param originalList {@link java.util.List} never-null.
     * @return {@link java.util.ArrayList}<{@link javax.faces.model.SelectItem}>
     */
	private static synchronized List<SelectItem> wrapAsGUIList(List<ExperimenterGroup> originalList) {
		ArrayList<SelectItem> items = new ArrayList<SelectItem>(originalList
				.size());
		for (int i = 0, n = originalList.size(); i < n; i++) {
			ExperimenterGroup bean = originalList.get(i);
			SelectItem item = new SelectItem(bean.getId().toString(), bean
					.getName());
			items.add(item);
		}
		return items;
	}

    /**
     * Gets wrapped {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup}
     * @return {@link java.util.List}
     */
	public List<SelectItem> getOtherGroups() {
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		try {
			groups = iadmin.getGroups();
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage(
					"You cannot get list of groups");
			context.addMessage("experimenterForm", message);
		}
		return wrapAsGUIList(groups);
	}

    /**
     * Sets {@link ome.admin.controller.IAdminExperimenterController#selectedGroup}
     * @param groups {@link java.util.List}<{@link java.lang.String}>
     */
	public void setSelectedGroup(List<String> groups) {
		this.selectedGroup = groups;
	}

    /**
     * Gets {@link java.util.ArrayList}<{@link java.lang.String}>
     * @return {@link ome.admin.controller.IAdminExperimenterController#selectedGroup}
     */
	public List<String> getSelectedGroup() {
		if (this.editMode) {
			ExperimenterGroup[] exg = iadmin.containedGroups(this.experimenter
					.getId());
			this.selectedGroup = new ArrayList<String>();

			for (int i = 0; i < exg.length; i++) {
				ExperimenterGroup bean = (ExperimenterGroup) exg[i];
				this.selectedGroup.add(bean.getId().toString());
			}
		}
		return this.selectedGroup;
	}

    /**
     * Gets {@link ome.admin.controller.IAdminExperimenterController#adminRole}
     * @return boolean
     */
	public boolean isAdminRole() {
		try {
			if (this.editMode)
				this.adminRole = iadmin.isAdmin(this.experimenter.getId());
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("You cannot edit user "
					+ this.experimenter.getOmeName());
			context.addMessage("experimenterForm", message);
		}
		return this.adminRole;
	}

    /**
     * Sets {@link ome.admin.controller.IAdminExperimenterController#adminrole}
     * @param adminRole boolean
     */
	public void setAdminRole(boolean adminRole) {
		this.adminRole = adminRole;
	}

    /**
     * Gets {@link ome.admin.controller.IAdminExperimenterController#userRole}
     * @return boolean
     */
	public boolean isUserRole() {
		try {
			if (this.editMode)
				this.userRole = iadmin.isUser(this.experimenter.getId());
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("You cannot edit user "
					+ this.experimenter.getOmeName());
			context.addMessage("experimenterForm", message);
		}
		return this.userRole;
	}

    /**
     * Sets {@link ome.admin.controller.IAdminExperimenterController#userRole}
     * @param userRole boolean 
     */
	public void setUserRole(boolean userRole) {
		this.userRole = userRole;
	}

    /**
     * Gets size the data collection wrapped by this {@link javax.faces.model.DataModel}
     * @return int
     */
	public int getSize() {
		return experimenterModel.getRowCount();
	}

    /**
     * Sets {@link ome.admin.controller.IAdminExperimenterController#experimenter}
     * @param exp {@link ome.model.meta.Experimenter}
     */
	public void setExperimenter(Experimenter exp) {
		this.experimenter = exp;
	}

    /**
     * Gets {@link ome.admin.controller.IAdminExperimenterController#experimenter}
     * @return {@link ome.admin.controller.IAdminExperimenterController#experimenter}
     */
	public Experimenter getExperimenter() {
		return this.experimenter;
	}

    /**
     * Gets default wrapped {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup}
     * @return {@link java.util.List}
     */
	public DataModel getExperimenters() {
		return this.experimenterModel;
	}

    /**
     * Sets {@link ome.admin.controller.IAdminExperimenterController#editMode}
     * @param em boolean
     */
	public void setEditMode(boolean em) {
		this.editMode = em;
	}

    /**
     * Checks {@link ome.admin.controller.IAdminExperimenterController#editMode}
     * @return boolean
     */
	public boolean isEditMode() {
		return editMode;
	}

    /**
     * Provides action for navigation rule "addNewExperimenter" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success" or "false"
     */
	public String addNewExperimenter() {
		this.editMode = false;
		this.experimenter = new Experimenter();
		this.defaultGroup = 0L;
		this.selectedGroup = Collections.EMPTY_LIST;
		this.adminRole = false;
		return "success";
	}

    /**
     * Provides action for navigation rule "addExperimenter" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success" or "false"
     */
	public String addExperimenter() {
		iadmin.createExperimenter(this.experimenter, this.defaultGroup,
				this.selectedGroup, this.userRole, this.adminRole);
		this.experimenterModel.setWrappedData(iadmin.sortItems(sortItem, sort));
		return "success";
	}

    /**
     * Provides action for navigation rule "editExperimenter" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success" or "false"
     */
	public String editExperimenter() {
		try {
			this.editMode = true;
			this.experimenter = (Experimenter) experimenterModel.getRowData();
			this.experimenter = (Experimenter) iadmin
					.getExperimenterById(this.experimenter.getId());
			return "success";
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("You cannot edit user "
					+ this.experimenter.getOmeName());
			context.addMessage("experimenters", message);
			return "false";
		}
	}

    /**
     * Provides action for navigation rule "deleteExperimenter" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success" or "false"
     */
	public String delExperimenter() {
		try {
			this.experimenter = (Experimenter) experimenterModel.getRowData();
			this.experimenter = (Experimenter) iadmin
					.getExperimenterById(this.experimenter.getId());
			iadmin.deleteExperimenter(this.experimenter.getId());
			this.experimenterModel.setWrappedData(iadmin.sortItems(sortItem,
					sort));
			return "success";
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("You cannot delete user "
					+ this.experimenter.getOmeName());
			context.addMessage("experimenters", message);
			return "false";
		}

	}

    /**
     * Provides action for navigation rule "updateExperimenter" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success" or "false"
     */
	public String updateExperimenter() {
		try {
			iadmin.updateExperimenter(this.experimenter, this.defaultGroup,
					this.selectedGroup, this.userRole, this.adminRole);
			this.experimenterModel.setWrappedData(iadmin.sortItems(sortItem,
					sort));
			return "success";
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage(
					"You cannot change details, baceuse user is loged in");
			context.addMessage("experimenterForm", message);
			return "false";
		}

	}

    /**
     * Provides validaton for email
     * @param context {@link javax.faces.context.FacesContext}
     * @param toValidate {@link javax.faces.component.UIComponent}
     * @param value {@link java.lang.Object}
     */
	public void validateEmail(FacesContext context, UIComponent toValidate,
			Object value) {
		String email = (String) value;
		if (email.indexOf('@') == -1) {
			((UIInput) toValidate).setValid(false);
			FacesMessage message = new FacesMessage("Invalid Email");
			context.addMessage(toValidate.getClientId(context), message);
		}
		if (iadmin.checkEmail(email)
				&& !email.equals(this.experimenter.getEmail())) {
			((UIInput) toValidate).setValid(false);
			FacesMessage message = new FacesMessage("Email exist");
			context.addMessage(toValidate.getClientId(context), message);
		}
	}

    /**
     * Provides validaton for {@link ome.model.meta.Experimenter#getOmeName()}. Cannot exist the same omeNames.
     * @param context {@link javax.faces.context.FacesContext}
     * @param toValidate {@link javax.faces.component.UIComponent}
     * @param value {@link java.lang.Object}
     */
	public void validateOmeName(FacesContext context, UIComponent toValidate,
			Object value) {
		String omeName = (String) value;
		if (iadmin.checkExperimenter(omeName)
				&& !omeName.equals(this.experimenter.getOmeName())) {
			((UIInput) toValidate).setValid(false);
			FacesMessage message = new FacesMessage("New Ome name exist");
			context.addMessage(toValidate.getClientId(context), message);
		}
	}

}
