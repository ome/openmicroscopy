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

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import ome.admin.logic.ITypesEnumManagerDelegate;
import ome.admin.model.Enumeration;
import ome.utils.NavigationResults;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.datascroller.ScrollerActionEvent;

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
	private Enumeration enumeration;

	/**
	 * {@link javax.faces.model.ListDataModel} The data collection wrapped by
	 * this {@link javax.faces.model.DataModel}
	 */
	private DataModel enumerationModel = new ListDataModel();

	/**
	 * {@link ome.admin.logic.ITypesEnumManagerDelegate}
	 */
	private ITypesEnumManagerDelegate itype = new ITypesEnumManagerDelegate();

	/**
	 * {@link java.util.List}<{@link java.lang.String}> List of
	 * {@link ome.admin.data.ConnectionDB#getEnumerations()}
	 */
	private List<Enumeration> enumerations = Collections.EMPTY_LIST;

	/**
	 * boolean value for providing Add/Edit form in one JSP.
	 */
	private boolean editMode = false;

	/**
	 * boolean value for providing Add/Edit form in one JSP.
	 */
	private boolean scrollerMode = true;

	/**
	 * {@link java.lang.String} default value "className"
	 */
	private String sortItem = "className";

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
	 * Sets {@link ome.admin.controller.ITypeEnumController#group}
	 * 
	 * @param exg
	 *            {@link ome.admin.model.Enumeration}
	 */
	public void setEnumeration(Enumeration en) {
		this.enumeration = en;
	}

	/**
	 * Gets {@link ome.admin.model.Enumeration}
	 * 
	 * @return {@link ome.admin.controller.ITypeEnumController#enumeration}
	 */
	public Enumeration getEnumeration() {
		return this.enumeration;
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
					"Enumeration cannot gets values:");
			context.addMessage("enumerations", message);
		}
		return this.enumerationModel;
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
	 * Checks {@link ome.admin.controller.ITypeEnumController#scrollerMode}
	 * 
	 * @return boolean
	 */
	public boolean isScrollerMode() {
		return this.scrollerMode;
	}

	/**
	 * Sets {@link ome.admin.controller.ITypeEnumController#scrollerMode}
	 * 
	 * @param scrollerMode
	 *            boolean
	 */
	public void setScrollerMode(boolean scrollerMode) {
		this.scrollerMode = scrollerMode;
	}

	/**
	 * Provides action for navigation rule "addNewEnumeration" what is described in
	 * the faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success"
	 */
	public String addNewEnumeration() {
		return NavigationResults.FALSE;
	}

	/**
	 * Provides action for navigation rule "addEnumeration" what is described in the
	 * faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success"
	 */
	public String addEnumeration() {
		return NavigationResults.FALSE;
	}

	/**
	 * Provides action for navigation rule "editEnumeration" what is described in the
	 * faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success" or "false"
	 */
	public String editGroup() {
		return NavigationResults.FALSE;
	}

	/**
	 * Provides action for navigation rule "deleteEnumeration" what is described in
	 * the faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success" or "false"
	 */
	public String delGroup() {
		return NavigationResults.FALSE;
	}

	/**
	 * Provides action for navigation rule "updateEnumeration" what is described in
	 * the faces-config.xml file.
	 * 
	 * @return {@link java.lang.String} "success" or "false"
	 */
	public String updateGroup() {
		return NavigationResults.FALSE;
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
	 * {@link ome.admin.model.Enumeration} wrapped by this
	 * {@link javax.faces.model.DataModel}
	 * 
	 * @param event
	 *            {@link javax.faces.event.ActionEvent} object from the
	 *            specified source component and action command.
	 * @return {@link ome.admin.controller.ITypeEnumController}
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

	/**
	 * Provides validaton for {@link ome.admin.model.Enumeration#getClassName()}.
	 * Cannot exist the same group names.
	 * 
	 * @param context
	 *            {@link javax.faces.context.FacesContext}
	 * @param toValidate
	 *            {@link javax.faces.component.UIComponent}
	 * @param value
	 *            {@link java.lang.Object}
	 */
	public void validateClassName(FacesContext context, UIComponent toValidate,
			Object value) {
		/*
		 * String name = (String) value; if (itype.checkEnumeration(name) &&
		 * !name.equals(this.group.getName())) { ((UIInput)
		 * toValidate).setValid(false); FacesMessage message = new
		 * FacesMessage("Groupname already exist");
		 * context.addMessage(toValidate.getClientId(context), message); }
		 */
	}
}
