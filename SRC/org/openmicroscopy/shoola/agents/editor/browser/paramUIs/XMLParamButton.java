 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.XMLParamButton 
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.XMLFieldContent;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;

/** 
 * This is a UI component, displayed in a {@link FieldPanel} when the
 * field represents 'foreign' or 'custom' XML element. 
 * The data is modeled by a single {@link XMLFieldContent}, which contains the
 * attribute map of the XML element. 
 * This UI displays a button, which launches a pop-up {@link XMLParamDialog}
 * for editing the attributes. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class XMLParamButton
	extends JPanel 
{
	/**	
	 * The parent UI which handles the edits of the {@link #dialog}
	 * A reference to the parent is passed to the {@link XMLParamDialog}
	 * constructor.
	 */
	private PropertyChangeListener 		parent; 
	
	/** The object that holds the attribute map of the XML element */
	private XMLFieldContent 			param;
	
	/** The dialog for editing the XML attributes */
	private XMLParamDialog 				dialog;
	
	/**
	 * Creates an instance and builds the UI;
	 * 
	 * @param xmlParam		The data we're editing
	 * @param parent
	 */
	public XMLParamButton(IAttributes xmlParam, PropertyChangeListener parent)
	{
		this.parent = parent;
		this.param = (XMLFieldContent)xmlParam;
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(null);
		
		// button for launching the dialog
		IconManager iM = IconManager.getInstance();
		Icon xmlEdit = iM.getIcon(IconManager.EDIT_XML_ICON);
		JButton xmlParamButton = new CustomButton(xmlEdit);
		
		// disable if there are no attributes to edit
		if (param.getAttributeNames().length == 0) {
			xmlParamButton.setEnabled(false);
		} else {
			xmlParamButton.addMouseListener(new ParamMouseListener());
		}
		
		add(xmlParamButton);
	}
	
	/**
	 * This MouseListener launches the dialog at the point of the mouse, when
	 * the button is clicked
	 * 
	 * @author will
	 */
	private class ParamMouseListener extends MouseAdapter
    {
		/**
		 * Launches the attribute-editor dialog
		 * 
		 * @see MouseAdapter#mouseClicked(MouseEvent)
		 */
    	public void mouseClicked(MouseEvent e) 
    	{
    		if (dialog == null)
    			dialog = new XMLParamDialog(param, null, parent);
    		
    		Point mouseLoc = e.getPoint();
    		Point paneLoc = getLocationOnScreen();
			// give the exact location of the mouse
			if (mouseLoc != null)
				paneLoc.translate((int)mouseLoc.getX(), 
									(int)mouseLoc.getY());
			
    		dialog.setLocation(paneLoc);
    		dialog.setVisible(true);
    	}
    }
	
}
