/*
 * org.openmicroscopy.shoola.agents.treeviewer.clsf.AddWin
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

package org.openmicroscopy.shoola.agents.treeviewer.clsf;


//Java imports
import java.util.Set;



//Third-party libraries

//Application-internal dependencies
import pojos.ImageData;

/** 
 * The component hosting the possible categories where to classify the image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class AddWin
    extends Classifier
{

    /** Text displayed in the title panel. */
    private static final String     PANEL_TITLE = "Add To Category";
    
    /** Text displayed in the text panel. */
    private static final String     PANEL_TEXT = "Select to classify.";
    
    /** Text displayed in the note panel. */
    private static final String     PANEL_NOTE = "The image can be classified "+
          "under the following categories. " +
          "Double click on the name to browse the group or the category.";
    
    /** Message displayed if the image is unclassified. */
    private static final String     UNCLASSIFIED_TEXT = "The image cannot be " +
            "classified because there is no category available. ";

    /**
     * Overriden to return the title associated to this component.
     * @see Classifier#getPanelTitle()
     */
    protected String getPanelTitle() { return PANEL_TITLE; }

    /**
     * Overriden to return the tetx associated to this component.
     * @see Classifier#getPanelText()
     */
    protected String getPanelText() { return PANEL_TEXT; }
    
    /**
     * Overriden to return the note associated to this component.
     * @see Classifier#getPanelNote()
     */
    protected String getPanelNote() { return PANEL_NOTE; }

    /**
     * Overriden to return the note associated to this component.
     * @see Classifier#getUnclassifiedNote()
     */
    protected String getUnclassifiedNote() { return UNCLASSIFIED_TEXT; }

    /**
     * Creates a new instance.
     * 
     * @param paths The paths to display.
     * @param image The image to classify.
     */
    AddWin(Set paths, ImageData image)
    {
        super(paths, Classifier.CLASSIFY_MODE, image);
        tree.setSingleSelectionInParent(true);
        buildGUI();
    }
    
}
