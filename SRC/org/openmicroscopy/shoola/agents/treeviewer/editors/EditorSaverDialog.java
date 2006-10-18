/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.EditorSaverDialog
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;





//Java imports
import javax.swing.Icon;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.OptionsDialog;

/** 
 * Asks the user if she/he wants to save the edited data before switching
 * to another item.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class EditorSaverDialog
    extends OptionsDialog
{
    
    /** Bounds poperty to save the data. */
    public static final String  SAVING_DATA_EDITOR_PROPERTY =
                            "savingDataEditor";
    
    /** The message displayed. */
    private static final String MESSAGE = "Do you want to save the modified " +
            "data before selecting a new item?";
    
    /**
     * Overridden to save the data.
     * @see OptionsDialog#onYesSelection()
     */
    protected void onYesSelection()
    {
        firePropertyChange(SAVING_DATA_EDITOR_PROPERTY, Boolean.FALSE, 
                                                        Boolean.TRUE);
        super.onYesSelection();
    }
    
    /**
     * Overridden to save the data
     * @see OptionsDialog#onYesSelection()
     */
    protected void onNoSelection()
    {
        firePropertyChange(SAVING_DATA_EDITOR_PROPERTY, Boolean.TRUE, 
                            Boolean.FALSE);
        super.onYesSelection();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner The owner of the frame.
     * @param icon  The icon displayed next to the message.
     */
    public EditorSaverDialog(JFrame owner, Icon icon)
    {
        super(owner, "Save Edited data", MESSAGE, icon);
    }
}
