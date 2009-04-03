 /*
 * ui.components.CustomHtmlEditAction 
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
package ui.components.htmlActions;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.StyleConstants.FontConstants;
import javax.swing.text.StyledEditorKit.StyledTextAction;

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
public class BoldRedTextAction 
	extends StyledTextAction {

	public BoldRedTextAction(String name) {
		super(name);
	}

	
	public void actionPerformed(ActionEvent e) {
		
		JEditorPane editor = getEditor(e);
	    if (editor != null) {
			StyledEditorKit kit = getStyledEditorKit(editor);
			MutableAttributeSet attr = kit.getInputAttributes();
			boolean bold = (StyleConstants.isBold(attr)) ? false : true;
			SimpleAttributeSet sas = new SimpleAttributeSet();
			
			StyleConstants.setForeground(sas, bold ? Color.red : Color.black);
			StyleConstants.setBold(sas, bold);
			
			setCharacterAttributes(editor, sas, false);
	    }
	}

}
