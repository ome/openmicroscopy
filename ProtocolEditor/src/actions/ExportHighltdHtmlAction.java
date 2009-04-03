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
import java.util.List;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;

import tree.DataFieldNode;
import ui.IModel;
import util.ImageFactory;
import cmd.ActionCmd;
import cmd.ExportHtmlCmd;

public class ExportHighltdHtmlAction extends ProtocolEditorAction {
	
	public ExportHighltdHtmlAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Export highlighted Fields to HTML");
		putValue(Action.SHORT_DESCRIPTION, "Exports highlighted fields to html for printing");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.WWW_ICON)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		
		List<DataFieldNode> rootNodes = model.getHighlightedFields();
		
		ActionCmd printAll = new ExportHtmlCmd(rootNodes);
		printAll.execute();
	}
	
	
	// disable if no files are open
	public void stateChanged(ChangeEvent e) {
		
		String[] fileList = model.getOpenFileList();
		
		this.setEnabled(!(fileList.length == 0));
	}
}
