/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.BrowserBICanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.awt.image.BufferedImage;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;
import org.openmicroscopy.shoola.env.rnd.data.Region;
import org.openmicroscopy.shoola.env.rnd.data.Tile;

/** 
 * Paints the image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * </small>
 * @since 3.0-Beta4
 */
class BrowserBICanvas
	extends ImageCanvas
{

	/**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param view  Reference to the View. Mustn't be <code>null</code>.
     */
	BrowserBICanvas(BrowserModel model, BrowserUI view)
    {
        super(model, view);
    }
       
    /**
     * Overridden to paint the image.
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
    	Graphics2D g2D = (Graphics2D) g;
        ImagePaintingFactory.setGraphicRenderingSettings(g2D, model.isInterpolation());
        if (model.isBigImage()) {
        	g2D.setColor(BACKGROUND);
        	g2D.drawRect(0, 0, getWidth()-1, getHeight()-1);
        	Map<Integer, Tile> tiles = model.getTiles();
        	int rows = model.getRows();
        	int columns = model.getColumns();
        	Tile tile;
        	int index;
        	Object img;
            Region region;
        	for (int i = 0; i < rows; i++) {
    			for (int j = 0; j < columns; j++) {
    				index = i*columns+j;
    				tile = tiles.get(index);
    				region = tile.getRegion();
    				img = tile.getImage();
    				if (img != null)
    					 g2D.drawImage((BufferedImage) img,
    							 region.getX(), region.getY(), null);
    				else {
    					g2D.drawRect(region.getX(), region.getY(), 
    							region.getWidth(), region.getHeight());
    				}
    			}
    		}
        	paintScaleBar(g2D, model.getTiledImageSizeX(),
        			model.getTiledImageSizeY(), view.getViewport());
        } else {
        	 BufferedImage img = model.getDisplayedImage();
             if (img == null) return;
             g2D.drawImage(img, 0, 0, null); 
             paintScaleBar(g2D, img.getWidth(), img.getHeight(),
            		 view.getViewport());
        }
        g2D.dispose();
    }

}
