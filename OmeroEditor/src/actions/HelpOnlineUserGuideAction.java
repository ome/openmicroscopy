 /*
 * actions.HelpOnlineUserGuide 
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

//Java imports

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;


//Application-internal dependencies

import tree.Tree.Actions;
import ui.IModel;
import util.BareBonesBrowserLaunch;
import util.ImageFactory;

/** 
 * This Action opens the online information about OMERO.edtior in a web browser.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class HelpOnlineUserGuideAction 
	extends ProtocolEditorAction {
	
	/**
	 * The URL for the OMERO.editor wiki pages, user guide etc. 
	 */
	public static final String OMERO_EDITOR_URL = 
		"http://trac.openmicroscopy.org.uk/shoola/wiki/OmeroEditor";
	
	public HelpOnlineUserGuideAction (IModel model) {

		super(model);
	
		putValue(Action.NAME, "Open Online User Guide");
		putValue(Action.SHORT_DESCRIPTION, "View the OMERO.editor user guide" +
				"in a web browser.");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance()
				.getIcon(ImageFactory.WWW_ICON)); 
	}
	
	/**
	 * Open the OMERO_EDITOR_URL in a browser.
	 */
	public void actionPerformed(ActionEvent e) {
		
		BareBonesBrowserLaunch.openURL
			(OMERO_EDITOR_URL);
	}
	
	
	
	public void stateChanged(ChangeEvent e) {
	
	}

}
