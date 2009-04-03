 /*
 * org.openmicroscopy.shoola.agents.editor.actions.OpenLocalFileAction 
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

//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;
import org.openmicroscopy.shoola.util.filter.file.XMLFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

/** 
 * Action allows user to choose a file from their local machine, to be 
 * opened by the Editor.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class OpenLocalFileAction 
	extends EditorAction
	implements PropertyChangeListener
{

	/** The description of the action. */
    private static final String 	NAME = "Open File";
    
	 /** The description of the action. */
    private static final String 	DESCRIPTION = 
    								"Open a Local File on your computer";
    
    /** Collection of supported file formats. */
	private List<FileFilter>		filters;
	
	/** The file chooser.  */
	private FileChooser		 		chooser;
    
    /** Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
   public OpenLocalFileAction(Editor model)
   {
       super(model);
       setEnabled(true);
       setName(NAME);
       setDescription(DESCRIPTION);
       setIcon(IconManager.OPEN_FOLDER);
       
       filters = new ArrayList<FileFilter>();
       filters.add(new EditorFileFilter());
       filters.add(new XMLFilter());
   }
   
   /**
    * Brings up on screen the {@link FileChooser}.
    * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
    */
   public void actionPerformed(ActionEvent e) 
   {
	   chooser = new FileChooser(null, FileChooser.LOAD, 
				"Open File", "Choose a file to open in the Editor", 
				filters);
		chooser.addPropertyChangeListener(
				FileChooser.APPROVE_SELECTION_PROPERTY, this);
		UIUtilities.centerAndShow(chooser);
   }

   /**
    * Responds to the user choosing a file to open.
    * Calls {@link Editor#openLocalFile(File)}
    * 
    * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
    */
   public void propertyChange(PropertyChangeEvent evt) 
   {
	   String name = evt.getPropertyName();
		if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
			File f = (File) evt.getNewValue();
			
			FileFilter filter = chooser.getSelectedFilter();
			
			// only allow accepted files to be opened.
			// User must actually choose XML filter to open non-Editor file. 
			if (filter.accept(f))
				model.openLocalFile(f);
			// TODO show (or don't hide) the file-chooser if file not accepted.
		}
   }
}
