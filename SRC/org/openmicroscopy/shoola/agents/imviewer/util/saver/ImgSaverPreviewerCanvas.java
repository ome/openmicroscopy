/*
 * org.openmicroscopy.shoola.agents.imviewer.util.saver.ImgSaverPreviewerCanvas
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

package org.openmicroscopy.shoola.agents.imviewer.util.saver;




//Java imports
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;

/** 
 * Component hosting the images.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 			<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ImgSaverPreviewerCanvas
    extends JPanel
{
    
    /** Reference to the parent. */
    private ImgSaverPreviewer   model;
    
    /**
     * Paints the images composing the main image.
     * 
     * @param g2D   The graphics context.
     * @param w     The location of the first image.
     * @param b     Pass <code>true</code> to paint the unit bar,
     *              <code>false</code> otherwise.
     * @param v     The value to paint.
     * @param size  The size of the unit bar.
     * @return  The location of the main image.
     */
    private int paintComponents(Graphics2D g2D, int w, boolean b, String v, int
                                size)
    {
        Iterator i = model.getImageComponents().iterator();
        BufferedImage img;
        int width;
        while (i.hasNext()) {
            img = (BufferedImage) i.next();
            g2D.drawImage(img, null, w, ImgSaverPreviewer.SPACE);
            width = img.getWidth();
            if (b && v != null) {
                ImagePaintingFactory.paintScaleBar(g2D, 
                        w+img.getWidth()-size-10, 
                        ImgSaverPreviewer.SPACE+img.getHeight()-10, size, v);
            }
            w += (width+ImgSaverPreviewer.SPACE);
        }
        return w;
    }

    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link ImgSaverPreviewer}.
     *              Mustn't be <code>null</code>.
     */
    ImgSaverPreviewerCanvas(ImgSaverPreviewer model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
    }
    
    /**
     * Overridden to paint the main image and its components if any.
     * @see JPanel#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        ImagePaintingFactory.setGraphicRenderingSettings(g2D);
        BufferedImage img = model.getImage();
        if (img == null) return;
        boolean unitBar = model.isUnitBar();
        String value = model.getUnitBarValue(1.0); 
        int size = (int) model.getUnitBarSize();
        int w = ImgSaverPreviewer.SPACE;
        if (model.getImageComponents() != null) 
            w = paintComponents(g2D, w, unitBar, value, size);
        g2D.drawImage(img, null, w, ImgSaverPreviewer.SPACE);
        if (unitBar && value != null)
            ImagePaintingFactory.paintScaleBar(g2D, w+img.getWidth()-size-10, 
                ImgSaverPreviewer.SPACE+img.getHeight()-10, size, value);
    }
    
}
