/*
 * org.openmicroscopy.shoola.env.data.views.calls.ImageRenderer
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

package org.openmicroscopy.shoola.env.data.views.calls;



//Java imports
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import omeis.providers.re.data.PlaneDef;
import org.openmicroscopy.shoola.env.data.RenderingService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * 
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
public class ImageRenderer
    extends BatchCallTree
{

    /** The rendered image. */
    private BufferedImage       result;
    
    /** Loads the specified tree. */
    private BatchCall           loadCall;
    
    /**
     * Creates a {@link BatchCall} to retrieve the user images.
     * 
     * @param pixelsID  The id of the pixels set the plane belongs to.
     * @param pd        The plane to render.
     * @return          The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final long pixelsID, final PlaneDef pd)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                RenderingService rds = context.getRenderingService();
                result = rds.renderImage(pixelsID, pd);
            }
        };
    } 
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the {@link BufferedImage rendered image}.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }

    /**
     * Creates a new instance.
     * 
     * @param pixelsID  The id of the pixels set the plane belongs to.
     * @param pd        The plane to render.
     */
    public ImageRenderer(long pixelsID, PlaneDef pd)
    {
        if (pixelsID < 0)
            throw new IllegalArgumentException("ID not valid.");
       loadCall = makeBatchCall(pixelsID, pd);
    }
    
}
