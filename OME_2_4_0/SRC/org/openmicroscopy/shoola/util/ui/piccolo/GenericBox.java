/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.GenericBox
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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



/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui.piccolo;

//Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;

import org.openmicroscopy.shoola.util.ui.Constants;


//Third-party libraries
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/** 
 * A subclass of {@link PPath} that is used to provide a colored background
 * to various widgets in the Chain builder
 * 
 *   
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public class GenericBox extends PNode implements SortableBufferedObject {
	

	
	private Color colors[]=Constants.BORDER_COLORS;
	private static final int RECT_COUNT=5;
	private static final double SCALE_MULTIPLIER=2;
	private static final double SCALE_THRESHOLD=1;
	private static final double STROKE_MAX=75;

	private double area = 0.0;
	private PText label = null;
	
	private Rectangle2D rect = new Rectangle2D.Double();
		
	public GenericBox() {
		this(0,0,0,0);
	}
	
	public GenericBox(float x,float y) {
		this(x,y,0f,0f);
	}
	
	public GenericBox(float x,float y,float w,float h) {
		super();
		setBounds(x,y,w,h);
	}
	
	/**
	 * The bounds of a category box include a space of {@link Constants.BORDER}
	 * around the box in all four directions. This buffer is needed for 
	 * appropriate scaling: using these bounds, we can zoom to center the node 
	 * without having it occupy the whole canvas.
	 * 
	 * @return the bounds of the box with the appropriate spacing buffer.
	 */
	public PBounds getBufferedBounds() {
		PBounds b = getGlobalFullBounds();
		
		PBounds p=  new PBounds(b.getX()-Constants.BORDER,
				b.getY()-Constants.BORDER,
				b.getWidth()+2*Constants.BORDER,
				b.getHeight()+2*Constants.BORDER);
		
		return p;
	}
	
	/**
	 * Set the size of the box
	 * @param width the new width
	 * @param height the new height
	 */
	public void setExtent(double width,double height) {
		PBounds b = getBounds();
		setBounds(b.getX(),b.getY(),width,height);
	}
		
	public void paint(PPaintContext aPaintContext) {
		Graphics2D g = (Graphics2D) aPaintContext.getGraphics();
		
		g.setStroke(Constants.BORDER_STROKE);
		double scale = aPaintContext.getScale();
		drawRects(g,scale);
	}
	
	private void drawRects(Graphics2D g,double scale) {
		PBounds b = getBounds();
		
		
		double strokeWidth = Constants.STROKE_WIDTH;
		if (scale < SCALE_THRESHOLD) 
			strokeWidth = strokeWidth/(SCALE_MULTIPLIER*scale);
	
		if (strokeWidth > STROKE_MAX)
			strokeWidth = STROKE_MAX;
		
		double halfStroke = strokeWidth/2;
		double rX = b.getX()+halfStroke;
		double rY=b.getY()+halfStroke;
		double rW =b.getWidth()-strokeWidth;
		double rH =b.getHeight()-strokeWidth;
	
		for (int i = 0; i <RECT_COUNT; i++) {
			g.setPaint(colors[i]);
			rect.setFrame(rX,rY,rW,rH);
			g.setStroke(new BasicStroke((int) strokeWidth));
			g.draw(rect);
			rX +=strokeWidth;
			rY+=strokeWidth;
			rW-=2*strokeWidth;
			rH-=2*strokeWidth;	
		}		
	}
	
	
	/**
	 * Add a node containing a textual label
	 * @param label
	 */
	public void addLabel(PText label) {
		addChild(label);
		this.label = label;
	
	}
	
	public  float getLabelHeight() {
		if (label != null)
			return (float)label.getFullBoundsReference().getHeight();
		else
			return 0f;		
	}
	
	public int compareTo(Object o) {
		if (o instanceof SortableBufferedObject) {
			SortableBufferedObject node = (SortableBufferedObject) o;
			PBounds b = getFullBoundsReference();
			double myArea = b.getHeight()*b.getWidth();
			PBounds bounds = node.getBufferedBounds();
			double nodeArea = bounds.getHeight()*bounds.getWidth();
			int res =(int) (myArea-nodeArea);
			return res;
		}
		else
			return -1;
	}
	
	public void addArea(double newArea) {
		area += newArea;
	}
	
	public double getArea() {
		return area;
	}
		
	public void setHighlighted(boolean v) {
		if (v == true) 
			colors = Constants.HIGHLIGHT_COLORS;
		else
			colors = Constants.BORDER_COLORS;
		repaint();
	}
} 