/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.UploadScriptAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.actions;

//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.ui.ScriptUploaderDialog;
import org.openmicroscopy.shoola.env.data.model.ScriptActivityParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Action to bring the a dialog to pick the a script to upload.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class UploadScriptAction 
	extends TreeViewerAction
{

	/** The name of the action. */
	private static final String NAME = "Upload Script...";
	
	/** The description of the action. */
	private static final String DESCRIPTION = "Upload a script to the server.";
	
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	public UploadScriptAction(TreeViewer model)
	{
		super(model);
		setEnabled(true);
		name = NAME;
		putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.UPLOAD_SCRIPT));
	}
	
    /**
     * Brings up the dialog to upload the script.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    { 
    	ScriptUploaderDialog dialog = new ScriptUploaderDialog(model.getUI(), TreeViewerAgent.getRegistry(), model.getSecurityContext());
    	dialog.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				Object o = evt.getNewValue();
				if (o instanceof ScriptObject &&
					TreeViewerAgent.isAdministrator()) {
					ScriptObject script = (ScriptObject) o;
					UserNotifier un = 
						TreeViewerAgent.getRegistry().getUserNotifier();
					ScriptActivityParam p = new ScriptActivityParam(script, 
							ScriptActivityParam.UPLOAD);
					un.notifyActivity(TreeViewerAgent.getAdminContext(), p);
				}
			}
		});
    	UIUtilities.centerAndShow(dialog);
    }
    
}
