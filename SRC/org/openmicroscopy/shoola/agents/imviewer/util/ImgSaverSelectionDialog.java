/*
 * org.openmicroscopy.shoola.agents.imviewer.util.ImgSaverSelectionDialog
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

package org.openmicroscopy.shoola.agents.imviewer.util;



//Java imports
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.OptionsDialog;


/** 
 * Brings up the preview image widget if the answer to the question is 
 * <code>yes</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ImgSaverSelectionDialog
    extends OptionsDialog
{
    
    /** The message when an image with the name and extension aleady exists. */
    private static final String MESSAGE = "A file with the same name and " +
                                "extension already exists in this directory. " +
                                "Do you really want to save the image?";
    
    /** Reference to the {@link ImgSaver}. */
    private ImgSaver    model;
    
    /**
     * Overridden to bring up the preview image widget.
     * @see OptionsDialog#onYesSelection()
     */
    protected void onYesSelection() { model.previewImage(false); }
    
    /**
     * Creates a new instance.
     * 
     * @param parent    The parent of this dialog.
     * @param icon      The icon displayed next to the message.
     */
    ImgSaverSelectionDialog(ImgSaver parent, Icon icon)
    {
        super(parent, "Save Image", MESSAGE, icon);
        if (parent == null) throw new IllegalArgumentException("No model.");
        model = parent;
    }

}
