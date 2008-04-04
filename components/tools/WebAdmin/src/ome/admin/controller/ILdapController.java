/*
 * ome.admin.controller.ILdapController
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.controller;

// Java imports
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import ome.admin.logic.ILdapManagerDelegate;
import ome.admin.model.User;
import ome.utils.NavigationResults;
import ome.utils.Utils;

import org.apache.log4j.Logger;

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
public class ILdapController implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * log4j logger
     */
    static Logger logger = Logger.getLogger(ILdapController.class.getName());

    /**
     * {@link ome.admin.model.User}.
     */
    private User user = new User();

    private String searchField = "";

    private String searchAttribute = "sn";

    /**
     * {@link javax.faces.model.ListDataModel} The data collection wrapped by
     * this {@link javax.faces.model.DataModel}.
     */
    private DataModel userModel = new ListDataModel();

    /**
     * {@link ome.admin.logic.ILdapManagerDelegate}
     */
    private ILdapManagerDelegate ildap = new ILdapManagerDelegate();

    /**
     * boolean value for providing Search/Add form in one JSP.
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
     * Creates a new instance of ILdapController.
     */
    public ILdapController() {
        this.userModel.setWrappedData(ildap.getAndSortItems(sortItem, sort));
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getSearchField() {
        return searchField;
    }

    public void setSearchField(String searchField) {
        this.searchField = searchField;
    }

    public DataModel getUserModel() {
        return userModel;
    }

    public void setUserModel(DataModel userModel) {
        this.userModel = userModel;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSearchAttribute() {
        return searchAttribute;
    }

    public void setSearchAttribute(String searchAttribute) {
        this.searchAttribute = searchAttribute;
    }

    public DataModel getUsers() {
        this.userModel.setWrappedData(ildap.getAndSortItems(sortItem, sort));
        return this.userModel;
    }

    public String searchInLdap() {
        try {
            this.editMode = true;
            ildap.lookupImportingExperimenters("", this.searchAttribute,
                    this.searchField);
            this.userModel
                    .setWrappedData(ildap.getAndSortItems(sortItem, sort));
            return NavigationResults.FALSE;
        } catch (Exception e) {
            logger.error(e.getMessage(), e.fillInStackTrace());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Exception: "
                    + e.getMessage());
            context.addMessage("searchLdap", message);
            return NavigationResults.FALSE;
        }

    }

    /**
     * Is called by action for saving datas. Selected users are created.
     * 
     * @return {@link java.lang.String} "success" or "false"
     */
    public String saveItems() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            ildap.createExperimenters((List<User>) this.userModel
                    .getWrappedData());

            IAdminExperimenterController ia = (IAdminExperimenterController) context
                    .getApplication().getVariableResolver().resolveVariable(
                            context, "IAEManagerBean");
            ia.setEditMode(true);
            this.editMode = false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e.fillInStackTrace());
            FacesMessage message = new FacesMessage("Exception: "
                    + e.getMessage());
            context.addMessage("searchLdap", message);
            return NavigationResults.FALSE;
        }

        return NavigationResults.SUCCESS;
    }

    public int getSize() {
        return this.userModel.getRowCount();
    }

    public List<SelectItem> getAttributes() {
        FacesContext fc = FacesContext.getCurrentInstance();
        return Utils.wrapAttrsAsGUIList(fc.getExternalContext()
                .getInitParameter("ldapAttributes").split(","));
    }

}
