/*
 * org.openmicroscopy.shoola.agents.hiviewer.actions.ClassifyAction
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
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clsf.Classifier;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ClassifyCmd;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ImageData;

/** 
 * Brings up the classification widget if required.
 * This action is enabled if the hierarchy object is an image.
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
public class ClassifyAction
    extends HiViewerAction
{

    /** Name of the action. */
    private static final String NAME = "Add to category";
    
    /** Description of the action. */
    private static final String DESCRIPTION = "Classify the selected image" +
            " in a category.";
    
    
    /**
     * Sets the action enabled depending on the currently selected display
     * @see HiViewerAction#onDisplayChange(ImageDisplay)
     */
    protected void onDisplayChange(ImageDisplay selectedDisplay)
    {
        if (selectedDisplay.getParentDisplay() == null) setEnabled(false);
        else {
            Object ho = selectedDisplay.getHierarchyObject();
            setEnabled(ho instanceof ImageData);
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public ClassifyAction(HiViewer model)
    {
        super(model);
        putValue(Action.NAME, NAME);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
    }

    /**
     * Creates a {@link ClassifyCmd} command to execute the action.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
       ClassifyCmd cmd = new ClassifyCmd(model, Classifier.CLASSIFICATION_MODE);
       cmd.execute();
    }

}
