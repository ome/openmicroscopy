/*
 * IAdminExperimenterController.java
 *
 * Created on March 14, 2007, 10:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ome.admin.controller;

import ome.admin.logic.IAdminExperimenterManagerDelegate;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.component.UIInput;
import javax.faces.component.UIComponent;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.event.ActionEvent;

import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author Ola
 */
public class IAdminExperimenterController implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Experimenter experimenter;

	private DataModel experimenterModel = new ListDataModel();

	private IAdminExperimenterManagerDelegate iadmin = new IAdminExperimenterManagerDelegate();

	private boolean editMode = false;

	private String sortItem = "lastName";

	private String sort = "asc";

	private Long defaultGroup = -1L;

	private List<String> selectedGroup = Collections.EMPTY_LIST;

	private boolean adminRole = false;

	private boolean userRole = false;

	private String password;

	private String password2;

	/** Creates a new instance of IAdminExperimenterController */
	public IAdminExperimenterController() {
		this.experimenterModel.setWrappedData(iadmin.sortItems(sortItem, sort));
	}

	private static String getAttribute(ActionEvent event, String name) {
		return (String) event.getComponent().getAttributes().get(name);
	}

	public String sortItems(ActionEvent event) {
		this.sortItem = getAttribute(event, "sortItem");
		this.sort = getAttribute(event, "sort");
		this.experimenterModel.setWrappedData(iadmin.sortItems(sortItem, sort));
		this.editMode = false;
		return this.sort;
	}

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

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword2(String password2) {
		this.password2 = password2;
	}

	public String getPassword2() {
		return this.password2;
	}

	public void setDefaultGroup(Long id) {
		this.defaultGroup = id;
	}

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

	public List getdefaultGroups() {
		List<ExperimenterGroup> groups = iadmin.getGroupsAdd();
		return wrapAsGUIList(groups);
	}

	private static synchronized List wrapAsGUIList(List originalList) {
		ArrayList<SelectItem> items = new ArrayList<SelectItem>(originalList
				.size());
		for (int i = 0, n = originalList.size(); i < n; i++) {
			ExperimenterGroup bean = (ExperimenterGroup) originalList.get(i);
			SelectItem item = new SelectItem(bean.getId().toString(), bean
					.getName());
			items.add(item);
		}
		return items;
	}

	public List getOtherGroups() {
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

	public void setSelectedGroup(List groups) {
		selectedGroup = groups;
	}

	public List getSelectedGroup() {
		if (this.editMode) {
			ExperimenterGroup[] exg = iadmin.containedGroups(this.experimenter
					.getId());
			selectedGroup = new ArrayList<String>();

			for (int i = 0; i < exg.length; i++) {
				ExperimenterGroup bean = (ExperimenterGroup) exg[i];
				selectedGroup.add(bean.getId().toString());
			}
		}
		return selectedGroup;
	}

	public boolean getAdminRole() {
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

	public void setAdminRole(boolean adminRole) {
		this.adminRole = adminRole;
	}

	public boolean getUserRole() {
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

	public void setUserRole(boolean userRole) {
		this.userRole = userRole;
	}

	public int getSize() {
		return experimenterModel.getRowCount();
	}

	public void setExperimenter(Experimenter exp) {
		this.experimenter = exp;
	}

	public Experimenter getExperimenter() {
		return this.experimenter;
	}

	public DataModel getExperimenters() {
		return this.experimenterModel;
	}

	public void setEditMode(boolean em) {
		this.editMode = em;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public String addNewExperimenter() {
		this.editMode = false;
		this.experimenter = new Experimenter();
		this.defaultGroup = 0L;
		this.selectedGroup = Collections.EMPTY_LIST;
		this.adminRole = false;
		return "success";
	}

	public String addExperimenter() {
		iadmin.createExperimenter(this.experimenter, this.defaultGroup,
				this.selectedGroup, this.userRole, this.adminRole);
		this.experimenterModel.setWrappedData(iadmin.sortItems(sortItem, sort));
		return "success";
	}

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
