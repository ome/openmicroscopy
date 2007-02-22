/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BrowserCanvas
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

package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;

/** 
 * UI component where the renderered image is painted.
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
class BrowserCanvas
    extends JPanel
{

	/** Reference to the Model. */
    private BrowserModel    model;
    
    /** Reference to the View. */
    private BrowserUI    view;

    /**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param view  Reference to the View. Mustn't be <code>null</code>.
     */
    BrowserCanvas(BrowserModel model, BrowserUI view)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        this.model = model;
        this.view = view;
        setDoubleBuffered(true);
    }
    
    /**
     * Overridden to paint the image.
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        BufferedImage img = model.getDisplayedImage();
        if (img == null) return;
        Graphics2D g2D = (Graphics2D) g;
        ImagePaintingFactory.setGraphicRenderingSettings(g2D);
        g2D.drawImage(img, null, 0, 0); 
        if (!(model.isUnitBar())) return;
        
        String value = model.getUnitBarValue(); 
        if (value == null) return;
        
        int size = (int) (model.getUnitBarSize());
        
        // Position scalebar in the bottom left of the viewport or
        // the image which ever is viewable. 
        Rectangle imgRect = new Rectangle(0, 0, img.getWidth(), 
        									img.getHeight());
        Rectangle viewRect = view.getViewport().getBounds();
        Point p = view.getViewport().getViewPosition();
        int x = (int) p.getX();
        int y = (int) p.getY();
        int width = Math.min(x+viewRect.width, img.getWidth());
        int height = Math.min(y+viewRect.height, img.getHeight());
        if (imgRect.contains(viewRect)) {
            width = x+viewRect.width;
            height = y+viewRect.height;
        }
        if (viewRect.width >= size)
        	ImagePaintingFactory.paintScaleBar(g2D, width-size-10, 
        							height-10, size, value, 
        							model.getUnitBarColor());
    }
    
}
