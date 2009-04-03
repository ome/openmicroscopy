 /*
 * ui.MyTaskBarManager 
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
package ui;

import javax.swing.JFrame;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * A subclass of TaskBarManager that contains a reference to the 
 * XMLView UI class.
 * The doExit() method is overridden to call the 
 * tryQuitApp() in the view, which closes all saved files, and returns
 * false if the user wants to save any unsaved files.
 * Otherwise, if true, the superclass doExit() method is called. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MyTaskBarManager 
	extends TaskBarManager {
	
	XMLView view;
	
	/**
	 * Creates an instance of this class.
	 * 
	 * @param frame		A Frame to add the quit listeners to.
	 * @param view		The UI, for handling the tryQuitApp().
	 */
	public MyTaskBarManager(JFrame frame, XMLView view) {
		
		super (frame);
		
		this.view = view;
	}

	/**
	 * Calls the XMLView.tryQuitApp().
	 * If this returns true, then super.doExit() is called, to quit the app. 
	 */
	protected void doExit() {
		
		if (view.tryQuitApp()) {
			
			super.doExit();
		}
	}
}
