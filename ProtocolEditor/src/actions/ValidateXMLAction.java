
/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;

import ui.IModel;
import ui.XMLView;
import util.ImageFactory;
import validation.SAXValidator;
import validation.XMLSchema;
import xmlMVC.ConfigConstants;

public class ValidateXMLAction 
	extends ProtocolEditorAction {
	
	public ValidateXMLAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Validate File");
		putValue(Action.SHORT_DESCRIPTION, "Validates the current file as an XML document against its XML schema.");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.VALIDATION_ICON)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		
		File xmlFile = new File(ConfigConstants.OMERO_EDITOR_FILE  + "/tempFile");
		model.exportTreeToXmlFile(xmlFile);
		List<String> errorMsgs = SAXValidator.validateXML(xmlFile);
	
		String schemaLocation = SAXValidator.getRootElementAttribute(xmlFile, XMLSchema.SCHEMA_LOCATION);
		
		xmlFile.delete();
		
		if (errorMsgs.isEmpty()) {
			JOptionPane.showMessageDialog(frame, "The current file is valid against the schema at \n" +
					schemaLocation, 
					"File Valid", JOptionPane.INFORMATION_MESSAGE);
		}
		
		else {
			String displayMessage = "The current file is not valid, using the schema at\n" +
					schemaLocation + "\nValidation errors:\n \n";
			for (String message: errorMsgs) {
				displayMessage += message + "\n";
			}
			
			JOptionPane.showMessageDialog(frame, displayMessage, "File Not Valid", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	
	public void stateChanged(ChangeEvent e) {
		
		/*
		 * This action should only be enabled if a file is open.
		 */
		setEnabled(filesOpen());
	
	}

}
