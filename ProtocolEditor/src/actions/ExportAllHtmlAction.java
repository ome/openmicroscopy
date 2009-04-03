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
import javax.swing.event.ChangeEvent;

import cmd.ActionCmd;
import cmd.ExportHtmlCmd;

import tree.DataFieldNode;
import ui.IModel;
import util.ImageFactory;

public class ExportAllHtmlAction extends ProtocolEditorAction {
	
	public ExportAllHtmlAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Export the whole document to HTML");
		putValue(Action.SHORT_DESCRIPTION, "Exports the entire document to html for printing");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.WWW_ICON)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		
		DataFieldNode rootNode = model.getRootNode();
		
		ActionCmd printAll = new ExportHtmlCmd(rootNode);
		printAll.execute();
	}
	
	public void stateChanged(ChangeEvent e) {
		
		String[] fileList = model.getOpenFileList();
		
		this.setEnabled(!(fileList.length == 0));
	}
	
}
