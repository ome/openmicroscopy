/*
 * org.openmicroscopy.shoola.agents.executions.AxisHash
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
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.text.SimpleDateFormat;
import java.util.Date;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.executions.ui.model.GridModel;

/** 
 * A hash mark on the execution canvas
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </smalbl>
 * @since OME2.2
 */

public class AxisHash extends Rectangle2D.Double {

	public static final int PICK_RANGE=10;
	public static final int LABEL_GAP=5;
	private GridModel gridModel;
	private long time;
	private int xPos;
	private int yStart;
	private int yEnd;
	
	public AxisHash(GridModel gridModel,int xPos,int yStart,int yEnd,
			long time) {
		this.gridModel = gridModel;
		this.xPos  = xPos;
		this.yStart = yStart;
		this.yEnd = yEnd;
		setFrame(xPos,yStart,0,yEnd-yStart);
		this.time = time;
	}
	
	public void paint(Graphics2D g) {
		g.draw(this);
	}
	
	// can't do straight intersects call here, as we're dealing with a line
	public boolean isAt(int x,int y) {
		boolean inX = (Math.abs(x-xPos) <=PICK_RANGE);
		boolean inY = (y>=yStart-PICK_RANGE && y <= yEnd+PICK_RANGE);
		boolean res = inX && inY;
		return res;
	}
	
	private  String getDateString() {
		Date date = new Date(time);
		// formaat is like "Sat Jan 24 2004 hh:mm:ss EST yyyy"
		SimpleDateFormat strFormat = new SimpleDateFormat("MMM dd yyyy hh:mm:ss");
		
		return strFormat.format(date);
	}
	
	private String getLabelString() {
		Date date = new Date(time);
		// formaat is like "Sat Jan 24 2004 hh:mm:ss EST yyyy"
		SimpleDateFormat strFormat = new SimpleDateFormat("MMM dd yy");
		
		return strFormat.format(date);
	}
	
	private String getShortLabelString() {
		Date date = new Date(time);
		// formaat is like "Sat Jan 24 2004 hh:mm:ss EST yyyy"
		SimpleDateFormat strFormat = new SimpleDateFormat("MMM dd");
		
		return strFormat.format(date);
	}
	
	private String getLabelToFit(FontMetrics metrics,int spacing) {
		String label = getLabelString();
		int width = metrics.stringWidth(label);
		// if it fits
		if (width < spacing) 
			return label;
		
		// try short version
		label = getShortLabelString();
		width = metrics.stringWidth(label);
		// if it fits
		if (width < spacing) 
			return label;
		return null;
	}
	
	public void drawHashTip(Graphics2D g,int xLoc,int yLoc) {
		g.setFont(ExecutionsCanvas.TIPFONT);
		FontMetrics metrics = g.getFontMetrics(ExecutionsCanvas.TIPFONT);
		String tip = getDateString();
		int width = metrics.stringWidth(tip);
		int x = xLoc;
		if (x+width > gridModel.getHorizMax())
			x-= width;	
		g.drawString(tip,x,yLoc);
	}
	
	public void drawLabel(Graphics2D g,int spacing) {
		int x = xPos;
		int y = yEnd;
		FontMetrics metrics = g.getFontMetrics(ExecutionsCanvas.TIPFONT);
		
		String label = getLabelToFit(metrics,spacing-LABEL_GAP);
		if (label == null)
			return;
		
		Paint oldPaint = g.getPaint();
		int labelWidth = metrics.stringWidth(label);
		g.setPaint(Color.BLACK);
		g.setFont(ExecutionsCanvas.LABELFONT);
		if (x+labelWidth > gridModel.getHorizMax())
			x-=labelWidth;
		g.drawString(label,x,y+metrics.getHeight());
		g.setPaint(oldPaint);	
	}
}
