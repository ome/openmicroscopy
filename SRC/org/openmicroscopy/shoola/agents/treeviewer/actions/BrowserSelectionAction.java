/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserSelectionAction
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
 * implemented and defined by the following contants:
 * {@link Browser#CATEGORY_EXPLORER}, {@link Browser#PROJECT_EXPLORER} and
 * {@link Browser#IMAGES_EXPLORER}.
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
            case Browser.PROJECT_EXPLORER:
                putValue(Action.NAME, Browser.HIERARCHY_TITLE);
                putValue(Action.SMALL_ICON,
                        im.getIcon(IconManager.HIERARCHY_EXPLORER));
                break;
            case Browser.CATEGORY_EXPLORER:
                putValue(Action.NAME, Browser.CATEGORY_TITLE);
                putValue(Action.SMALL_ICON,
                    im.getIcon(IconManager.CATEGORY_EXPLORER));
                break;
            case Browser.IMAGES_EXPLORER:
                putValue(Action.NAME, Browser.IMAGES_TITLE);
                putValue(Action.SMALL_ICON,
                im.getIcon(IconManager.IMAGES_EXPLORER));
                break;
            default:
                throw new IllegalArgumentException("Browser type not " +
                        "supported.");
        }   
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param browserType 	The Browser's type. One of the following constants:
     * 						{@link Browser#CATEGORY_EXPLORER},
     * 						{@link Browser#PROJECT_EXPLORER} and 
     * 						{@link Browser#IMAGES_EXPLORER}.
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
        model.addBrowser(browserType);
    }
    
}
