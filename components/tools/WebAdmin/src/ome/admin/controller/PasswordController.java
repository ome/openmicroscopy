package ome.admin.controller;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import ome.admin.logic.PasswordManagerDelegator;
import ome.utils.NavigationResults;

import org.apache.log4j.Logger;

public class PasswordController implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String server;

    private int port;

    /**
     * String not-null. Might must pass validation in the security sub-system.
     * ChangePassword/ForgottenPassword - username.
     */
    private String omeName;

    /**
     * String not-null. Might must pass validation in the security sub-system.
     * Forgotten password - email.
     */
    private String email;

    /**
     * String not-null. Might must pass validation in the security sub-system.
     * Old Password.
     */
    private String oldPassword;

    /**
     * String not-null. Might must pass validation in the security sub-system.
     * New password.
     */
    private String newPassword;

    /**
     * String not-null. Might must pass validation in the security sub-system.
     * Confirmation of password.
     */
    private String newPassword2;

    /**
     * boolean value for providing ForgottenPassword/ChangePassword form in one
     * JSP. false - ForgottenPassword. true - ChangePassword
     */
    private boolean typeForm = false;

    /**
     * log4j logger
     */
    static Logger logger = Logger.getLogger(PasswordController.class.getName());

    /**
     * {@link ome.admin.logic.ITypesEnumManagerDelegate}
     */
    private PasswordManagerDelegator passwd;

    public boolean isTypeForm() {
        return this.typeForm;
    }

    public void setTypeForm(boolean type) {
        this.typeForm = type;
    }

    public String getServer() {
        if (this.server == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            this.server = fc.getExternalContext().getInitParameter(
                    "defaultServerHost");
        }
        return this.server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        if (this.port == 0) {
            FacesContext fc = FacesContext.getCurrentInstance();
            this.port = Integer.parseInt(fc.getExternalContext()
                    .getInitParameter("defaultServerPort"));
        }
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getOmeName() {
        return this.omeName;
    }

    public void setOmeName(String omeName) {
        this.omeName = omeName;
    }

    public String getOldPassword() {
        return this.oldPassword;
    }

    public void setOldPassword(String oldPasswd) {
        if (oldPasswd != null)
            this.oldPassword = oldPasswd;
        else
            this.oldPassword = "";
    }

    public String getNewPassword() {
        return this.newPassword;
    }

    public void setNewPassword(String newPasswd) {
        if (newPasswd != null)
            this.newPassword = newPasswd;
        else
            this.newPassword = "";
    }

    public String getNewPassword2() {
        return this.newPassword2;
    }

    public void setNewPassword2(String newPasswd2) {
        if (newPasswd2 != null)
            this.newPassword2 = newPasswd2;
        else
            this.newPassword2 = "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String resetPassword() {
        FacesContext context = FacesContext.getCurrentInstance();
        this.passwd = new PasswordManagerDelegator(this.server, this.port);
        try {
            passwd.reportForgottenPassword(this.omeName, this.email);
            FacesMessage message = new FacesMessage(
                    "Your new password was sent to your email: "
                            + this.email
                            + ". Please check your email and follow the instruction.");
            context.addMessage("loginForm", message);
            return NavigationResults.SUCCESS;
        } catch (Exception e) {
            logger.error("resetPassword: " + e.getMessage());
            FacesMessage message = new FacesMessage("Cannot reset password: "
                    + e.getMessage());
            context.addMessage("forgottenPassword", message);
            return NavigationResults.FALSE;
        }
    }

    public String expirePassword() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            if (!newPassword2.equals(this.newPassword)) {
                FacesMessage message = new FacesMessage(
                        "Confirmation has to be the same as password.");
                context.addMessage("expiredPassword", message);
                return NavigationResults.FALSE;
            } else {
                boolean res = passwd.changeExpiredPassword(this.omeName,
                        this.email, this.oldPassword, this.newPassword);
                if (res) {
                    FacesMessage message = new FacesMessage(
                            "Your password was reseted. Please login.");
                    context.addMessage("loginForm", message);
                    return NavigationResults.SUCCESS;
                } else {
                    FacesMessage message = new FacesMessage(
                            "You cannot change password for '"
                                    + this.oldPassword
                                    + "' and email: '"
                                    + this.email
                                    + "' because one of the parameter is incorrect. Please, try again.");
                    context.addMessage("expiredPassword", message);
                    return NavigationResults.FALSE;
                }
            }
        } catch (Exception e) {
            logger.error("resetPassword: " + e.getMessage());
            FacesMessage message = new FacesMessage("Password exception: "
                    + e.getMessage());
            context.addMessage("expiredPassword", message);
            return NavigationResults.FALSE;
        }
    }

}
