/*
* ome.admin.controller
*
*   Copyright 2007 University of Dundee. All rights reserved.
*   Use is subject to license terms supplied in LICENSE.txt
*/

package ome.admin.controller;

// Java imports
import ome.admin.logic.IAdminGroupManagerDelegate;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import ome.model.meta.ExperimenterGroup;

// Third-party libraries

// Application-internal dependencies

/**
 * It's the Java bean with six attributes and setter/getter and actions methods. The bean captures login params entered by a user after the user clicks the submit button. This way the bean provides a bridge between the JSP page and the application logic.
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class IAdminGroupController implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    /**
     * {@link ome.model.meta.ExperimenterGroup}
     */
	private ExperimenterGroup group;

    /**
     * {@link javax.faces.model.ListDataModel} The data collection wrapped by this {@link javax.faces.model.DataModel}
     */
	private DataModel groupModel = new ListDataModel();

    /**
     * {@link ome.admin.logic.IAdminGroupManagerDelegate}
     */
	private IAdminGroupManagerDelegate iadmin = new IAdminGroupManagerDelegate();

    /**
     * boolean value for providing Add/Edit form in one JSP.
     */
	private boolean editMode = false;

    /**
     * {@link java.lang.String} default value "name"
     */
	private String sortItem = "name";

    /**
     * {@link java.lang.String} sorting attribute default value is "asc"
     */
	private String sort = "asc";

    /**
     * Creates a new instance of IAdminGroupController
     */
	public IAdminGroupController() {
		this.groupModel.setWrappedData(iadmin.sortItems(sortItem, sort));
	}

    /**
     * Gets size the data collection wrapped by this {@link javax.faces.model.DataModel}
     * @return int
     */
	public int getSize() {
		return groupModel.getRowCount();
	}

    /**
     * Sets {@link ome.admin.controller.IAdminGroupController#group}
     * @param exg {@link ome.model.meta.ExperimenterGroup}
     */
	public void setGroup(ExperimenterGroup exg) {
		this.group = exg;
	}

    /**
     * Gets {@link ome.model.meta.ExperimenterGroup}
     * @return {@link ome.admin.controller.IAdminGroupController#group}
     */
	public ExperimenterGroup getGroup() {
		return this.group;
	}

    /**
     * Gets data collection of {@link ome.model.meta.ExperimenterGroup} wrapped by this {@link javax.faces.model.DataModel}
     * @return {@link javax.faces.model.DataModel}
     */
	public DataModel getGroups() {
		return this.groupModel;
	}

    /**
     * Sets editMode
     * @param em boolean
     */
	public void setEditMode(boolean em) {
		this.editMode = em;
	}

    /**
     * Check value of editMode
     * @return boolean
     */
	public boolean isEditMode() {
		return editMode;
	}

    /**
     * Provides action for navigation rule "addNewGroup" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success"
     */
	public String addNewGroup() {
		this.editMode = false;
		this.group = new ExperimenterGroup();
		return "success";
	}

    /**
     * Provides action for navigation rule "addGroup" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success"
     */
	public String addGroup() {
		iadmin.addGroup(this.group);
		this.groupModel.setWrappedData(iadmin.sortItems(sortItem, sort));
		return "success";
	}

    /**
     * Provides action for navigation rule "editGroup" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success" or "false"
     */
	public String editGroup() {
		try {
			this.editMode = true;
			this.group = (ExperimenterGroup) groupModel.getRowData();
			this.group = (ExperimenterGroup) iadmin.getGroupById(this.group
					.getId());
			return "success";
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("You cannot edit group");
			context.addMessage("groups", message);
			return "false";
		}
	}

    /**
     * Provides action for navigation rule "deleteGroup" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success" or "false"
     */
	public String delGroup() {
		try {
			this.editMode = true;
			this.group = (ExperimenterGroup) groupModel.getRowData();
			this.group = (ExperimenterGroup) iadmin.getGroupById(this.group
					.getId());
			iadmin.deleteGroup(this.group.getId());
			this.groupModel.setWrappedData(iadmin.sortItems(sortItem, sort));
			return "success";
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage(
					"You cannot delete this group");
			context.addMessage("groups", message);
			return "false";
		}
	}
        
    /**
     * Provides action for navigation rule "updateGroup" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success" or "false"
     */
	public String updateGroup() {
		try {
			this.editMode = false;
			iadmin.updateGroup(this.group);
			this.groupModel.setWrappedData(iadmin.sortItems(sortItem, sort));
			return "success";
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage(
					"You cannot update this group");
			context.addMessage("groupForm", message);
			return "false";
		}
	}        

    /**
     * Get Attribute from {@link javax.faces.component.UIComponent}
     * @param event {@link javax.faces.event.ActionEvent} object from the specified source component and action command.
     * @param name {@link java.lang.String}
     * @return {@link java.lang.String}
     */
	private static String getAttribute(ActionEvent event, String name) {
		return (String) event.getComponent().getAttributes().get(name);
	}

    /**
     * Sort items on the data collection of {@link ome.model.meta.ExperimenterGroup} wrapped by this {@link javax.faces.model.DataModel}
     * @param event {@link javax.faces.event.ActionEvent} object from the specified source component and action command.
     * @return {@link ome.admin.controller.IAdminGroupController}
     */
	public String sortItems(ActionEvent event) {
		this.sortItem = getAttribute(event, "sortItem");
		this.sort = getAttribute(event, "sort");
		this.groupModel.setWrappedData(iadmin.sortItems(sortItem, sort));
		this.editMode = false;
		return this.sort;
	}


}
