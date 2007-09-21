/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.AnnotateAction
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.PropertiesCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Brings the property widget to annotate the data object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class AnnotateAction
    extends TreeViewerAction
{
    
    /** The name of the action. */
    private static final String NAME = "Annotate";
    
    /** 
     * The description of the action if the <code>DataObject</code>
     * is an <code>Image</code> 
     */
    private static final String DESCRIPTION_IMAGE = "Annotate the image.";
    
    /** 
     * The description of the action if the <code>DataObject</code>
     * is a <code>Dataset</code> 
     */
    private static final String DESCRIPTION_DATASET = "Annotate the dataset.";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Annotate the dataset.";
    
    /**
     * Enables or not the action and sets the description depending on
     * the type of the passed object.
     * 
     * @param ho The object to check.
     */
    private void setValuesFor(Object ho)
    {
    	if (ho instanceof ImageData) {
    		setEnabled(true);
    		putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_IMAGE));
            description = DESCRIPTION_IMAGE;
    	} else if (ho instanceof DatasetData) {
    		setEnabled(true);
    		putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_DATASET));
            description = DESCRIPTION_DATASET;
    	} else {
    		setEnabled(false);
    		putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION));
            description = DESCRIPTION;
    	}
    }
    
    /**
     * Callback to notify of a change in the currently selected display
     * in the currently selected 
     * {@link org.openmicroscopy.shoola.agents.treeviewer.browser.Browser}.
     * 
     * @param selectedDisplay The newly selected display node.
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
            setEnabled(false);
            setEnabled(false);
    		putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION));
            description = DESCRIPTION;
            return;
        }
        Browser browser = model.getSelectedBrowser();
        if (browser != null) {
            if (browser.getSelectedDisplays().length > 1) {
            	setValuesFor(selectedDisplay.getUserObject());
                return;
            }
        }
        setValuesFor(selectedDisplay.getUserObject());
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public AnnotateAction(TreeViewer model)
    {
        super(model);
        name = NAME;
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        description = DESCRIPTION;
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.ANNOTATION));
    } 

    /** 
     * Creates a {@link PropertiesCmd} command to execute the action.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	Browser browser = model.getSelectedBrowser();
        if (browser != null) {
        	TreeImageDisplay[] nodes = browser.getSelectedDisplays();
            if (nodes.length > 1) {
            	Set<DataObject> s = new HashSet<DataObject>();
            	Class type = null;
            	TreeImageDisplay node;
            	Object ho;
            	for (int i = 0; i < nodes.length; i++) {
					node = nodes[i];
					ho = node.getUserObject();
					type = ho.getClass();
					if (ho instanceof ImageData || ho instanceof DatasetData)
						s.add((DataObject) ho);
				}
                model.annotate(type, s);
                return;
            }
        }
        PropertiesCmd cmd = new PropertiesCmd(model);
        cmd.execute();
    }

}

