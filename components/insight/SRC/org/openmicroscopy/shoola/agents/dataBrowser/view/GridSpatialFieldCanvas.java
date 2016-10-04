/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.WellFieldsCanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
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
class GridSpatialFieldCanvas 
	extends WellFieldsCanvas
{
	
	/** Color of the axis and border. */
	private static final Color LINE_COLOR = Color.BLACK;
	
	/** The unit of reference. */
	private static final int UNIT = 100;
	
	/** The size of a tick. */
	private static final int TICK = 2;
	
	/** Collection of rectangles indicating where the image is painted. */
	private Map<Rectangle, WellSampleNode> locations;
	
	
	/**
     * Implemented as specified by the {@link WellFieldsCanvas} interface.
     * @see WellFieldsCanvas#refreshUI()
     */
	public void refreshUI() {
	    repaint();
	}
	
	/**
     * Implemented as specified by the {@link WellFieldsCanvas} interface.
     * @see WellFieldsCanvas#setLoading(boolean)
     */
	public void setLoading(boolean loading) {
	    
	};
	
	/** 
	 * Draws the grid.
	 * 
	 * @param g2D The graphics context.
	 * @param x	  The location on the X-axis of a unit.
	 * @param y	  The location on the Y-axis of a unit.
	 */
	private void drawGrid(Graphics2D g2D, int x, int y)
	{
		double f = parent.getMagnification();
		int w = (int) (WellFieldsView.DEFAULT_WIDTH*f);
		int h = (int) (WellFieldsView.DEFAULT_HEIGHT*f);
		
		g2D.setColor(UIUtilities.LIGHT_GREY);
		for (int i = 0; i < h; i = i+8) {
			g2D.drawLine(0, i, w, i);
		}
		
		for (int i = 0; i < w; i = i+8) {
			g2D.drawLine(i, 0, i, h);
		}
		
		g2D.setColor(LINE_COLOR);
		//X-axis
		g2D.drawLine(0, h/2, w, h/2);
		
		int n = h/2;
		if (x != 0) n = n/x;
		for (int i = 1; i <= n; i++) {
			g2D.drawLine(w/2+x*i, h/2-TICK, w/2+x*i, h/2+TICK);
			
			g2D.drawLine(w/2-x*i, h/2-TICK, w/2-x*i, h/2+TICK);
		}
		
		//Y-axis
		g2D.drawLine(w/2, 0, w/2, h);
		
		n = h/2;
		if (y != 0) n = n/y;
		for (int i = 1; i <= n; i++) {
			g2D.drawLine(w/2-TICK, h/2+y*i, w/2+TICK, h/2+y*i);
			g2D.drawLine(w/2-TICK, h/2-y*i, w/2+TICK, h/2-y*i);
		}
		if (f > Thumbnail.MIN_SCALING_FACTOR) {
			//draw unit
			String s = ""+UNIT;
			FontMetrics fm = getFontMetrics(getFont());
			int fs = fm.stringWidth(s);
			
			g2D.drawString(s, w/2+x-fs/2, h/2-3*TICK);
			g2D.drawString(s, w/2+2*TICK, h/2-y+2*TICK);
			
			s = "-"+UNIT;
			fs = fm.stringWidth(s);
			g2D.drawString(s, w/2-x-fs/2, h/2-3*TICK);
			g2D.drawString(s, w/2+2*TICK, h/2+y+2*TICK);
		}
		
		//Border
		g2D.drawRect(0, 0, w, h);
	}
	
	/** Sets the font according to the magnification factor. */
	private void setFont()
	{
		Font f = getFont();
		int size = (int) ((f.getSize()-4)*parent.getMagnification());
		setFont(f.deriveFont(f.getStyle(), size));
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The parent of the canvas.
	 */
	public GridSpatialFieldCanvas(WellFieldsView parent)
	{
		super(parent);
		setDoubleBuffered(true);
		setPreferredSize(new Dimension(WellFieldsView.DEFAULT_WIDTH, 
				WellFieldsView.DEFAULT_HEIGHT));
		setSize(getPreferredSize());
		setBackground(UIUtilities.BACKGROUND);
		locations = new HashMap<Rectangle, WellSampleNode>();
		setFont();
	}
	
	/**
     * Implemented as specified by the {@link WellFieldsCanvas} interface.
     * @see WellFieldsCanvas#getNode(Point)
     */
	public WellSampleNode getNode(Point p)
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
		double factor = parent.getMagnificationUnscaled();
        switch (parent.getLayoutFields()) {
			case WellFieldsView.ROW_LAYOUT:
			default:
				int w = 0;
				int h = 0;
				double ff = f*factor;
		        while (i.hasNext()) {
					n = i.next();
					img = n.getThumbnail().getFullScaleThumb();
					if (ff != 1) img = Factory.magnifyImage(ff, img);
					if (img != null) {
						if (w+img.getWidth() > WellFieldsView.DEFAULT_WIDTH*ff) {
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
				
				int wMax = xc+xMax+(int) (width*f);
				int hMax = yc+yMax+(int) (height*f);
				
				double rx = (double) WellFieldsView.DEFAULT_WIDTH*f/wMax;
				double ry = (double) WellFieldsView.DEFAULT_HEIGHT*f/hMax;
				
				drawGrid(g2D, (int) (UNIT*rx), (int) (UNIT*ry));
				
				BufferedImage scaled;
				while (i.hasNext()) {
					n = i.next();
					img = n.getThumbnail().getFullScaleThumb();
					if (img != null) {
						x = (int) n.getPositionX();
						y = (int) n.getPositionY();
						vx = (int) ((x+xc)*rx);
						vy = (int) (WellFieldsView.DEFAULT_HEIGHT*f)-
							(int) ((y+yc)*ry);
						w = (int) (width*rx*f*factor);
						h = (int) (height*ry*f);
						vy = vy-h;
						
						h = (int) (h*factor);
						scaled = Factory.scaleBufferedImage(img, w, h);
						r = new Rectangle(vx, vy, w, h);
						g2D.drawImage(scaled, null, r.x, r.y); 
						locations.put(r, n);
					}
				}
		}
        
        g2D.dispose();
    }
    
}
