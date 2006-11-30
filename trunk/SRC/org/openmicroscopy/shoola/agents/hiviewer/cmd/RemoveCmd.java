/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.RemoveCmd
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
