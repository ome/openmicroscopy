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

package org.openmicroscopy.shoola.agents.zoombrowser.piccolo;

//Java imports
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;

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
	
	private static final Color BORDER_COLORS[] = {
			PConstants.BORDER_OUTER,
			PConstants.BORDER_MIDDLE,
			PConstants.BORDER_INNER,
	};
	
	private static final Color HIGHLIGHT_COLORS[] = {
			PConstants.HIGHLIGHT_COLOR_OUTER,
			PConstants.HIGHLIGHT_COLOR_MIDDLE,
			PConstants.HIGHLIGHT_COLOR_INNER,
	};
	
	private Color colors[]=BORDER_COLORS;

	private double area = 0.0;
	private PText label = null;
	
	private double x;
	private double y;
	private double w;
	private double h;
	
	
	
	private Rectangle2D rects[] = {
			new Rectangle2D.Double(),
			new Rectangle2D.Double(),
			new Rectangle2D.Double(),
	};
	
	public GenericBox() {
		this(0,0,0,0);
	}
	
	public GenericBox(float x,float y) {
		this(x,y,0f,0f);
	}
	
	public GenericBox(float x,float y,float w,float h) {
		super();
		this.x=x;
		this.y=y;
		this.w=w;
		this.h=h;
		setBounds(x,y,w,h);
	}
	
	/**
	 * The bounds of a category box include a space of {@link PConstants.BORDER}
	 * around the box in all four directions. This buffer is needed for 
	 * appropriate scaling: using these bounds, we can zoom to center the node 
	 * without having it occupy the whole canvas.
	 * 
	 * @return the bounds of the box with the appropriate spacing buffer.
	 */
	public PBounds getBufferedBounds() {
		PBounds b = getFullBoundsReference();
		
		PBounds p=  new PBounds(b.getX()-PConstants.BORDER,
				b.getY()-PConstants.BORDER,
				b.getWidth()+2*PConstants.BORDER,
				b.getHeight()+2*PConstants.BORDER);
		
		return p;
	}
	
	/**
	 * Set the size of the box
	 * @param width the new width
	 * @param height the new height
	 */
	public void setExtent(double width,double height) {
		this.w =  width;
		this.h = height;
		setBounds(x,y,w,h);
	}
	
	public boolean setBounds(double x,double y,double w,double h) {
		double rX = x;
		double rY=y;
		double rW =w;
		double rH =h;
		for (int i = 0; i <rects.length; i++) {
			rects[i].setFrame(rX,rY,rW,rH);
			rX +=PConstants.STROKE_WIDTH;
			rY+=PConstants.STROKE_WIDTH;
			rW-=2*PConstants.STROKE_WIDTH;
			rH-=2*PConstants.STROKE_WIDTH;
			
		}		
		
		return super.setBounds(x,y,w,h);
	}
	
	public void paint(PPaintContext aPaintContext) {
		Graphics2D g = (Graphics2D) aPaintContext.getGraphics();
		
		g.setStroke(PConstants.BORDER_STROKE);
		for (int i = 0; i < rects.length;  i++) {
			g.setPaint(colors[i]);
			g.draw(rects[i]);
		}
		
		if (getPaint() != null) {
			g.setPaint(getPaint());
			g.fill(rects[rects.length-1]);
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
			double myArea = h*w;
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
			colors = HIGHLIGHT_COLORS;
		else
			colors = BORDER_COLORS;
		repaint();
	}
} 