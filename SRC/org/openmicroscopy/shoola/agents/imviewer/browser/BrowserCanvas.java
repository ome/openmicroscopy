/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BrowserCanvas
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

package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JViewport;


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
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        BufferedImage img = model.getDisplayedImage();
        if (img == null) return;
        Graphics2D g2D = (Graphics2D) g;
        ImagePaintingFactory.setGraphicRenderingSettings(g2D);
        g2D.drawImage(img, null, 0, 0); 
        if (model.isUnitBar()) {
            String value = model.getUnitBarValue(); 
            if (value != null) {
                int size = (int) (model.getUnitBarSize());
                int width, height;
                width = img.getWidth();
                height = img.getHeight();
                
                Rectangle imgRect = new Rectangle(0, 0, width, height);
                Rectangle viewRect = view.getViewport().getBounds();
                if (imgRect.contains(viewRect)) {
                    JViewport port = view.getViewport();
                    width = (int) port.getViewPosition().getX()+port.getWidth();
                    height = (int) port.getViewPosition().getY()+
                            port.getHeight();
                } else {
                	if (viewRect.width < imgRect.width) width = viewRect.width;
                	if (viewRect.height < imgRect.height)
                		height = viewRect.height;
                }
                ImagePaintingFactory.paintScaleBar(g2D, width-size-10, 
                										height-10, size, value);
            }
        }
    }
    
}
