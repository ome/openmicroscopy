 /*
 * org.openmicroscopy.shoola.agents.editor.actions.OpenFileAction 
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
package org.openmicroscopy.shoola.agents.editor.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
public class OpenFileAction
	extends EditorAction {

	
	/** The description of the action. */
    private static final String NAME = "Open File";
    
	 /** The description of the action. */
    private static final String DESCRIPTION = "File not found.";
    
    /**
     * The file that this Action opens. 
     */
    private File 		file;
    
    
    /** Creates a new instance.
     * Action not enabled until {@link #setFile(String)} is called. 
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
   public OpenFileAction(Editor model)
   {
       super(model);
       setEnabled(false);		// not enabled until setFile(File) is called.
       setName(NAME);
       setDescription(DESCRIPTION);
       setIcon(IconManager.LINK_LOCAL_ICON);
   }
   
   /**
    * Sets the file that this Action will open.
    * This Action is disabled until this method is called and the file is found.
    * 
    * @param filePath		The absolute path of the file.
    */
   public void setFile(String filePath) 
   {
	   if (filePath == null) return;
	   
	   file = new File(filePath);
	   setName(file.getName());
	   
	   if (file.exists()) {
		   setDescription("Open file at " + filePath);
		   setEnabled(true);
	   } 
	   else {
		   setDescription("File not found at " + filePath);
		   setEnabled(false);
	   }
   }
   
   /**
    * Brings up on screen the {@link TreeViewer}.
    * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
    */
   public void actionPerformed(ActionEvent e) 
   {
	   model.discard();
   }
   
   /** 
    * Reacts to state changes in the {@link Editor}. 
    * @see ChangeListener#stateChanged(ChangeEvent)
    */
   public void stateChanged(ChangeEvent e)
   {
       onStateChange();
   }
   
}
