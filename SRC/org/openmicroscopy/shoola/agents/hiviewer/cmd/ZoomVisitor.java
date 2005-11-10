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

    /** The error factor. */
    private static final int EXTRA_PIXEL = 1;
    
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
     */
    public void visit(ImageNode node)
    {
        Rectangle r = node.getBounds();
        Thumbnail th = node.getThumbnail();
        double sf = th.getScalingFactor();
        double factor = ZoomCmd.calculateFactor(sf);
        if (sf != factor) {
            th.scale(factor);
            double ratio = factor/sf;
            node.setLocation((int) (r.x*ratio), (int) (r.y*ratio));
        }
    }

    /** Required by I/F, no-op performed in this case. */
    public void visit(ImageSet node)
    {
        //Trick to show the scrollBar on screen
        Dimension d = node.getSize();
        node.setSize(d.width+EXTRA_PIXEL, d.height+EXTRA_PIXEL);
    }

}
