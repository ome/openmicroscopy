/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.ZoomVisitor
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;


/** 
 * Magnifies the {@link ImageNode}s contained in the selected {@link ImageSet}.
 * This visitor is accepted by an {@link ImageSet} not by the browser.
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
class ZoomVisitor
    extends HiViewerVisitor
{

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    ZoomVisitor(HiViewer model)
    {
        super(model);
    }
    
    /** 
     * Magnifies the {@link ImageNode} if and only if the 
     * magnification factor has been modified.
     * @see HiViewerVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node)
    {
        Thumbnail th = node.getThumbnail();
        double sf = th.getScalingFactor();
        double factor = ZoomCmd.calculateFactor(sf);
        if (sf != factor) th.scale(factor); 
    }
    
}
