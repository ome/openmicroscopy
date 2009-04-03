/*
 * org.openmicroscopy.shoola.agents.hiviewer.clsf.AddWin
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

package org.openmicroscopy.shoola.agents.hiviewer.clsf;


//Java imports
import java.util.Set;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies

/** 
 * The <code>ADD</code> dialog widget.
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
class AddWin
    extends ClassifierWin
{
    
    /** Text displayed in the title panel. */
    private static final String     PANEL_TITLE = "Add To Category";
    
    /** Text displayed in the text panel. */
    private static final String     PANEL_TEXT = "Select to classify.";
    
    /** Text displayed in the note panel. */
    private static final String     PANEL_NOTE = 
                                         "The image can be classified "+
                                         "under the following categories." +
                                         "Note that only one category can " +
                                         "be selected within a group.";
    
    /** Message displayed if the image is unclassified. */
    private static final String     UNCLASSIFIED_TEXT = "The image cannot be " +
            "classified because there is no category available.";
    
    /**
     * Overriden to return the title associated to this component.
     * 
     * @see ClassifierWin#getPanelTitle()
     */
    protected String getPanelTitle() { return PANEL_TITLE; }

    /**
     * Overriden to return the tetx associated to this component.
     * 
     * @see ClassifierWin#getPanelText()
     */
    protected String getPanelText() { return PANEL_TEXT; }
    
    /**
     * Overriden to return the note associated to this component.
     * 
     * @see ClassifierWin#getPanelNote()
     */
    protected String getPanelNote() { return PANEL_NOTE; }
    
    /**
     * Overriden to return the note associated to this component.
     * @see ClassifierWin#getUnclassifiedNote()
     */
    protected String getUnclassifiedNote() { return UNCLASSIFIED_TEXT; }

    /**
     * Creates a new instance. 
     * 
     * @param availablePaths    The paths to the images. Mustn't be 
     *                          <code>null</code>.
     * @param owner             The owner of the frame.
     */
    AddWin(Set availablePaths, JFrame owner)
    {
        super(availablePaths, owner);
        tree.setSingleSelectionInParent(true);
        buildGUI();
    }
    
}
