/*
* ome.admin.controller
*
*   Copyright 2007 University of Dundee. All rights reserved.
*   Use is subject to license terms supplied in LICENSE.txt
*/

package ome.admin.controller;

// Java imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.faces.component.UIInput;
import javax.faces.component.UIComponent;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

// Third-party libraries

// Application-internal dependencies
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.admin.logic.IAdminExperimenterManagerDelegate;
import ome.admin.controller.LoginBean;
import ome.admin.data.ConnectionDB;

/**
 * It's the Java bean with fife attributes and setter/getter and actions methods. The bean captures login params entered by a user after the user clicks the submit button. This way the bean provides a bridge between the JSP page and the application logic.
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class IAdminMyAccountController implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * log4j logger
	 */
	static Logger logger = Logger.getLogger(IAdminMyAccountController.class.getName());
	
    /**
     * {@link ome.model.meta.Experimenter}
     */
	private Experimenter experimenter;

    /**
     * {@link ome.admin.logic.IAdminExperimenterManagerDelegate}
     */
	private IAdminExperimenterManagerDelegate iadmin = new IAdminExperimenterManagerDelegate();

    /**
     * {@link java.lang.String}
     */
	private String defaultGroup = "-1";

    /**
     * Not-null. Might must pass validation in the security sub-system.
     */
	private String password = "";

    /**
     * Confirmation of password. Not-null. Might must pass validation in the security sub-system.
     */
	private String password2 = "";

    /**
     * Creates a new instance of IAdminMyAccountController
     */
	@SuppressWarnings("deprecation")
	public IAdminMyAccountController() {
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			LoginBean lb = (LoginBean) facesContext.getApplication()
					.getVariableResolver().resolveVariable(facesContext,
							"LoginBean");
			this.experimenter = iadmin.getExperimenterById(Long.parseLong(lb
					.getId()));
		} catch (Exception e) {
			logger.error("changeMyPassword: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.experimenter.getId() + ", '"
					+ this.experimenter.getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
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
     * Provides action for navigation rule "changeMyPassword" what is described in the faces-config.xml file. Changes the password for {@link ome.model.meta.Experimenter}.
     * @return {@link java.lang.String}
     */
	public String changeMyPassword() {
		try {
			this.experimenter = (Experimenter) this.iadmin
					.getExperimenterById(this.experimenter.getId());
			return "success";
		} catch (Exception e) {
			logger.error("changeMyPassword: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.experimenter.getId() + ", '"
					+ this.experimenter.getOmeName() + "'] : " + e.getMessage());
			context.addMessage("changePassword", message);
			return "false";
		}
	}

    /**
     * Provides action for navigation rule "updateMyPassword" what is described in the faces-config.xml file. Changes the password for current {@link ome.model.meta.Experimenter}.
     */
	public String updateMyPassword() {
		try {
			if (!password2.equals(this.password)) {
				FacesContext context = FacesContext.getCurrentInstance();
				FacesMessage message = new FacesMessage(
						"Confirmation has to be the same as password.");
				context.addMessage("changePassword", message);
				return "false";
			} else {
				iadmin.changeMyPassword(this.password);
				FacesContext facesContext = FacesContext.getCurrentInstance();
				HttpSession session = (HttpSession) facesContext
						.getExternalContext().getSession(false);
				session.invalidate();
				return "success";
			}
		} catch (Exception e) {
			logger.error("updateMyPassword: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.experimenter.getId() + ", '"
					+ this.experimenter.getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
			return "false";
		}

	}

    /**
     * Sets password
     * @param password @Not-null. Might must pass validation in the security sub-system.
     */
	public void setPassword(String password) {
		if (password != null)
			this.password = password;
		else
			this.password = "";
	}

    /**
     * Get password
     * @return Not-null. Might must pass validation in the security sub-system.
     */
	public String getPassword() {
		return this.password;
	}

    /**
     * Set confirmation of password
     * @param password2 Not-null. Might must pass validation in the security sub-system.
     */
	public void setPassword2(String password2) {
		if (password2 != null)
			this.password2 = password2;
		else
			this.password2 = "";
	}

    /**
     * Gets confirmation of password
     * @return Not-null. Might must pass validation in the security sub-system.
     */
	public String getPassword2() {
		return this.password2;
	}

    /**
     * Provides action for navigation rule "editMyAccount" what is described in the faces-config.xml file.
     * @param event {@link javax.faces.event.ActionEvent} object from the specified source component and action command.
     * @return {@link java.lang.String} "success" or "false"
     */
	public String editMyAccount(ActionEvent event) {
		try {
			this.experimenter = (Experimenter) this.iadmin
					.getExperimenterById(Long.parseLong(getAttribute(event,
							"userid")));
			return "success";
		} catch (Exception e) {
			logger.error("editMyAccount: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.experimenter.getId() + ", '"
					+ this.experimenter.getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
			return "false";
		}
	}

    /**
     * Gets wrapped {@link ome.model.meta.ExperimenterGroup} for all of the {@link ome.model.meta.Experimenter#getId()} without "system" and "user" groups.
     * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
     */
	public List getMyGroups() {
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		try {
			ExperimenterGroup[] exg = iadmin
					.containedMyGroups(this.experimenter.getId());

			groups = Arrays.asList(exg);
		} catch (Exception e) {
			logger.error("getMyGroups: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.experimenter.getId() + ", '"
					+ this.experimenter.getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
		}
		return wrapAsGUIList(groups);
	}

    /**
     * Sets fild {@link ome.admin.controller.IAdminMyAccountController#defaultGroup}
     * @param id Long
     */
	public void setDefaultGroup(String id) {
		this.defaultGroup = id;
	}

    /**
     * Gets default group {@link ome.model.meta.ExperimenterGroup#getId()}
     * @return Long
     */
	public String getDefaultGroup() {
		try {
			ExperimenterGroup exg = iadmin.getDefaultGroup(this.experimenter
					.getId());
			this.defaultGroup = exg.getId().toString();
		} catch (Exception e) {
			logger.error("getDefaultGroup: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.experimenter.getId() + ", '"
					+ this.experimenter.getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
		}
		return this.defaultGroup;
	}

    /**
     * Wraps original {@link java.util.List} as GUI List {@link javax.faces.model.SelectItem}
     * @param originalList {@link java.util.List}
     * @return {@link java.util.ArrayList}<{@link javax.faces.model.SelectItem}>
     */
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

    /**
     * Sets {@link ome.admin.controller.IAdminMyAccountController#experimenter}
     * @param exp {@link ome.model.meta.Experimenter}
     */
	public void setExperimenter(Experimenter exp) {
		this.experimenter = exp;
	}

    /**
     * Gets {@link ome.admin.controller.IAdminMyAccountController#experimenter}
     * @return {@link ome.model.meta.Experimenter}
     */
	public Experimenter getExperimenter() {
		return this.experimenter;
	}

    /**
     * Provides action for navigation rule "editExperimenter" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success" or "false"
     */
	public String editExperimenter() {
		try {
			this.experimenter = (Experimenter) iadmin
					.getExperimenterById(this.experimenter.getId());
			return "success";
		} catch (Exception e) {
			logger.error("editExperimenter: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.experimenter.getId() + ", '"
					+ this.experimenter.getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
			return "false";
		}
	}

    /**
     * Provides action for navigation rule "updateExperimenter" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success" or "false"
     */
	public String updateExperimenter() {
		try {
			iadmin.updateMyAccount(this.experimenter, this.defaultGroup);
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext
					.getExternalContext().getSession(true);
			session.invalidate();
			return "success";
		} catch (Exception e) {
			logger.error("updateExperimenter: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.experimenter.getId() + ", '"
					+ this.experimenter.getOmeName() + "'] : " + e.getMessage());
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
		try {
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
		} catch (Exception e) {
			logger.error("validateEmail: " + e.getMessage());
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.experimenter.getId() + ", '"
					+ this.experimenter.getOmeName() + "'] : " + e.getMessage());
			context.addMessage("groupForm", message);
		}
	}

}
