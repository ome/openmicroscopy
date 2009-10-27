/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.WellFieldsCanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;

import pojos.WellSampleData;

/** 
 * Display all the fields for a given well.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class WellFieldsCanvas 
	extends JPanel
{

	/** Reference to the parent. */
	private WellFieldsView parent;
	
	/** Collection of rectangles indicating where the image is painted. */
	private Map<Rectangle, WellSampleNode> locations;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The parent of the canvas.
	 */
	WellFieldsCanvas(WellFieldsView parent)
	{
		this.parent = parent;
		setBackground(Color.black);
		setDoubleBuffered(true);
		locations = new HashMap<Rectangle, WellSampleNode>();
	}
	
	/**
	 * Returns the node corresponding to the passed location, or 
	 * <code>null</code> if no nodes found.
	 * 
	 * @param p The location.
	 * @return See above.
	 */
	WellSampleNode getNode(Point p)
	{
		Iterator<Rectangle> i = locations.keySet().iterator();
		Rectangle r;
		while (i.hasNext()) {
			r = i.next();
			if (r.contains(p)) return locations.get(r);
		}
		return null;
	}
	
	/**
     * Overridden to paint the image.
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
    	super.paintComponent(g);
    	locations.clear();
    	List<WellSampleNode> l = parent.getNodes();
        if (l == null || l.size() == 0) return;
        Graphics2D g2D = (Graphics2D) g;
        ImagePaintingFactory.setGraphicRenderingSettings(g2D);
        Iterator<WellSampleNode> i = l.iterator();
        WellSampleNode n;
        BufferedImage img;
        WellSampleData data;
        Rectangle r;
        switch (parent.getLayoutFields()) {
			case WellFieldsView.ROW_LAYOUT:
			default:
				int w = 0;
		        while (i.hasNext()) {
					n = i.next();
					img = n.getThumbnail().getFullScaleThumb();
					r = new Rectangle(w, 0, img.getWidth(), img.getHeight());
					locations.put(r, n);
					g2D.drawImage(img, null, w, 0); 
					w += img.getWidth()+1;
				}
				break;
			case WellFieldsView.SPATIAL_LAYOUT:
				int x = 0;
				int y = 0;
		        while (i.hasNext()) {
					n = i.next();
					data = (WellSampleData) n.getHierarchyObject();
					img = n.getThumbnail().getFullScaleThumb();
					x = (int) data.getPositionX();
					y = (int) data.getPositionY();
					r = new Rectangle(x, y, img.getWidth(), img.getHeight());
					g2D.drawImage(img, null, x, y); 
					locations.put(r, n);
				}
		}
        
        g2D.dispose();
    }
    
}
