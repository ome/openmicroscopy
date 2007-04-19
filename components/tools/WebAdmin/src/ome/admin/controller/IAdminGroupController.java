/*
 * IAdminGroupController.java
 *
 * Created on March 14, 2007, 10:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ome.admin.controller;

import ome.admin.logic.IAdminGroupManagerDelegate;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import ome.model.meta.ExperimenterGroup;

/**
 * 
 * @author Ola
 */
public class IAdminGroupController implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ExperimenterGroup group;

	private DataModel groupModel = new ListDataModel();

	private IAdminGroupManagerDelegate iadmin = new IAdminGroupManagerDelegate();

	private boolean editMode = false;

	private String sortItem = "name";

	private String sort = "asc";

	public IAdminGroupController() {
		this.groupModel.setWrappedData(iadmin.sortItems(sortItem, sort));
	}

	public int getSize() {
		return groupModel.getRowCount();
	}

	public void setGroup(ExperimenterGroup exg) {
		this.group = exg;
	}

	public ExperimenterGroup getGroup() {
		return this.group;
	}

	public DataModel getGroups() {
		return this.groupModel;
	}

	public void setEditMode(boolean em) {
		this.editMode = em;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public String addNewGroup() {
		this.editMode = false;
		this.group = new ExperimenterGroup();
		return "success";
	}

	public String addGroup() {
		iadmin.addGroup(this.group);
		this.groupModel.setWrappedData(iadmin.sortItems(sortItem, sort));
		return "success";
	}

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

	private static String getAttribute(ActionEvent event, String name) {
		return (String) event.getComponent().getAttributes().get(name);
	}

	public String sortItems(ActionEvent event) {
		this.sortItem = getAttribute(event, "sortItem");
		this.sort = getAttribute(event, "sort");
		this.groupModel.setWrappedData(iadmin.sortItems(sortItem, sort));
		this.editMode = false;
		return this.sort;
	}

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

}
