 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.XMLParamDialog 
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.XMLFieldContent;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;

/** 
 * This is a dialog for editing the attributes of a {@link XMLFieldContent}
 * object, which represents the data in a 'custom' XML element. 
 * It relies on a parent {@link PropertyChangeListener} to handle the edits,
 * and merely applies this listener to the text component for each attribute. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class XMLParamDialog 
	extends JDialog 
{

	/**
	 * Creates an instance of a non-modal dialog. 
	 * Builds the UI but does not show the dialog. 	
	 * 
	 * @param param				The parameter to edit with this dialog
	 * @param mouseLocation		The location to show dialog. Can be null. 
	 * @param parent			Responsible for listening for edits. 
	 */
	public XMLParamDialog(XMLFieldContent param, Point mouseLocation, 
			PropertyChangeListener parent) {
		
		super();
		setModal(false);
		setResizable(false);
		setUndecorated(false);
		// when focus is lost, dialog is closed. 
		addWindowFocusListener(new WindowListener());
		
		String[] attNames = param.getAttributeNames();
		
		
		JPanel UIpanel = new JPanel();
		UIpanel.setBorder(new EmptyBorder(2,2,2,2));
		
		UIpanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		
        
		// put the text field for each attribute in a vertical box
		JComponent attributeUI;
		for (int i=0; i<attNames.length; i++) {
			attributeUI = new TextFieldEditor(param, attNames[i]);
			attributeUI.addPropertyChangeListener(
					ITreeEditComp.VALUE_CHANGED_PROPERTY, parent);
			
			++c.gridy;
			c.gridx = 0;
			c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
	        c.fill = GridBagConstraints.NONE;      //reset to default
	        c.weightx = 0.0;  
	        UIpanel.add(new CustomLabel(attNames[i]), c);
	        
	        c.gridx++;
	        UIpanel.add(Box.createHorizontalStrut(5), c); 
	        c.gridx++;
	        c.gridx++;
	        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
	        c.fill = GridBagConstraints.HORIZONTAL;
	        c.weightx = 1.0;
	        UIpanel.add(attributeUI, c); 
		}
		
		add(UIpanel);
		
		pack();
		if (mouseLocation != null) {
			setLocation(mouseLocation);
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

