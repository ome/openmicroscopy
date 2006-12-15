/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.RemoveCmd
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

package org.openmicroscopy.shoola.agents.hiviewer.cmd;




//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import pojos.DataObject;

/** 
 * Command to removes the selected nodes from the container.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class RemoveCmd
    implements ActionCmd
{

    /** Reference to the model. */
    private HiViewer        model;
    
    /**
     * Creates a new instance.
     * 
     * @param model A reference to the model. Mustn't be <code>null</code>.
     */
    public RemoveCmd(HiViewer model)
    {
        if (model == null) throw new IllegalArgumentException("No model");
        this.model = model;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        Browser browser = model.getBrowser();
        if (browser == null) return;
        Set nodes = browser.getSelectedDisplays();
        if (nodes.size() == 0) return;
        ImageDisplay n;
        Iterator i = nodes.iterator();
        List toRemove = new ArrayList();
        while (i.hasNext()) {
            n = (ImageDisplay) i.next();
            if (n.getHierarchyObject() instanceof DataObject)
                toRemove.add(n);
        }
        if (toRemove.size() != 0) model.removeObjects(toRemove);
    }

}
