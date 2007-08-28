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

//Third-party libraries
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.component.UIInput;
import javax.faces.component.UIComponent;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.event.ActionEvent;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.datascroller.ScrollerActionEvent;

// Application-internal dependencies
import ome.model.meta.ExperimenterGroup;
import ome.admin.logic.IAdminExperimenterManagerDelegate;
import ome.admin.model.User;

/**
 * It's the Java bean with attributes and setter/getter and actions methods. The
 * bean captures login params entered by a user after the user clicks the submit
 * button. This way the bean provides a bridge between the JSP page and the
 * application logic.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class IAdminExperimenterController implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * log4j logger
	 */
	static Logger logger = Logger.getLogger(IAdminExperimenterController.class.getName());

	/**
	 * {@link ome.model.meta.Experimenter}.
	 */
	private User user = new User();

	/**
	 * {@link javax.faces.model.ListDataModel} The data collection wrapped by
	 * this {@link javax.faces.model.DataModel}.
	 */
	private DataModel userModel = new ListDataModel();

	/**
	 * {@link ome.admin.logic.IAdminExperimenterManagerDelegate}.
	 */
	private IAdminExperimenterManagerDelegate iadmin = new IAdminExperimenterManagerDelegate();

	/**
	 * boolean value for providing Add/Edit form in one JSP.
	 */
	private boolean editMode = false;

	/**
	 * boolean value for providing Add/Edit form in one JSP.
	 */
	private boolean scrollerMode = true;
	
	/**
	 * Sort item default value "lastName".
	 */
	private String sortItem = "lastName";

	/**
	 * Sort attribute default value is "asc".
	 */
	private String sort = "asc";

	/**
	 * String not-null. Might must pass validation in the security sub-system.
	 */
	private String password;

	/**
	 * String not-null. Might must pass validation in the security sub-system.
	 * Confirmation of password.
	 */
	private String password2;
	
	/**
	 * Creates a new instance of IAdminExperimenterController.
	 */
	public IAdminExperimenterController() {
		this.userModel.setWrappedData(iadmin.getAndSortItems(sortItem, sort));
		this.scrollerMode = iadmin.setScroller();
	}

	/**
	 * Sets {@link ome.admin.controller.IAdminExperimenterController#password}.
	 * 
	 * @param password
	 *            String not-null. Might must pass validation in the security
	 *            sub-system.
	 */
	public void setPassword(String password) {
		if (password != null)
			this.password = password;
		else
			this.password = "";
	}

	/**
	 * Gets {@link ome.admin.controller.IAdminExperimenterController#password}.
	 * 
	 * @return String not-null. Might must pass validation in the security
	 *         sub-system.
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * Sets confirmation of password
	 * {@link ome.admin.controller.IAdminExperimenterController#password2}
	 * 
	 * @param password2
	 *            String not-null. Might must pass validation in the security
	 *            sub-system. Confirmation of password.
	 */
	public void setPassword2(String password2) {
		if (password2 != null)
			this.password2 = password2;
		else
			this.password2 = "";
	}

	/**
	 * Gets confirmation of password
	 * {@link ome.admin.controller.IAdminExperimenterController#password2}
	 * 
	 * @return String not-null. Might must pass validation in the security
	 *         sub-system. Confirmation of password.
	 */
	public String getPassword2() {
		return this.password2;
	}

	/**
	 * Gets size the data collection wrapped by this
	 * {@link javax.faces.model.DataModel}
	 * 
	 * @return int
	 */
	public int getSize() {
		return this.userModel.getRowCount();
	}
	
	public boolean isScrollerMode() {
		return this.scrollerMode;
	}

	public void setScrollerMode(boolean scrollerMode) {
		this.scrollerMode = scrollerMode;
	}

	/**
	 * Sets
	 * {@link ome.admin.controller.IAdminExperimenterController#user}
	 * 
	 * @param u
	 *            {@link ome.admin.model.User}
	 */
	public void setUser(User u) {
		this.user = u;
	}

	/**
	 * Gets
	 * {@link ome.admin.model.User}
	 * 
	 * @return {@link ome.admin.controller.IAdminExperimenterController#user}
	 */
	public User getUser() {
		return this.user;
	}

	/**
	 * Gets default wrapped {@link java.util.List} of
	 * {@link ome.model.meta.Experimenter}
	 * 
	 * @return {@link java.util.List}
	 */
	public DataModel getUsers() {
		return this.userModel;
	}

	/**
	 * Sets {@link ome.admin.controller.IAdminExperimenterController#editMode}
	 * 
	 * @param em
	 *            boolean
	 */
	public void setEditMode(boolean em) {
		this.editMode = em;
	}

	/**
	 * Checks {@link ome.admin.controller.IAdminExperimenterController#editMode}
	 * 
	 * @return boolean
	 */
	public boolean isEditMode() {
		return editMode;
	}

	/**
	 * Gets attribute from {@link javax.faces.component.UIComponent}.
	 * 
	 * @param event
	 *            {@link javax.faces.event.ActionEvent} object from the
	 *            specified source component and action command.
	 * @param name
	 *            name of attribute as {@link java.lang.String}.
	 * @return {@link java.lang.String}
	 */
	private static String getAttribute(ActionEvent event, String name) {
		return (String) event.getComponent().getAttributes().get(name);
	}

	/**
	 * Sorts items on the data collection of
	 * {@link ome.model.meta.ExperimenterGroup} wrapped by this
	 * {@link javax.faces.model.DataModel}.
	 * 
	 * @param event
	 *            {@link javax.faces.event.ActionEvent} object from the
	 *            specified source component and action command.
	 * @return {@link ome.admin.controller.IAdminExperimenterController#sort}
	 */
	public String sortItems(ActionEvent event) {
		this.sortItem = getAttribute(event, "sortItem");
		this.sort = getAttribute(event, "sort");
		this.userModel.setWrappedData(iadmin.sortItems(sortItem, sort));
		this.editMode = false;
		return this.sort;
	}

    public void scrollerAction(ActionEvent event)
    {
        ScrollerActionEvent scrollerEvent = (ScrollerActionEvent) event;
        FacesContext.getCurrentInstance().getExternalContext().log(
                        "scrollerAction: facet: "
                                        + scrollerEvent.getScrollerfacet()
                                        + ", pageindex: "
                                        + scrollerEvent.getPageIndex());
    }
    
	/**
	 * Provides action for navigation rule "changePassword" what is described in
	 * the faces-config.xml file. Changes the password for
	 * {@link ome.model.meta.Experimenter}.
	 * 
	 * @return String "success" or "false".
	 */
	public String changePassword() {
		try {
			this.user = (User) this.iadmin
					.getExperimenterById(this.user.getExperimenter().getId());
			this.editMode = true;
			return "success";
		} catch (Exception e) {
			logger.error("changePassword: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("changePassword", message);
			return "false";
		}
	}

	/**
	 * Provides action for navigation rule "updatePassword" what is described in
	 * the faces-config.xml file. Updates the password for
	 * {@link ome.model.meta.Experimenter}.
	 * 
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
				iadmin.changePassword(this.user.getExperimenter().getOmeName(),
						this.password);
				return "success";
			}
		} catch (Exception e) {
			logger.error("updatePassword: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("changePassword", message);
			return "false";
		}
	}
	
	/**
	 * Provides action for navigation rule "addNewExperimenter" what is
	 * described in the faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success" or "false"
	 */
	public String addNewExperimenter() {
		this.editMode = false;
		this.user = new User();
		return "success";
	}

	/**
	 * Provides action for navigation rule "addExperimenter" what is described
	 * in the faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success" or "false"
	 */
	public String addExperimenter() {
		try {
			if (this.user.getSelectedGroups() == null)
				this.user.setSelectedGroups(Collections.EMPTY_LIST);

			iadmin.createExperimenter(this.user);
			this.userModel.setWrappedData(iadmin.getAndSortItems(sortItem,
					sort));
			return "success";
		} catch (Exception e) {
			logger.error("createExperimenter: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenters", message);
			return "false";
		}
	}

	/**
	 * Provides action for navigation rule "editExperimenter" what is described
	 * in the faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success" or "false"
	 */
	public String editExperimenter() {
		try {
			this.editMode = true;
			this.user = (User) iadmin.getExperimenterById(((User) userModel.getRowData()).getExperimenter().getId());
			return "success";
		} catch (Exception e) {
			logger.error("editExperimenter: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenters", message);
			return "false";
		}
	}

	/**
	 * Provides action for navigation rule "deleteExperimenter" what is
	 * described in the faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success" or "false"
	 */
	public String delExperimenter() {
		try {
			this.user = (User) userModel.getRowData();
			iadmin.deleteExperimenter(this.user.getExperimenter().getId());
			this.userModel.setWrappedData(iadmin.getAndSortItems(sortItem,
					sort));
			return "success";
		} catch (Exception e) {
			logger.error("deleteExperimenter: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenters", message);
			return "false";
		}

	}

	/**
	 * Provides action for navigation rule "updateExperimenter" what is
	 * described in the faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success" or "false"
	 */
	public String updateExperimenter() {
		try {
			iadmin.updateExperimenter(this.user);
			this.userModel.setWrappedData(iadmin.getAndSortItems(sortItem,
					sort));
			return "success";
		} catch (Exception e) {
			logger.error("updateExperimenter: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
			return "false";
		}

	}

	/**
	 * Gets default wrapped {@link java.util.List} of
	 * {@link ome.model.meta.ExperimenterGroup}
	 * 
	 * @return {@link java.util.List} never-null.
	 */
	public List<SelectItem> getDefaultGroups() {
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		try {
			groups = iadmin.getGroupsAdd();
		} catch (Exception e) {
			logger.error("getDefaultGroups: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
		}
		return Utils.wrapAsGUIList(groups);
	}

	/**
	 * Create empty {@link ome.model.meta.ExperimenterGroup}
	 * 
	 * @return {@link ome.model.meta.ExperimenterGroup} never-null.
	 */
	private ExperimenterGroup emptyGroup() {
		ExperimenterGroup empty = new ExperimenterGroup();
		empty.setId(-1L);
		empty.setName(" ");
		return empty;
	}

	/**
	 * Gets wrapped {@link java.util.List} of
	 * {@link ome.model.meta.ExperimenterGroup}
	 * 
	 * @return {@link java.util.List}
	 */
	public List<SelectItem> getOtherGroups() {
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		try {
			if (this.editMode)
				groups.add(emptyGroup());
			groups.addAll(iadmin.getGroups());
		} catch (Exception e) {
			logger.error("getOtherGroups: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
		}
		return Utils.wrapAsGUIList(groups);
	}
	
	/**
	 * Provides validaton for email
	 * 
	 * @param context
	 *            {@link javax.faces.context.FacesContext}
	 * @param toValidate
	 *            {@link javax.faces.component.UIComponent}
	 * @param value
	 *            {@link java.lang.Object}
	 */
	public void validateEmail(FacesContext context, UIComponent toValidate,
			Object value) {
		try {
			String email = (String) value;

			if (email.indexOf('@') == -1) {
				((UIInput) toValidate).setValid(false);
				FacesMessage message = new FacesMessage("Invalid Email");
				context.addMessage(toValidate.getClientId(context), message);
			}
			if (iadmin.checkEmail(email)
					&& !email.equals(this.user.getExperimenter().getEmail())) {
				((UIInput) toValidate).setValid(false);
				FacesMessage message = new FacesMessage("Email exist");
				context.addMessage(toValidate.getClientId(context), message);
			}
		} catch (Exception e) {
			logger.error("validateEmail: " + e.getMessage());
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
		}
	}

	/**
	 * Provides validaton for {@link ome.model.meta.Experimenter#getOmeName()}.
	 * Cannot exist the same omeNames.
	 * 
	 * @param context
	 *            {@link javax.faces.context.FacesContext}
	 * @param toValidate
	 *            {@link javax.faces.component.UIComponent}
	 * @param value
	 *            {@link java.lang.Object}
	 */
	public void validateOmeName(FacesContext context, UIComponent toValidate,
			Object value) {
		String omeName = (String) value;
		if (iadmin.checkExperimenter(omeName)
				&& !omeName.equals(this.user.getExperimenter().getOmeName())) {
			((UIInput) toValidate).setValid(false);
			FacesMessage message = new FacesMessage("Username already exist");
			context.addMessage(toValidate.getClientId(context), message);
		}
	}
	
	/**
	 * Provides validaton for {@link ome.model.meta.Experimenter#getOmeName()}.
	 * Cannot exist the same omeNames.
	 * 
	 * @param context
	 *            {@link javax.faces.context.FacesContext}
	 * @param toValidate
	 *            {@link javax.faces.component.UIComponent}
	 * @param value
	 *            {@link java.lang.Object}
	 */
	public void validateUserType(FacesContext context, UIComponent toValidate,
			Object value) {
		String omeName = (String) value;
		if (iadmin.checkExperimenter(omeName)
				&& !omeName.equals(this.user.getExperimenter().getOmeName())) {
			((UIInput) toValidate).setValid(false);
			FacesMessage message = new FacesMessage("Username already exist");
			context.addMessage(toValidate.getClientId(context), message);
		}
	}

}
