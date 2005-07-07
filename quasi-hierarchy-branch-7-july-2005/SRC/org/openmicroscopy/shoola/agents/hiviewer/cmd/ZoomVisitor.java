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
import java.awt.Dimension;
import java.awt.Rectangle;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;


/** 
 * Zoom in or out the imageNodes of a selected ImageSet.
 * This visitor is accepted by an ImageSet not by the browser.
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

    private static final double EPSILON = 0.1;
    
    /** Creates a new instance. */
    ZoomVisitor(HiViewer model)
    {
        super(model);
    }
    
    /** Scale the imageNode iff the specified scaling factor is different. */
    public void visit(ImageNode node)
    {
        Rectangle r = node.getBounds();
        Thumbnail th = node.getThumbnail();
        double sf = th.getScalingFactor();
        double factor = ZoomCmd.calculateFactor(sf);
        if (sf != factor) {
            th.scale(factor);
            double ratio = factor/sf;
            if (ratio < 1) ratio += EPSILON;
            node.setLocation((int) (r.x*ratio), (int) (r.y*ratio));
        }
    }

    /** Required by I/F, no-op performed in this case. */
    public void visit(ImageSet node)
    {
        //Trick to show the scrollBar on screen
        Dimension d = node.getSize();
        node.setSize((int) (d.getWidth()+10*EPSILON), 
                    (int) (d.getHeight()+10*EPSILON));
    }

}
