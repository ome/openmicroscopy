/*
 * org.openmicroscopy.shoola.agents.hiviewer.saver.ContainerSaverManager
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
 *------------------------------------------------------------------------------s
 */

package org.openmicroscopy.shoola.agents.hiviewer.saver;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Manager of the {@link ContainerSaver}.
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
class ContainerSaverManager
{
    
    /** Reference to the model. */
    private ContainerSaver      model; 
    
    /** Reference to the preview widget. */
    private Preview             preview;
    
    /**
     * Creates a new instance. 
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    ContainerSaverManager(ContainerSaver model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
    }
    
    /** Brings up the selection dialog. */
    void showSelectionDialog()
    {
        IconManager im = IconManager.getInstance();
        SelectionDialog dialog = new SelectionDialog(model,
                                    im.getIcon(IconManager.QUESTION));
        dialog.pack();
        UIUtilities.centerAndShow(dialog);
    }
    
    /** Brings up the preview widget. */
    void showPreview()
    {
        //Hide the model
        model.setVisible(false);
        if (preview == null) preview = new Preview(model);
        preview.pack();
        UIUtilities.centerAndShow(preview);
    }

}
