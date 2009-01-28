 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.ParamEditor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate;

//Java imports

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.FieldPanel;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.FieldParamsFactory;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.DropDownMenu;

/** 
 * This UI panel is for editing the template attributes of a Parameter. 
 * It has UI components for editing the Parameter name, and changing the type
 * of parameter. However, it delegates the parameter-specific UI to other 
 * classes. E.g. an Enumeration parameter will be edited by the 
 * {@link EnumTemplate} class. 
 * 
 * This class adds it's "parent" {@link PropertyChangeListener} to the 
 * parameter-specific UI editing components, so this class is not involved 
 * in handling parameter edits.
 * However, this class will call its 
 * {@link #firePropertyChange(String, Object, Object)
 * method when the parameter type is changed. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ParamEditor 
	extends JPanel 
	implements PropertyChangeListener
{
	/**
	 * A reference to a parent class that creates this UI and handles the 
	 * parameter edits. It is added as a listener to the parameter-specific
	 * UI components. 
	 */
	private PropertyChangeListener 	parent;
	
	/**
	 * A drop-down menu for changing the type of parameter. E.g. from 
	 * boolean to text-line. 
	 */
	private DropDownMenu 			paramTypeChooser; 
	
	/**
	 * The property associated with changing the type of parameter. 
	 */
	public static final String 		PARAM_TYPE = "paramType";
	
	/**
	 * The parameter that this UI is editing. The {@link #getParameter()} 
	 * method is called by other classes that handle the changing of parameter
	 * type, following a firePropertyChange(PARAM_TYPE, null, newType);
	 */
	private IParam					parameter;
	
	/**
	 * Initialise UI components.
	 */
	private void initialise()
	{
		String[] options = FieldParamsFactory.UI_INPUT_TYPES;
		paramTypeChooser = new DropDownMenu(options);
		// listen for selection changes
		paramTypeChooser.addPropertyChangeListener(DropDownMenu.SELECTION, this);
	}
	
	/**
	 * Build the UI
	 */
	private void buildUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(null);
		
		// add name field
		AttributeEditLine nameEditor = new AttributeEditNoLabel
			(parameter, AbstractParam.PARAM_NAME, "Parameter Name");
		nameEditor.addPropertyChangeListener
			(ITreeEditComp.VALUE_CHANGED_PROPERTY, parent);
	
		// add parameter type chooser, set it's value
		String paramType = parameter.getAttribute(AbstractParam.PARAM_TYPE);
		if (paramType != null) {
			for (int i=0; i<FieldParamsFactory.UI_INPUT_TYPES.length; i++)
				if (paramType.equals(FieldParamsFactory.PARAM_TYPES[i])) {
					paramTypeChooser.setSelectedIndex(i);
				}
		}

		// need somewhere for small icons to show 'required', 'info' etc. 
		Box miniToolBar = Box.createHorizontalBox();
		
		// indicate whether parameter is necessary (required)
		if (parameter.isAttributeTrue(AbstractParam.PARAM_REQUIRED)) {
			Icon red = IconManager.getInstance().getIcon
										(IconManager.RED_ASTERISK_ICON_11);
			JButton requiredButton = new CustomButton(red);
			requiredButton.setToolTipText("This parameter is 'required'");
			miniToolBar.add(requiredButton);
		}
		
		// indicate whether parameter has description (show with toolTip)
		String paramDesc = parameter.getAttribute(AbstractParam.PARAM_DESC);
		if (paramDesc != null) {
			Icon info = IconManager.getInstance().getIcon
										(IconManager.INFO_12_ICON);
			JButton infoButton = new CustomButton(info);
			infoButton.setToolTipText("<html><div style='width:250px; " +
					"padding:2px'>" + "Parameter Description:<br>" + 
					paramDesc + "</div></html>");
			miniToolBar.add(infoButton);
		}
		
		JPanel nameAndTypeContainer = new JPanel(new BorderLayout());
		nameAndTypeContainer.setBackground(null);
		nameAndTypeContainer.setOpaque(false);
		nameAndTypeContainer.add(nameEditor, BorderLayout.CENTER);
		nameAndTypeContainer.add(paramTypeChooser, BorderLayout.EAST);
		nameAndTypeContainer.add(miniToolBar, BorderLayout.WEST);
		
		add(nameAndTypeContainer);
		
		// add the parameter editing component
		JComponent defaultEdit = ParamTemplateUIFactory.
		getEditDefaultComponent(parameter);
		if (defaultEdit != null) {
			add(defaultEdit);
			defaultEdit.addPropertyChangeListener( 
				ITreeEditComp.VALUE_CHANGED_PROPERTY, 
				parent);
			defaultEdit.addPropertyChangeListener
				(FieldPanel.UPDATE_EDITING_PROPERTY, this);
		}
	}
	
	/**
	 * Creates an instance of this UI. 
	 * 
	 * @param param		The parameter this UI edits.
	 * @param parent	The parent that listens for edit changes. 
	 */
	ParamEditor(IParam param, PropertyChangeListener parent) {
		
		this.parameter = param;
		this.parent = parent;
		
		initialise();
		buildUI();
	}
	
	/**
	 * Implemented as specified by the {@link PropertyChangeListener} interface
	 * Handles changes to the drop-down menu for changing parameter type. 
	 * Calls {@link #firePropertyChange(String, Object, Object)} to delegate
	 * the handling of this edit to other classes. 
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		
		// refresh the size of the UI.
		if (FieldPanel.UPDATE_EDITING_PROPERTY.equals(evt.getPropertyName())) {
			// don't need this??
		}
		
		if (DropDownMenu.SELECTION.equals(evt.getPropertyName())) {
			int selectedIndex = paramTypeChooser.getSelectedIndex();
			 String newType = FieldParamsFactory.PARAM_TYPES[selectedIndex];
			 
			 firePropertyChange(PARAM_TYPE, null, newType);
		}
	}
	
	/**
	 * Gets the parameter object that this UI component is editing. 
	 */
	public IParam getParameter()
	{
		return parameter;
	}
}
