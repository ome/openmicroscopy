package actions;

/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;

import ui.IModel;
import util.ImageFactory;


public class MultiplyValuesAction extends ProtocolEditorAction {
	
	public MultiplyValuesAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Multiply Values by...");
		putValue(Action.SHORT_DESCRIPTION, "Multiply selected Numerical values by a factor of...");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.EDU_MATHS)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		multiplyValueOfSelectedFields();
	}
	
	
	
	public void stateChanged(ChangeEvent e) {
		
		String[] fileList = model.getOpenFileList();
		
		this.setEnabled(!(fileList.length == 0));
	}
	
	
	public void multiplyValueOfSelectedFields() {
		String s = (String)JOptionPane.showInputDialog(
                frame,
                "Multiply all selected field values by:\n"
                + "(enter a number)\n NB: This will only apply to NUMBER fields \n"
                + "To divide, use /, eg '/3' ",
                "Enter a number",
                JOptionPane.QUESTION_MESSAGE);
		
		if (s != null && s.length() > 0) {
			boolean division = false;
			if (s.startsWith("/")) {
				s = s.substring(1);
				division = true;
			}
			
			float factor;
			try {
				factor = Float.parseFloat(s);
				if (division) model.multiplyValueOfSelectedFields(1/factor);
				else model.multiplyValueOfSelectedFields(factor);
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(frame, 
						"You didn't enter a valid number", 
						"Invalid multiplication factor", 
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
