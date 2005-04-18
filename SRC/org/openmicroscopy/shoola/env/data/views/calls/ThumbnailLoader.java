/*
 * org.openmicroscopy.shoola.env.data.views.calls.ThumbnailLoader
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Command to load a given set of thumbnails.
 * <p>As thumbnails are retrieved from <i>OMEIS</i>, they're posted back to the 
 * caller through <code>DSCallFeedbackEvent</code>s.  Each thumbnail will be
 * posted in a single event; the caller can then invoke the <code>
 * getPartialResult</code> method to retrieve a <code>BufferedImage</code> for
 * that thumbnail.  The final <code>DSCallOutcomeEvent</code> will have no 
 * result.</p>
 * <p>Thumbnails are generated respecting the <code>X/Y</code> ratio of the
 * original image and so that their area doesn't exceed <code>maxWidth*
 * maxHeight</code>, which is specified to the constructor.</p>
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
public class ThumbnailLoader
    extends BatchCallTree
{

    /** The images for which we need thumbnails. */
    private ImageSummary[]  images;
    
    /** The maximum acceptable width of the thumbnails. */
    private int             maxWidth;
    
    /** The maximum acceptable height of the thumbnails. */
    private int             maxHeight;
    
    /** The lastly retrieved thumbnail. */
    private BufferedImage   currentThumbnail;
    
    
    /**
     * Loads the thumbnail for {@link #images}<code>[index]</code>.
     * 
     * @param index The index of the image in the {@link #images} array.
     * @throws ImageServerException If an error occurs.
     */
    private void loadThumbail(int index) 
        throws ImageServerException
    {
        PixelsDescription pxd = images[index].getDefaultPixels();
        int sizeX = maxWidth, sizeY = maxHeight;
        double ratio = (double) pxd.getSizeX()/pxd.getSizeY();
        if (ratio < 1) sizeX *= ratio;
        else if (ratio > 1) sizeY *= ratio;
        currentThumbnail = context.getPixelsService().getThumbnail(
                pxd.getPixels(), sizeX, sizeY);
    }
    
    /**
     * Adds a {@link BatchCall} to the tree for each thumbnail to retrieve.
     * The batch call simply invokes {@link #loadThumbail(int)}.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree()
    {
        String description;
        for (int i = 0;  i < images.length; ++i) {
            description = "Loading thumbnail: "+images[i].getName();
            final int index = i;
            add(new BatchCall(description) {
                    public void doCall() throws ImageServerException 
                    { 
                        loadThumbail(index); 
                    }
            });
        }
    }

    /**
     * Returns the lastly retrieved thumbnail.
     * This will be packed by the framework into a feedback event and
     * sent to the provided call observer, if any.
     * 
     * @return A {@link BufferedImage} containing the thumbnail pixels.
     */
    protected Object getPartialResult() { return currentThumbnail; }
    
    /**
     * Returns <code>null</code> as there's no final result.
     * In fact, thumbnails are progressively delivered with 
     * feedback events.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return null; }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime exception so to fail
     * early and in the caller's thread.
     * 
     * @param imgSummaries Contains <code>ImageSummary</code> objects, one
     *                      for each thumbnail to retrieve.
     * @param maxWidth  The maximum acceptable width of the thumbnails.
     * @param maxHeight The maximum acceptable height of the thumbnails.
     */
    public ThumbnailLoader(Set imgSummaries, int maxWidth, int maxHeight)
    {
        if (imgSummaries == null) 
            throw new NullPointerException("No image summaries.");
        if (maxWidth <= 0)
            throw new IllegalArgumentException(
                    "Non-positive width: "+maxWidth+".");
        if (maxHeight <= 0)
            throw new IllegalArgumentException(
                    "Non-positive height: "+maxHeight+".");
        images = (ImageSummary[]) imgSummaries.toArray(new ImageSummary[] {});
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

}
