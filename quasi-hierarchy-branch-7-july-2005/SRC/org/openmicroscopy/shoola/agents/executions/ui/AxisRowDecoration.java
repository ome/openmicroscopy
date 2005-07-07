/*
 * org.openmicroscopy.shoola.agents.executions.ui.AxisRowDecoration
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

package org.openmicroscopy.shoola.agents.executions.ui;

//Java imports
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.executions.ui.model.GridModel;

/** 
 * A label for a row in the execution display.
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </smalbl>
 * @since OME2.2
 */

public class AxisRowDecoration {
	
	// x is the left-hand corner of the box that goes in the grid area
	
	private int x;
	private int y;
	private int width;
	private int height;
	private int labelHeight;
	private int xLabel;
	private int yLabel;
	private Color color;
	private String label;
	
	public AxisRowDecoration(int x,int y,int width,int height,Color color,
			String label) {
		this.x=x;
		this.y=y;
		this.width =width;
		this.height = height;
		this.color = color;
		this.label = label;
	}
	
	public void paint(Graphics2D g) {
		Paint oldColor = g.getPaint();
		g.setColor(color);
		g.fillRect(x,y,width,height);
		g.setPaint(Color.black);
		drawText(g);
		g.setPaint(oldColor);
	}
	
	// must draw the text for this 
	// starting at x-GridModel.LABEL_SIZE, with max width being LABEL_SIZE-
	// LABEL_BUFFER. must be vertically centered around y+height/2; 
	// - thus y is y+height/2 + stringheight/2.
	private void drawText(Graphics2D g) {
		if (label == null) 
			return;
		FontMetrics metrics = g.getFontMetrics(ExecutionsCanvas.LABELFONT);
		
		// trim width
		String displayLabel = trimLabel(g,metrics);
		drawString(g,displayLabel);
		
	}
	
	private void drawString(Graphics2D g,String text) {
		Font oldFont = g.getFont();
		
		g.setFont(ExecutionsCanvas.LABELFONT);
		
		FontMetrics metrics = g.getFontMetrics(ExecutionsCanvas.LABELFONT);
		
		// find x,y
		xLabel = x - GridModel.LABEL_SIZE;
	
		labelHeight = metrics.getHeight();
		
		yLabel = (int) (y+height/2-labelHeight/2);
		
		//draw
		g.drawString(text,xLabel,yLabel);
		g.setFont(oldFont);
	}
	
	private String trimLabel(Graphics2D g,FontMetrics metrics) {
		String res = label;
		while (metrics.stringWidth(res) > GridModel.LABEL_WIDTH) {
			res = res.substring(0,res.length()-1);
		}
		return res;
	}
	
	// can't do straight intersects call here, as we're dealing with a line
	public boolean isAt(int moveX,int moveY) {
		if ( moveY < y || moveY > y+height)
			return false;
		
		if (moveX < xLabel || moveX > xLabel+GridModel.LABEL_SIZE)
			return false;
		return true;
	}
	
	public void drawFull(Graphics2D g) {
		drawString(g,label);
	}
}