 /*
 * org.openmicroscopy.shoola.agents.editor.preview.EditorPreviewModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.preview;

import java.io.File;

import javax.swing.tree.TreeModel;

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelFactory;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;

import pojos.FileAnnotationData;

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
 * @since 3.0-Beta4
 */
public class EditorPreviewModel {

	/** The annotation object hosting information about the file. */
	private FileAnnotationData 	fileAnnotation;
	
	TreeModel 					treeModel;
	
	void setFileToEdit(File file)
	{
		if (file == null)	return;
		
		// try opening file as recognised OMERO.editor file (pro.xml or cpe.xml)
		try {
			treeModel = TreeModelFactory.getTree(file);
		} catch (ParsingException e) {
			e.printStackTrace();
			Registry reg = EditorAgent.getRegistry();
			UserNotifier un = reg.getUserNotifier();
			un.notifyInfo("Problem with preview", 
					"File was not found or is not valid.\n" + e.getMessage());
		}
	}
	
	TreeModel getModel() { return treeModel; }
	
}
