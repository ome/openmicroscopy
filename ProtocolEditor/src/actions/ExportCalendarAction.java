
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

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;

import ui.IModel;
import ui.components.FileChooserReturnFile;
import util.ImageFactory;


public class ExportCalendarAction 
	extends ProtocolEditorAction {
	
	public ExportCalendarAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Export to iCalendar");
		putValue(Action.SHORT_DESCRIPTION, "Export the Date-Time fields in this file to iCalendar (.ics) file");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.CALENDAR_EXPORT_ICON)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		
		// Create a file chooser
		JFileChooser fc = new JFileChooser();
		
		int returnVal = fc.showSaveDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();  // this may be a directory! 
			
            if (file.isDirectory()) {
                JOptionPane.showMessageDialog(frame, "Please choose a file name (not a directory)");
                // try again! 
                actionPerformed(e);
                return;
            }
            
			String filePath = file.getAbsolutePath();
			if (!(filePath.endsWith(".ics"))) {
				filePath = filePath + ".ics";
			}
			
			model.exportFileEventsToICalendar(filePath);
		}
	}
	
	
	
	public void stateChanged(ChangeEvent e) {
		
		/*
		 * This action should only be enabled if a file is open
		 */
		setEnabled(filesOpen());
	
	}

}
