 /*
 * actions.ExportAllWikiAction 
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
package actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;

import tree.DataFieldNode;
import ui.IModel;
import util.ImageFactory;
import cmd.ActionCmd;
import cmd.ExportHtmlCmd;
import cmd.ExportWikiCmd;

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

public class ExportAllWikiAction extends ProtocolEditorAction {
	
	public ExportAllWikiAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Export to Wiki");
		putValue(Action.SHORT_DESCRIPTION, "Exports the entire document to wiki page");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.WWW_ICON)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		
		DataFieldNode rootNode = model.getRootNode();
		
		ActionCmd exportWiki = new ExportWikiCmd(rootNode);
		exportWiki.execute();
	}
	
	public void stateChanged(ChangeEvent e) {
		
		String[] fileList = model.getOpenFileList();
		
		this.setEnabled(!(fileList.length == 0));
	}
	
}
