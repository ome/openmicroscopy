 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.EditorLinkPreview 
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate;

//Java imports

import java.awt.BorderLayout;

import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.BrowserControl;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.AbstractParamEditor;
import org.openmicroscopy.shoola.agents.editor.model.params.EditorLinkParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.preview.EditorPreview;
import org.openmicroscopy.shoola.agents.editor.view.EditorFactory;

/** 
 * This UI displays a Preview of an Editor file, linked to on the server (by ID)
 * or locally (by file path).
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class EditorLinkPreview 
	extends AbstractParamEditor {
	
	public EditorLinkPreview(IParam param, BrowserControl controller) 
	{
		super(param);
		
		setController(controller);
		
		setLayout(new BorderLayout());
		setBackground(null);
		
		// only display one linked file. 
		// If more are linked (in table), these will be ignored. 
		String link = param.getParamValue();
		
		
		EditorPreview preview;	
		
		// if the link is an ID (on server)...
		boolean idValid =  EditorLinkParam.isLinkValidId(link);
		if (idValid) {
			long fileID = new Long(link);
			preview = new EditorPreview(fileID, controller);
		} else {
			// otherwise assume that link is a file path
			preview = new EditorPreview(link, controller);
		}
		
		// add the preview UI. 
		add(preview.getUI(), BorderLayout.CENTER);
	}

	public String getEditDisplayName() {
		return "Editor Link";
	}

}
