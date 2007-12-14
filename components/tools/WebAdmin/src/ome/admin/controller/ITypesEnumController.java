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
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.datascroller.ScrollerActionEvent;

// Application-internal dependencies
import ome.admin.logic.ITypesEnumManagerDelegate;
import ome.admin.model.Entry;
import ome.admin.model.Enumeration;
import ome.model.IEnum;
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
public class ITypesEnumController implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * log4j logger
     */
    static Logger logger = Logger.getLogger(ITypesEnumController.class
            .getName());

    /**
     * {@link ome.admin.model.Enumeration}
     */
    private Enumeration enumeration = new Enumeration();

    /**
     * {@link ome.admin.model.Entry}
     */
    private Entry entry = new Entry();

    /**
     * {@link javax.faces.model.ListDataModel} The data collection wrapped by
     * this {@link javax.faces.model.DataModel}
     */
    private DataModel enumerationModel = new ListDataModel();

    /**
     * {@link javax.faces.model.ListDataModel} The data collection wrapped by
     * this {@link javax.faces.model.DataModel}
     */
    private DataModel entryModel = new ListDataModel();

    /**
     * {@link ome.admin.logic.ITypesEnumManagerDelegate}
     */
    private ITypesEnumManagerDelegate itype = new ITypesEnumManagerDelegate();

    /**
     * boolean value for providing Add/Edit form in one JSP.
     */
    private boolean editMode = false;

    /**
     * boolean value for providing scroller on the data table.
     */
    private boolean scrollerMode = true;

    /**
     * {@link java.lang.String} default value "className"
     */
    private String sortItem = "className";

    /**
     * {@link java.lang.String} default value "value"
     */
    private String sortEntry = "value";

    /**
     * {@link java.lang.String} sorting attribute default value is "asc"
     */
    private String sort = "asc";

    /**
     * Creates a new instance of ITypesEnumController
     */
    public ITypesEnumController() {
        try {
            this.enumerationModel.setWrappedData(itype.getAndSortItems(
                    sortItem, sort));
            this.scrollerMode = itype.setScroller();
        } catch (Exception e) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage(
                    "Enumeration cannot gets values. " + e.getMessage());
            context.addMessage("enumerations", message);
        }
    }

    /**
     * Gets size the data collection wrapped by this
     * {@link javax.faces.model.DataModel}
     * 
     * @return int
     */
    public int getSize() {
        return enumerationModel.getRowCount();
    }

    /**
     * Sets {@link ome.admin.controller.ITypesEnumController#enumeration}
     * 
     * @param en
     *            {@link ome.admin.model.Enumeration}
     */
    public void setEnumeration(Enumeration en) {
        this.enumeration = en;
    }

    /**
     * Gets {@link ome.admin.model.Enumeration}
     * 
     * @return {@link ome.admin.controller.ITypesEnumController#enumeration}
     */
    public Enumeration getEnumeration() {
        return this.enumeration;
    }

    /**
     * Sets {@link ome.admin.controller.ITypesEnumController#entry}
     * 
     * @param en
     *            {@link ome.admin.model.Entry}
     */
    public void setEntry(Entry en) {
        this.entry = en;
    }

    /**
     * Gets {@link ome.admin.model.Entry}
     * 
     * @return {@link ome.admin.controller.ITypesEnumController#entry}
     */
    public Entry getEntry() {
        return this.entry;
    }

    /**
     * Gets data collection of {@link ome.admin.model.Enumeration} wrapped by
     * this {@link javax.faces.model.DataModel}
     * 
     * @return {@link javax.faces.model.DataModel}
     */
    public DataModel getEnumerations() {
        if (this.editMode) {
            this.enumerationModel.setWrappedData(itype.getAndSortItems(
                    sortItem, sort));
            this.scrollerMode = itype.setScroller();
        }
        if (this.enumerationModel == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage(
                    "Enumeration cannot get values.");
            context.addMessage("enumerations", message);
        }
        return this.enumerationModel;
    }

    /**
     * Gets data collection of {@link ome.admin.model.Entry} wrapped by this
     * {@link javax.faces.model.DataModel}
     * 
     * @return {@link javax.faces.model.DataModel}
     */
    public DataModel getEntrys() {
        if (this.entryModel == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage(
                    "Enumeration cannot get values.");
            context.addMessage("enumerationEditForm", message);
        }
        return this.entryModel;

    }

    /*
     * public void setEntrys(DataModel dm) { this.entryModel = dm; }
     */

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
     * Checks {@link ome.admin.controller.ITypesEnumController#scrollerMode}
     * 
     * @return boolean
     */
    public boolean isScrollerMode() {
        return this.scrollerMode;
    }

    /**
     * Sets {@link ome.admin.controller.ITypesEnumController#scrollerMode}
     * 
     * @param scrollerMode
     *            boolean
     */
    public void setScrollerMode(boolean scrollerMode) {
        this.scrollerMode = scrollerMode;
    }

    /**
     * Provides action for navigation rule "addNewEnumeration" what is described
     * in the faces-config.xml file.
     * 
     * @return {@link java.lang.String} "success"
     */
    public String addNewEnumeration() {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage message = new FacesMessage("Cannot add new enumeration." +
                "v2.3 does not support it.");
        context.addMessage("enumerations", message);
        return NavigationResults.FALSE;
        /*this.editMode = false;
        this.enumeration = new Enumeration();
        this.enumeration.setClassName("");
        return NavigationResults.SUCCESS;*/
    }

    /**
     * Provides action for navigation rule "addEnumeration" what is described in
     * the faces-config.xml file.
     * 
     * @return {@link java.lang.String} "success"
     */
    public String addEnumeration() {
        return NavigationResults.FALSE;
        /*try {
            itype.checkEnumeration(Class.forName(this.enumeration
                    .getClassName()), this.enumeration.getEvent());
            itype.addEnumeration(this.enumeration);
            this.enumerationModel.setWrappedData(itype.getAndSortItems(
                    sortItem, sort));
            return NavigationResults.SUCCESS;
        } catch (Exception e) {
            logger.error("addEnumeration: " + e.getMessage());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Enumeration: [type: "
                    + this.enumeration.getClassName() + "'] : "
                    + e.getMessage());
            context.addMessage("enumerationAddForm", message);
            return NavigationResults.FALSE;
        }*/
    }

    /**
     * Provides action for navigation rule "editEnumeration" what is described
     * in the faces-config.xml file.
     * 
     * @return {@link java.lang.String} "success" or "false"
     */
    public String editEnumeration() {
        try {
            this.editMode = true;
            this.enumeration = (Enumeration) enumerationModel.getRowData();
            this.entryModel.setWrappedData(itype.getAndSortEntrys(sortEntry,
                    sort, this.enumeration.getEntryList(), null));
            return NavigationResults.SUCCESS;
        } catch (Exception e) {
            logger.error("editEnumeration: " + e.getMessage());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Enumeration: [class: "
                    + this.enumeration.getClassName() + "'] : "
                    + e.getMessage());
            context.addMessage("enumerations", message);
            return NavigationResults.FALSE;
        }
    }

    /**
     * Provides action for navigation rule "deleteEnumeration" what is described
     * in the faces-config.xml file.
     * 
     * @return {@link java.lang.String} "success" or "false"
     */
    public String delEnumeration() {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage message = new FacesMessage("Cannot delete enumeration." +
                "v2.3 does not support it.");
        context.addMessage("enumerationEditForm", message);
        return NavigationResults.FALSE;
        /*try {
            itype.delEnumeration((IEnum) entryModel.getRowData());
            this.enumerationModel.setWrappedData(itype.getAndSortItems(
                    sortItem, sort));
            Class klass = entryModel.getRowData().getClass();
            this.entryModel.setWrappedData(itype.getAndSortEntrys(sortEntry,
                    sort, null, klass));
            return NavigationResults.SUCCESS;
        } catch (Exception e) {
            logger.error("deleteEnumeration: " + e.getMessage());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Enumeration: [id: "
                    + this.entry.getId() + ", '" + this.entry.getValue()
                    + "'] : " + e.getMessage());
            context.addMessage("enumerationEditForm", message);
            return NavigationResults.FALSE;
        }*/
    }

    /**
     * Provides action for navigation rule "updateEnumeration" what is described
     * in the faces-config.xml file.
     * 
     * @return {@link java.lang.String} "success" or "false"
     */
    public String updateEnumerations() {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage message = new FacesMessage("Cannot update enumerations." +
                "v2.3 does not support it.");
        context.addMessage("enumerationEditForm", message);
        return NavigationResults.FALSE;
        /*try {
            this.editMode = false;
            Class klass = Class.forName(this.enumeration.getClassName());
            itype.updateEnumerations(this.enumeration.getEntryList());
            this.enumerationModel.setWrappedData(itype.getAndSortItems(
                    sortItem, sort));
            this.entryModel.setWrappedData(itype.getAndSortEntrys(sortEntry,
                    sort, null, klass));
            return NavigationResults.SUCCESS;
        } catch (Exception e) {
            logger.error("updateEnumeration: " + e.getMessage());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Enumeration: [id: "
                    + this.entry.getId() + ", '" + this.entry.getValue()
                    + "'] : " + e.getMessage());
            context.addMessage("groupForm", message);
            return NavigationResults.FALSE;
        }*/
    }

    /**
     * Gets default wrapped {@link java.util.List} of
     * {@link ome.model.IEnum}
     * 
     * @return {@link java.util.List} never-null.
     */
    public List<SelectItem> getEnumerationsType() {
        List<Class<IEnum>> klass = Collections.EMPTY_LIST;
        try {
            klass = itype.getEnumerations();
        } catch (Exception e) {
            logger.error("getEnumerations: " + e.getMessage());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage(
                    "Cannot get types of enumeration: " + e.getMessage());
            context.addMessage("enumerationAddForm", message);
        }
        return Utils.wrapExperimenterClassAsGUIList(klass);
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
     * Sort items on the data collection of {@link ome.admin.model.Enumeration}
     * wrapped by this {@link javax.faces.model.DataModel}
     * 
     * @param event
     *            {@link javax.faces.event.ActionEvent} object from the
     *            specified source component and action command.
     * @return {@link ome.admin.controller.ITypesEnumController}
     */
    public String sortItems(ActionEvent event) {
        this.sortItem = getAttribute(event, "sortItem");
        this.sort = getAttribute(event, "sort");
        this.enumerationModel.setWrappedData(itype.sortItems(sortItem, sort));
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

}
