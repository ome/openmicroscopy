/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.CollapseCmd
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

package org.openmicroscopy.shoola.agents.treeviewer.cmd;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplayVisitor;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class CollapseCmd
    implements ActionCmd
{

    /** Reference to the model. */
    private Browser model;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    public CollapseCmd(Browser model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        CollapseVisitor visitor = new CollapseVisitor(model);
        model.accept(visitor, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
    }
    
}
