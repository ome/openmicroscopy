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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
	
	/** Color of the axis and border. */
	private static final Color LINE_COLOR = Color.BLACK;
	
	/** The unit of reference. */
	private static final int UNIT = 100;
	
	/** The size of a tick. */
	private static final int TICK = 2;
	
	/** Reference to the parent. */
	private WellFieldsView parent;
	
	/** Collection of rectangles indicating where the image is painted. */
	private Map<Rectangle, WellSampleNode> locations;
	
	/** 
	 * Draws the grid.
	 * 
	 * @param g2D The graphics context.
	 * @param x	  The location on the X-axis of a unit.
	 * @param y	  The location on the Y-axis of a unit.
	 */
	private void drawGrid(Graphics2D g2D, int x, int y)
	{
		g2D.setColor(UIUtilities.LIGHT_GREY);
		for (int i = 0; i < WellFieldsView.DEFAULT_HEIGHT; i = i+8) {
			g2D.drawLine(0, i, WellFieldsView.DEFAULT_WIDTH, i);
		}
		
		for (int i = 0; i < WellFieldsView.DEFAULT_WIDTH; i = i+8) {
			g2D.drawLine(i, 0, i, WellFieldsView.DEFAULT_HEIGHT);
		}
		
		g2D.setColor(LINE_COLOR);
		//X-axis
		g2D.drawLine(0, WellFieldsView.DEFAULT_HEIGHT/2, 
				WellFieldsView.DEFAULT_WIDTH, WellFieldsView.DEFAULT_HEIGHT/2);
		
		int n = WellFieldsView.DEFAULT_WIDTH/2;
		n = n/x;
		for (int i = 1; i <= n; i++) {
			g2D.drawLine(WellFieldsView.DEFAULT_WIDTH/2+x*i, 
					WellFieldsView.DEFAULT_HEIGHT/2-TICK, 
					WellFieldsView.DEFAULT_WIDTH/2+x*i, 
					WellFieldsView.DEFAULT_HEIGHT/2+TICK);
			
			g2D.drawLine(WellFieldsView.DEFAULT_WIDTH/2-x*i, 
					WellFieldsView.DEFAULT_HEIGHT/2-TICK, 
					WellFieldsView.DEFAULT_WIDTH/2-x*i, 
					WellFieldsView.DEFAULT_HEIGHT/2+TICK);
		}
		
		//Y-axis
		g2D.drawLine(WellFieldsView.DEFAULT_WIDTH/2, 0,
				WellFieldsView.DEFAULT_WIDTH/2, WellFieldsView.DEFAULT_HEIGHT);
		
		n = WellFieldsView.DEFAULT_HEIGHT/2;
		n = n/y;
		for (int i = 1; i <= n; i++) {
			g2D.drawLine(WellFieldsView.DEFAULT_WIDTH/2-TICK, 
					WellFieldsView.DEFAULT_HEIGHT/2+y*i,
					WellFieldsView.DEFAULT_WIDTH/2+TICK, 
					WellFieldsView.DEFAULT_HEIGHT/2+y*i);
			g2D.drawLine(WellFieldsView.DEFAULT_WIDTH/2-TICK, 
					WellFieldsView.DEFAULT_HEIGHT/2-y*i,
					WellFieldsView.DEFAULT_WIDTH/2+TICK, 
					WellFieldsView.DEFAULT_HEIGHT/2-y*i);
		}
		
		//draw unit
		String s = ""+UNIT;
		FontMetrics fm = getFontMetrics(getFont());
		int w = fm.stringWidth(s);
		
		g2D.drawString(s, WellFieldsView.DEFAULT_WIDTH/2+x-w/2, 
				WellFieldsView.DEFAULT_HEIGHT/2-3*TICK);
		
		g2D.drawString(s, WellFieldsView.DEFAULT_WIDTH/2+2*TICK, 
				WellFieldsView.DEFAULT_HEIGHT/2-y+2*TICK);
		
		s = "-"+UNIT;
		w = fm.stringWidth(s);
		g2D.drawString(s, WellFieldsView.DEFAULT_WIDTH/2-x-w/2, 
				WellFieldsView.DEFAULT_HEIGHT/2-3*TICK);
		g2D.drawString(s, WellFieldsView.DEFAULT_WIDTH/2+2*TICK, 
				WellFieldsView.DEFAULT_HEIGHT/2+y+2*TICK);
		
		//Border
		g2D.drawRect(0, 0, WellFieldsView.DEFAULT_WIDTH, 
				WellFieldsView.DEFAULT_HEIGHT);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The parent of the canvas.
	 */
	WellFieldsCanvas(WellFieldsView parent)
	{
		this.parent = parent;
		setDoubleBuffered(true);
		setPreferredSize(new Dimension(WellFieldsView.DEFAULT_WIDTH, 
				WellFieldsView.DEFAULT_HEIGHT));
		setSize(getPreferredSize());
		locations = new HashMap<Rectangle, WellSampleNode>();
		Font f = getFont();
		setFont(f.deriveFont(f.getStyle(), f.getSize()-4));
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
    	//g.setColor(getBackground());
    	locations.clear();
    	List<WellSampleNode> l = parent.getNodes();
        if (l == null || l.size() == 0) return;
        Graphics2D g2D = (Graphics2D) g;
        ImagePaintingFactory.setGraphicRenderingSettings(g2D);
        Iterator<WellSampleNode> i = l.iterator();
        WellSampleNode n;
        BufferedImage img;
        Rectangle r;
        double f = parent.getMagnification();
        switch (parent.getLayoutFields()) {
			case WellFieldsView.ROW_LAYOUT:
			default:
				int w = 0;
				int h = 0;
		        while (i.hasNext()) {
					n = i.next();
					img = n.getThumbnail().getFullScaleThumb();
					if (img != null) {
						if (w+img.getWidth() > WellFieldsView.DEFAULT_WIDTH) {
							w = 0;
							h += img.getHeight()+1;
						}
						r = new Rectangle(w, h, img.getWidth(), 
								img.getHeight());
						locations.put(r, n);
						g2D.drawImage(img, null, w, h); 
						w += img.getWidth()+1;
					}
				}
				break;
			case WellFieldsView.SPATIAL_LAYOUT:
				int x = 0;
				int y = 0;
				int xMin = Integer.MAX_VALUE;
				int yMin = Integer.MAX_VALUE;
				int xMax = Integer.MIN_VALUE;
				int yMax = Integer.MIN_VALUE;
				int width = 0;
				int height = 0;
				
				while (i.hasNext()) {
					n = i.next();
					img = n.getThumbnail().getFullScaleThumb();
					if (img != null) {
						if (width < img.getWidth())
							width = img.getWidth();
						if (height < img.getHeight())
							height = img.getHeight();
						x = (int) n.getPositionX();
						y = (int) n.getPositionY();
						if (x < xMin) xMin = x;
						if (y < yMin) yMin = y;
						if (xMax < x) xMax = x;
						if (yMax < y) yMax = y;
					}
				}
				
				
				int xc = Math.abs(xMin);
				int yc = Math.abs(yMin);
				i = l.iterator();
				int vx = 0;
				int vy = 0;
				
				int wMax = xc+xMax+width;
				int hMax = yc+yMax+height;
				
				double rx = (double) WellFieldsView.DEFAULT_WIDTH/wMax;
				double ry = (double) WellFieldsView.DEFAULT_HEIGHT/hMax;
				
				drawGrid(g2D, (int) (UNIT*rx),  (int) (UNIT*ry));
				
				BufferedImage scaled;
				while (i.hasNext()) {
					n = i.next();
					img = n.getThumbnail().getFullScaleThumb();
					if (img != null) {
						x = (int) n.getPositionX();
						y = (int) n.getPositionY();
						vx = (int) ((x+xc)*rx);
						vy = WellFieldsView.DEFAULT_HEIGHT-(int) ((y+yc)*ry);
						
						w = (int) (width*rx*f);
						h = (int) (height*ry);
						vy = vy-h;
						h = (int) (height*ry*f);
						scaled = Factory.scaleBufferedImage(img, w, h);
						r = new Rectangle(vx, vy, w, h);
						g2D.drawImage(scaled, null, vx, vy); 
						locations.put(r, n);
					}
				}
		}
        
        g2D.dispose();
    }
    
}
