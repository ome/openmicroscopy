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
import javax.faces.model.SelectItem;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

// Third-party libraries

// Application-internal dependencies
import ome.admin.data.ConnectionDB;
import ome.admin.logic.IAdminGroupManagerDelegate;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

/**
 * It's the Java bean with six attributes and setter/getter and actions methods.
 * The bean captures login params entered by a user after the user clicks the
 * submit button. This way the bean provides a bridge between the JSP page and
 * the application logic.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class IAdminGroupController implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * log4j logger
	 */
	static Logger logger = Logger.getLogger(ConnectionDB.class.getName());

	/**
	 * {@link ome.model.meta.ExperimenterGroup}
	 */
	private ExperimenterGroup group;

	/**
	 * {@link javax.faces.model.ListDataModel} The data collection wrapped by
	 * this {@link javax.faces.model.DataModel}
	 */
	private DataModel groupModel = new ListDataModel();

	/**
	 * {@link ome.admin.logic.IAdminGroupManagerDelegate}
	 */
	private IAdminGroupManagerDelegate iadmin = new IAdminGroupManagerDelegate();

	/**
	 * {@link java.util.List}<{@link java.lang.String}> List of
	 * {@link ome.admin.data.ConnectionDB#lookupExperimenters()} for chosen
	 * {@link ome.model.meta.ExperimenterGroup#getId()}
	 */
	private List<Experimenter> experimenters = Collections.EMPTY_LIST;

	/**
	 * {@link java.util.List}<{@link java.lang.String}> List of
	 * {@link ome.admin.data.ConnectionDB#containedGroups()} for chosen
	 * {@link ome.model.meta.ExperimenterGroup#getId()}
	 */
	private List<String> selectedExperimenters = Collections.EMPTY_LIST;

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
	 * Gets size the data collection wrapped by this
	 * {@link javax.faces.model.DataModel}
	 * 
	 * @return int
	 */
	public int getSize() {
		return groupModel.getRowCount();
	}

	/**
	 * Sets {@link ome.admin.controller.IAdminGroupController#group}
	 * 
	 * @param exg
	 *            {@link ome.model.meta.ExperimenterGroup}
	 */
	public void setGroup(ExperimenterGroup exg) {
		this.group = exg;
	}

	/**
	 * Gets {@link ome.model.meta.ExperimenterGroup}
	 * 
	 * @return {@link ome.admin.controller.IAdminGroupController#group}
	 */
	public ExperimenterGroup getGroup() {
		return this.group;
	}

	/**
	 * Gets data collection of {@link ome.model.meta.ExperimenterGroup} wrapped
	 * by this {@link javax.faces.model.DataModel}
	 * 
	 * @return {@link javax.faces.model.DataModel}
	 */
	public DataModel getGroups() {
		return this.groupModel;
	}

	/**
	 * Sets editMode
	 * 
	 * @param em
	 *            boolean
	 */
	public void setEditMode(boolean em) {
		this.editMode = em;
	}

	/**
	 * Check value of editMode
	 * 
	 * @return boolean
	 */
	public boolean isEditMode() {
		return editMode;
	}

	/**
	 * Provides action for navigation rule "addNewGroup" what is described in
	 * the faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success"
	 */
	public String addNewGroup() {
		this.editMode = false;
		this.group = new ExperimenterGroup();
		return "success";
	}

	/**
	 * Provides action for navigation rule "addGroup" what is described in the
	 * faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success"
	 */
	public String addGroup() {
		try {
			iadmin.addGroup(this.group);
			this.groupModel.setWrappedData(iadmin.sortItems(sortItem, sort));
			return "success";
		} catch (Exception e) {
			logger.error("addGroup: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Group: [id: "
					+ this.group.getId() + ", '" + this.group.getName()
					+ "'] : " + e.getMessage());
			context.addMessage("changePassword", message);
			return "false";
		}
	}

	/**
	 * Provides action for navigation rule "editGroup" what is described in the
	 * faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success" or "false"
	 */
	public String editGroup() {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			this.group = (ExperimenterGroup) groupModel.getRowData();
			this.group = (ExperimenterGroup) iadmin.getGroupById(this.group
					.getId());
			if (!checkGroup(this.group.getName())) {
				this.editMode = true;
				return "success";
			} else {
				this.editMode = false;
				FacesMessage message = new FacesMessage(
						"Cannot edit group [name '" + this.group.getName()
								+ "']. This is system group.");
				context.addMessage("groups", message);
				return "false";
			}
		} catch (Exception e) {
			logger.error("editGroup: " + e.getMessage());
			FacesMessage message = new FacesMessage("Group: [id: "
					+ this.group.getId() + ", '" + this.group.getName()
					+ "'] : " + e.getMessage());
			context.addMessage("groups", message);
			return "false";
		}
	}

	private boolean checkGroup(String name) {
		if (name.equals("system"))
			return true;
		if (name.equals("default"))
			return true;
		return false;
	}

	/**
	 * Provides action for navigation rule "deleteGroup" what is described in
	 * the faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success" or "false"
	 */
	public String delGroup() {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			this.group = (ExperimenterGroup) groupModel.getRowData();
			this.group = (ExperimenterGroup) iadmin.getGroupById(this.group
					.getId());
			if (!checkGroup(this.group.getName())) {
				this.editMode = true;
				iadmin.deleteGroup(this.group.getId());
				this.groupModel
						.setWrappedData(iadmin.sortItems(sortItem, sort));
				return "success";
			}
			FacesMessage message = new FacesMessage(
					"Cannot delete group [name '" + this.group.getName()
							+ "']. This is system group.");
			context.addMessage("groups", message);
			return "false";
		} catch (Exception e) {
			logger.error("delGroup: " + e.getMessage());
			FacesMessage message = new FacesMessage("Group: [id: "
					+ this.group.getId() + ", '" + this.group.getName()
					+ "'] : " + e.getMessage());
			context.addMessage("groups", message);
			return "false";
		}
	}

	/**
	 * Provides action for navigation rule "updateGroup" what is described in
	 * the faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success" or "false"
	 */
	public String updateGroup() {
		try {
			this.editMode = false;
			iadmin.updateGroup(this.group);
			this.groupModel.setWrappedData(iadmin.sortItems(sortItem, sort));
			return "success";
		} catch (Exception e) {
			logger.error("updateGroup: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Group: [id: "
					+ this.group.getId() + ", '" + this.group.getName()
					+ "'] : " + e.getMessage());
			context.addMessage("groupForm", message);
			return "false";
		}
	}

	/**
	 * Gets {@link java.util.List} of {@link ome.model.meta.Experimenter#getId}
	 * who was selected on
	 * {@link ome.admin.controller.IAdminGroupController#group}
	 * 
	 * @return {@link java.util.List} never-null.
	 */
	public List<String> getSelectedExperimenters() {
		try {
			this.selectedExperimenters = iadmin
					.containedExperimenters(this.group.getId());
		} catch (Exception e) {
			logger.error("getSelectedExperimenters: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Group: [id: "
					+ this.group.getId() + ", '" + this.group.getName()
					+ "'] : " + e.getMessage());
			context.addMessage("group", message);
		}
		return this.selectedExperimenters;
	}

	/**
	 * Sets {@link java.util.List} of {@link ome.model.meta.Experimenter#getId}
	 * who now is selected on
	 * {@link ome.admin.controller.IAdminGroupController#group}
	 * 
	 * @param {@link java.util.List}
	 *            never-null.
	 */
	public void setSelectedExperimenters(List<String> exps) {
		this.selectedExperimenters = exps;
	}

	/**
	 * Gets default wrapped {@link java.util.List} of
	 * {@link ome.model.meta.ExperimenterGroup}
	 * 
	 * @return {@link java.util.List} never-null.
	 */
	public List<SelectItem> getExperimenters() {
		try {
			this.experimenters = iadmin.lookupExperimeters(this.group.getId());
		} catch (Exception e) {
			logger.error("getExperimenters: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Group: [id: "
					+ this.group.getId() + ", '" + this.group.getName()
					+ "'] : " + e.getMessage());
			context.addMessage("group", message);
		}
		return wrapAsGUIList(this.experimenters);
	}

	/**
	 * Wraps original {@link java.util.List} as GUI List
	 * {@link javax.faces.model.SelectItem}
	 * 
	 * @param originalList
	 *            {@link java.util.List} never-null.
	 * @return {@link java.util.ArrayList}<{@link javax.faces.model.SelectItem}>
	 */
	private static synchronized List<SelectItem> wrapAsGUIList(
			List<Experimenter> originalList) {
		ArrayList<SelectItem> items = new ArrayList<SelectItem>(originalList
				.size());
		for (int i = 0, n = originalList.size(); i < n; i++) {
			Experimenter bean = originalList.get(i);
			SelectItem item = new SelectItem(bean.getId().toString(), bean
					.getOmeName());
			items.add(item);
		}
		return items;
	}

	/**
	 * Provides action for navigation rule "editInGroup" what is described in
	 * the faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success" or "false"
	 */
	public String editInGroup() {
		try {
			this.editMode = false;
			this.group = (ExperimenterGroup) groupModel.getRowData();
			this.group = (ExperimenterGroup) iadmin.getGroupById(this.group
					.getId());

			return "success";
		} catch (Exception e) {
			logger.error("editInGroup: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Group: [id: "
					+ this.group.getId() + ", '" + this.group.getName()
					+ "'] : " + e.getMessage());
			context.addMessage("groups", message);
			return "false";
		}
	}

	/**
	 * Provides action for navigation rule "saveInGroup" what is described in
	 * the faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success" or "false"
	 */
	public String saveInToGroup() {
		try {
			iadmin.updateExperimenters(this.selectedExperimenters, this.group
					.getId());
			return "success";
		} catch (Exception e) {
			logger.error("SaveInToGroup: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Group: [id: "
					+ this.group.getId() + ", '" + this.group.getName()
					+ "'] : " + e.getMessage());
			context.addMessage("groups", message);
			return "false";
		}
	}

	/**
	 * Get Attribute from {@link javax.faces.component.UIComponent}
	 * 
	 * @param event
	 *            {@link javax.faces.event.ActionEvent} object from the
	 *            specified source component and action command.
	 * @param name
	 *            {@link java.lang.String}
	 * @return {@link java.lang.String}
	 */
	private static String getAttribute(ActionEvent event, String name) {
		return (String) event.getComponent().getAttributes().get(name);
	}

	/**
	 * Sort items on the data collection of
	 * {@link ome.model.meta.ExperimenterGroup} wrapped by this
	 * {@link javax.faces.model.DataModel}
	 * 
	 * @param event
	 *            {@link javax.faces.event.ActionEvent} object from the
	 *            specified source component and action command.
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
