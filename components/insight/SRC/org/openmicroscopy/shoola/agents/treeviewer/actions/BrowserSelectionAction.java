/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserSelectionAction
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
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;

/** 
 * Selects the type of <code>Browser</code>. Three types are actually 
 * implemented and defined by the following constants:
 * {@link Browser#PROJECTS_EXPLORER}, {@link Browser#IMAGES_EXPLORER},
 * {@link Browser#TAGS_EXPLORER} and {@link Browser#SCREENS_EXPLORER}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class BrowserSelectionAction
    extends TreeViewerAction
{
    
    /** The type of browser. */
    private int browserType;
    
    /**
     * Checks if the specified type is a valid browser's type.
     * 
     * @param type The specified type.
     */
    private void checkType(int type)
    {
        IconManager im = IconManager.getInstance();
        switch (type) {
            case Browser.PROJECTS_EXPLORER:
                putValue(Action.NAME, Browser.HIERARCHY_TITLE);
                putValue(Action.SMALL_ICON,
                        im.getIcon(IconManager.HIERARCHY_EXPLORER));
                break;
            case Browser.IMAGES_EXPLORER:
                putValue(Action.NAME, Browser.IMAGES_TITLE);
                putValue(Action.SMALL_ICON,
                im.getIcon(IconManager.IMAGES_EXPLORER));
                break;
            case Browser.TAGS_EXPLORER:
                putValue(Action.NAME, Browser.TAGS_TITLE);
                putValue(Action.SMALL_ICON,
                    im.getIcon(IconManager.TAGS_EXPLORER));
                break;
            case Browser.SCREENS_EXPLORER:
                putValue(Action.NAME, Browser.SCREENS_TITLE);
                putValue(Action.SMALL_ICON,
                        im.getIcon(IconManager.SCREENS_EXPLORER));
                break;
            case Browser.FILES_EXPLORER:
                putValue(Action.NAME, Browser.FILES_TITLE);
                putValue(Action.SMALL_ICON,
                        im.getIcon(IconManager.FILES_EXPLORER));
                break;
            case Browser.FILE_SYSTEM_EXPLORER:
                putValue(Action.NAME, Browser.FILE_SYSTEM_TITLE);
                putValue(Action.SMALL_ICON,
                        im.getIcon(IconManager.FILE_SYSTEM_EXPLORER));
                break;
            default:
                throw new IllegalArgumentException("Browser type not " +
                        "supported: "+type);
        }   
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param browserType 	The Browser's type. One of the following constants:
     * 						{@link Browser#PROJECTS_EXPLORER}, 
     * 						{@link Browser#IMAGES_EXPLORER},
     * 						{@link Browser#FILES_EXPLORER}
     * 						{@link Browser#TAGS_EXPLORER} and 
     * 						{@link Browser#SCREENS_EXPLORER}.
     * 						 
     */
    public BrowserSelectionAction(TreeViewer model, int browserType)
    {
        super(model);
        setEnabled(true);
        checkType(browserType);
        this.browserType = browserType;
    }
    
    /**
     * Adds a new browser to the {@link TreeViewer}.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    { 
        model.displayBrowser(browserType);
    }
    
}
