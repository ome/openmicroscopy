 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ParamEditorDialog 
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs;

//Java imports

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.FieldPanel;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * A dialog for hosting a component for editing a parameter. 
 * The parent that creates this dialog is added as a 
 * {@link PropertyChangeListener} to the dialog, to handle edits etc. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ParamEditorDialog 
	extends JDialog 
	implements ActionListener,
	PropertyChangeListener
{
	/**
	 * The UI component this dialog holds. 
	 */
	private JComponent 			paramEditor;
	
	/**
	 * Creates an instance of a non-modal, undecorated dialog. 
	 * Builds the UI but does not show the dialog. 	
	 * 
	 * @param param				The parameter to edit with this dialog
	 * @param mouseLocation		The location to show dialog. Can be null. 
	 * @param parent			Responsible for listening for edits. 
	 */
	public ParamEditorDialog(IParam param, Point mouseLocation, 
			PropertyChangeListener parent) {
		
		super();
		setModal(false);
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setUndecorated(true);
		
		// when focus is lost, dialog is closed. 
		addWindowFocusListener(new WindowListener());
		
		Box horizontalBox = Box.createHorizontalBox();
		
		paramEditor = ParamUIFactory.getEditingComponent(param);
		if (paramEditor != null) {
			paramEditor.addPropertyChangeListener
				(ITreeEditComp.VALUE_CHANGED_PROPERTY, parent);
			paramEditor.addPropertyChangeListener
				(FieldPanel.UPDATE_EDITING_PROPERTY, this);
			horizontalBox.add(paramEditor);
		}
		
		IconManager iM = IconManager.getInstance();
		Icon closeIcon = iM.getIcon(IconManager.FILE_CLOSE_ICON);
		JButton closeButton = new CustomButton(closeIcon);
		closeButton.addActionListener(this);
		horizontalBox.add(closeButton);
		
		add(horizontalBox);
		pack();
		if (mouseLocation != null) {
			setLocation(mouseLocation);
		}
	}

	/**
	 * Implemented as specified by the {@link ActionListener} interface
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		this.dispose();
	}

	/**
	 * Implemented as specified by the {@link PropertyChangeListener} interface
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		
		// refresh the size of the UI.
		if (FieldPanel.UPDATE_EDITING_PROPERTY.equals(evt.getPropertyName())) {
			pack();
		}
	}
	
	/**
	 * A Window Listener that disposes the dialog when focus is lost. 
	 * 
	 * @author will
	 *
	 */
	public class WindowListener extends WindowAdapter {
		
		/** 
		 * Calls {@link dispose} when focus lost. 
		 * 
		 * @see WindowFocusListener#windowLostFocus(WindowEvent)
		 */
		public void windowLostFocus(WindowEvent e) { dispose(); }
	}
	
}
