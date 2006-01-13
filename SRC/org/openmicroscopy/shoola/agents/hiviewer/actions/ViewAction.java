/*
 * org.openmicroscopy.shoola.agents.hiviewer.actions.ViewAction
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

package org.openmicroscopy.shoola.agents.hiviewer.actions;



//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ImageData;

/** 
 * Views or browses the selected node according to the hierarchy object type.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ViewAction
    extends HiViewerAction
{

    /** Name of the action. */
    private static final String VIEW = "View";
    
    /** Name of the action. */
    private static final String BROWSE = "Browse";
    
    /** Description of the action. */
    private static final String DESCRIPTION = "View the selected image or" +
            "browse the selected project, dataset, categoryGroup or category";

    /**
     * Sets the action enabled depending on the currently selected display
     * @see HiViewerAction#onDisplayChange(ImageDisplay)
     */
    protected void onDisplayChange(ImageDisplay selectedDisplay)
    {
        if (selectedDisplay.getParentDisplay() == null) setEnabled(false);
        else {
            Object ho = selectedDisplay.getHierarchyObject();
            if ((ho instanceof ImageData)) putValue(Action.NAME, VIEW);   
            else putValue(Action.NAME, BROWSE);
            setEnabled(true);
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public ViewAction(HiViewer model)
    {
        super(model);
        putValue(Action.NAME, VIEW);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.VIEWER)); 
    }
 
    /** 
     * Creates a {@link ViewCmd} command to execute the action. 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
       ViewCmd cmd = new ViewCmd(model);
       cmd.execute();
    }

}
