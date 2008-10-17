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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;

import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
	ITreeEditComp,
	PropertyChangeListener {

	private AbstractParamEditor paramEditor;
	
	public ParamEditorDialog(IParam param, Point mouseLocation, 
			PropertyChangeListener parent) {
		
		super();
		setModal(false);
		setUndecorated(true);
		
		Box horizontalBox = Box.createHorizontalBox();
		
		paramEditor = ParamUIFactory.getEditingComponent(param);
		if (paramEditor != null) {
			paramEditor.addPropertyChangeListener
				(ITreeEditComp.VALUE_CHANGED_PROPERTY, parent);
			horizontalBox.add(paramEditor);
		}
		
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		horizontalBox.add(closeButton);
		
		add(horizontalBox);
		pack();
		if (mouseLocation != null) {
			setLocation(mouseLocation);
		}
	}

	public void actionPerformed(ActionEvent e) {
		this.dispose();
	}

	public void attributeEdited(String attributeName, Object newValue) {
		if (attributeName == null) return;

		String oldValue = getParameter().getAttribute(attributeName);
		
		firePropertyChange(ITreeEditComp.VALUE_CHANGED_PROPERTY,
				oldValue, newValue);
	}

	public String getAttributeName() {
		if (paramEditor != null)
			return paramEditor.getAttributeName();
		return null;
	}

	public String getEditDisplayName() {
		if (paramEditor != null)
			return paramEditor.getEditDisplayName();
		return null;
	}

	public IAttributes getParameter() {
		if (paramEditor != null)
			return paramEditor.getParameter();
		return null;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (ITreeEditComp.VALUE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
			attributeEdited(getAttributeName(), evt.getNewValue());
		}
	}
	
	
}
