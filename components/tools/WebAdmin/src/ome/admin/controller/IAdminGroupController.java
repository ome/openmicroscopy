/*
 * ome.admin.controller
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.controller;

// Java imports
import java.util.Collections;
import java.util.List;

// Third-party libraries
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.datascroller.ScrollerActionEvent;

// Application-internal dependencies
import ome.admin.logic.IAdminGroupManagerDelegate;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.utils.NavigationResults;
import ome.utils.Utils;

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
    static Logger logger = Logger.getLogger(IAdminGroupController.class
            .getName());

    /**
     * {@link ome.model.meta.ExperimenterGroup}
     */
    private ExperimenterGroup group;
    
    private String owner = "0";

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
     * containedGroups for chosen
     * {@link ome.model.meta.ExperimenterGroup#getId()}
     */
    private List<String> selectedExperimenters = Collections.EMPTY_LIST;

    /**
     * boolean value for providing Add/Edit form in one JSP.
     */
    private boolean editMode = false;

    /**
     * boolean value for providing scroller on the data table.
     */
    private boolean scrollerMode = true;

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
        this.groupModel.setWrappedData(iadmin.getAndSortItems(sortItem, sort));
        this.scrollerMode = iadmin.setScroller();
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
        this.scrollerMode = iadmin.setScroller();
        return this.groupModel;
    }
    
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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
     * Checks {@link ome.admin.controller.IAdminGroupController#scrollerMode}
     * 
     * @return boolean
     */
    public boolean isScrollerMode() {
        return this.scrollerMode;
    }

    /**
     * Sets {@link ome.admin.controller.IAdminGroupController#scrollerMode}
     * 
     * @param scrollerMode
     *            boolean
     */
    public void setScrollerMode(boolean scrollerMode) {
        this.scrollerMode = scrollerMode;
    }

    /**
     * Provides action for navigation rule "addNewGroup" what is described in
     * the faces-config.xml file.
     * 
     * @return {@link java.lang.String} "success"
     */
    public String addNewGroup() {
        this.editMode = false;
        this.owner = "0";
        this.group = new ExperimenterGroup();
        return NavigationResults.SUCCESS;
    }

    /**
     * Provides action for navigation rule "addGroup" what is described in the
     * faces-config.xml file.
     * 
     * @return {@link java.lang.String} "success"
     */
    public String addGroup() {
        try {
            iadmin.addGroup(this.group, Long.valueOf(this.owner));
            this.groupModel.setWrappedData(iadmin.getAndSortItems(sortItem,
                    sort));
            return NavigationResults.SUCCESS;
        } catch (Exception e) {
            logger.error("addGroup: " + e.getMessage());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Group: [id: "
                    + this.group.getId() + ", '" + this.group.getName()
                    + "'] : " + e.getMessage());
            context.addMessage("changePassword", message);
            return NavigationResults.FALSE;
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
            this.owner = this.group.getDetails().getOwner().getId().toString();
            if (!checkGroup(this.group.getName())) {
                this.editMode = true;
                return NavigationResults.SUCCESS;
            } else {
                this.editMode = false;
                FacesMessage message = new FacesMessage(
                        "Cannot edit group [name '" + this.group.getName()
                                + "']. This is system group.");
                context.addMessage("groups", message);
                return NavigationResults.FALSE;
            }
        } catch (Exception e) {
            logger.error("editGroup: " + e.getMessage());
            FacesMessage message = new FacesMessage("Group: [id: "
                    + this.group.getId() + ", '" + this.group.getName()
                    + "'] : " + e.getMessage());
            context.addMessage("groups", message);
            return NavigationResults.FALSE;
        }
    }

    /**
     * Checks system or default group name
     * 
     * @param name
     *            {@link java.lang.String}
     * @return
     */
    private boolean checkGroup(String name) {
        if (name.equals("system"))
            return true;
        if (name.equals("default"))
            return true;
        if (name.equals("guest"))
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
            if (!checkGroup(this.group.getName())) {
                this.editMode = true;
                iadmin.deleteGroup(this.group.getId());
                this.groupModel.setWrappedData(iadmin.getAndSortItems(sortItem,
                        sort));
                return NavigationResults.SUCCESS;
            }
            FacesMessage message = new FacesMessage(
                    "Cannot delete group [name '" + this.group.getName()
                            + "']. This is system group.");
            context.addMessage("groups", message);
            return NavigationResults.FALSE;
        } catch (Exception e) {
            logger.error("delGroup: " + e.getMessage());
            FacesMessage message = new FacesMessage("Group: [id: "
                    + this.group.getId() + ", '" + this.group.getName()
                    + "'] : " + e.getMessage());
            context.addMessage("groups", message);
            return NavigationResults.FALSE;
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
            iadmin.updateGroup(this.group, Long.valueOf(this.owner));
            this.groupModel.setWrappedData(iadmin.getAndSortItems(sortItem,
                    sort));
            this.editMode = false;
            return NavigationResults.SUCCESS;
        } catch (Exception e) {
            logger.error("updateGroup: " + e.getMessage());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Group: [id: "
                    + this.group.getId() + ", '" + this.group.getName()
                    + "'] : " + e.getMessage());
            context.addMessage("groupForm", message);
            return NavigationResults.FALSE;
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
     * @param experimenters
     *            {@link java.util.List} of {@link ome.model.meta.Experimenter}
     *            never-null.
     */
    public void setSelectedExperimenters(List<String> experimenters) {
        this.selectedExperimenters = experimenters;
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
        return Utils.wrapSelectItemAsGUIList(this.experimenters);
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
            this.group = (ExperimenterGroup) iadmin
                    .getGroupById(((ExperimenterGroup) groupModel.getRowData())
                            .getId());
            return NavigationResults.SUCCESS;
        } catch (Exception e) {
            logger.error("editInGroup: " + e.getMessage());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Group: [id: "
                    + this.group.getId() + ", '" + this.group.getName()
                    + "'] : " + e.getMessage());
            context.addMessage("groups", message);
            return NavigationResults.FALSE;
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
            return NavigationResults.SUCCESS;
        } catch (Exception e) {
            logger.error("SaveInToGroup: " + e.getMessage());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Group: [id: "
                    + this.group.getId() + ", '" + this.group.getName()
                    + "'] : " + e.getMessage());
            context.addMessage("groups", message);
            return NavigationResults.FALSE;
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

    /**
     * Provides the scroller action.
     * 
     * @param event
     */
    public void scrollerAction(ActionEvent event) {
        ScrollerActionEvent scrollerEvent = (ScrollerActionEvent) event;
        logger.info("scrollerAction: facet: "
                + scrollerEvent.getScrollerfacet() + ", pageindex: "
                + scrollerEvent.getPageIndex());
    }

    /**
     * Provides validaton for {@link ome.model.meta.ExperimenterGroup#getName()}.
     * Cannot exist the same group names.
     * 
     * @param context
     *            {@link javax.faces.context.FacesContext}
     * @param toValidate
     *            {@link javax.faces.component.UIComponent}
     * @param value
     *            {@link java.lang.Object}
     */
    public void validateGroupName(FacesContext context, UIComponent toValidate,
            Object value) {
        String name = (String) value;
        if (iadmin.checkExperimenterGroup(name)
                && !name.equals(this.group.getName())) {
            ((UIInput) toValidate).setValid(false);
            FacesMessage message = new FacesMessage("Groupname already exist");
            context.addMessage(toValidate.getClientId(context), message);
        }
    }
}
