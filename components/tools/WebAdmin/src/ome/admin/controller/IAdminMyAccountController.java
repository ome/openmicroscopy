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

//Third-party libraries
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

//Application-internal dependencies
import ome.admin.logic.IAdminExperimenterManagerDelegate;
import ome.admin.model.User;
import ome.model.meta.ExperimenterGroup;

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
	private User user;

    /**
     * {@link ome.admin.logic.IAdminExperimenterManagerDelegate}
     */
	private IAdminExperimenterManagerDelegate iadmin = new IAdminExperimenterManagerDelegate();
	
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
	public IAdminMyAccountController() {
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			LoginBean lb = (LoginBean) facesContext.getApplication()
					.getVariableResolver().resolveVariable(facesContext,
							"LoginBean");
			this.user = iadmin.getExperimenterById(Long.parseLong(lb
					.getId()));
		} catch (Exception e) {
			logger.error("IAdminMyAccountController: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
		}
	}

    /**
     * Provides action for navigation rule "changeMyPassword" what is described in the faces-config.xml file. Changes the password for {@link ome.model.meta.Experimenter}.
     * @return {@link java.lang.String}
     */
	public String changeMyPassword() {
		try {
			this.user = (User) this.iadmin
					.getExperimenterById(this.user.getExperimenter().getId());
			return NavigationResults.SUCCESS;
		} catch (Exception e) {
			logger.error("changeMyPassword: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("changePassword", message);
			return NavigationResults.FALSE;
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
				return NavigationResults.FALSE;
			} else {
				iadmin.changeMyPassword(this.password);
				FacesContext facesContext = FacesContext.getCurrentInstance();
				HttpSession session = (HttpSession) facesContext
						.getExternalContext().getSession(false);
				session.invalidate();
				return NavigationResults.SUCCESS;
			}
		} catch (Exception e) {
			logger.error("updateMyPassword: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
			return NavigationResults.FALSE;
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
     * Gets wrapped {@link ome.model.meta.ExperimenterGroup} for all of the {@link ome.model.meta.Experimenter#getId()} without "system" and "user" groups.
     * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
     */
	public List getMyGroups() {
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		try {
			ExperimenterGroup[] exg = iadmin
					.containedMyGroups(this.user.getExperimenter().getId());

			groups = Arrays.asList(exg);
		} catch (Exception e) {
			logger.error("getMyGroups: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
		}
		return Utils.wrapExperimenterGroupAsGUIList(groups);
	}


    /**
     * Sets {@link ome.admin.controller.IAdminMyAccountController#user}
     * @param user {@link ome.admin.model.User}
     */
	public void setUser(User user) {
		this.user = user;
	}

    /**
     * Gets {@link ome.admin.controller.IAdminMyAccountController#user}
     * @return {@link ome.admin.model.User}
     */
	public User getUser() {
		return this.user;
	}

    /**
     * Provides action for navigation rule "editExperimenter" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success" or "false"
     */
	public String editExperimenter() {
		try {
			this.user = (User) iadmin
					.getExperimenterById(this.user.getExperimenter().getId());
			return NavigationResults.SUCCESS;
		} catch (Exception e) {
			logger.error("editExperimenter: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
			return NavigationResults.FALSE;
		}
	}

    /**
     * Provides action for navigation rule "updateExperimenter" what is described in the faces-config.xml file.
     * @return {@link java.lang.String} "success" or "false"
     */
	public String updateExperimenter() {
		try {
			iadmin.updateMyAccount(this.user);
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext
					.getExternalContext().getSession(true);
			session.invalidate();
			return NavigationResults.SUCCESS;
		} catch (Exception e) {
			logger.error("updateExperimenter: " + e.getMessage());
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Experimenter: [id: "
					+ this.user.getExperimenter().getId() + ", '"
					+ this.user.getExperimenter().getOmeName() + "'] : " + e.getMessage());
			context.addMessage("experimenterForm", message);
			return NavigationResults.FALSE;
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
			context.addMessage("groupForm", message);
		}
	}

}
