/* 
 * org.openmicroscopy.shoola.agents.executions.ui.model.GridModel
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

package org.openmicroscopy.shoola.agents.executions.ui.model;

//Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.executions.ui.AxisHash;
import org.openmicroscopy.shoola.agents.executions.ui.AxisRowDecoration;
import org.openmicroscopy.shoola.agents.executions.ui.ExecutionsCanvas;
import org.openmicroscopy.shoola.util.ui.Constants;
/** 
* A model of the mapping between values in a set of executions 
*  (low-high range displayed) and a canvas of a given width and height
*
* @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
* @version 2.2
* <small>
* (<b>Internal version:</b> $Revision$ $Date$)
* </small>
* @since OME2.2
*/

public class GridModel implements ChangeListener {
	
	private static int HASH_SPACE=40;
	private static int HASH_COUNT=10;
	private static final int HASH_LENGTH=5;
	
	public static final int LABEL_SIZE=75;
	public static final int LABEL_BUFFER=10;
	public static final int LABEL_WIDTH=LABEL_SIZE-LABEL_BUFFER;
	public static final int GRID_OFFSET=15;
	public static final int LEFT_GAP=GRID_OFFSET+LABEL_SIZE;
	public static final int RIGHT_GAP = 2*GRID_OFFSET;
	public static final int BOTTOM_GAP=2*GRID_OFFSET;
	private static final Color AXIS_COLOR= new Color(150,150,240);
	private static final Color GUIDE_COLOR = new Color(50,50,150,30);
	public static final int DOT_SIDE=4;
	public static final float AXIS_STROKE_WIDTH=2.0f;
	protected final static BasicStroke axisStroke = 
		new BasicStroke(AXIS_STROKE_WIDTH);
	protected final static BasicStroke hashStroke = new BasicStroke(1.0f);
	
	
	/** 
	 * at it's heart, the grid model tracks a range [min,max], which
	 * gets mapped on to the display of the given dimensions
	 */
	private long min;
	private long max;
	
	
	
	
	/** start and end points for drawing. these are adjusted to make sure
	 * that we have space for dots, so that dots don't go outside of axis.
	 */
	private int xStart;
	private int yStart;
	
	private int xEnd;
	
	private int xStartAxis;
	private int xEndAxis;	
	private int yStartAxis;
	private int yEndAxis;
	
	private int axisWidth;

	private int gridWidth;
	private int axisHeight;

	/** # of rows */
	private int rowCount;
	
	
	/** the canvas in question */
	private ExecutionsCanvas canvas = null;
	
	private Vector axisHashes = null;
	
	private Vector rowDecorations=null;
	
	private ExecutionsModel model;
	
	private int canvasHeight;
	
	public GridModel(ExecutionsModel model,long min,long max,int rowCount) {
		this.model = model;
		setExtent(min,max);
		this.rowCount = rowCount;
	}
	
	public void setExtent(long min,long max) {
		this.min = min;
		this.max = max;
	}
	
	public void setDimensions(int canvasWidth,int canvasHeight) {
		
		this.canvasHeight = canvasHeight;
		// First, horizontal parameters.
		// axes are LEFT_GAP away from left, to
		// account for label space and general buffer.
		
		xStartAxis = LEFT_GAP;
		
		// in order to make sure that dots don't hit axis, we actually
		// start a bit to the right of the axis.
		
		xStart = xStartAxis+DOT_SIDE;
		
		// end of the axis leaves some empty space on the right.
		// on OS X, this accounts for the window-resize icon on the
		// lower right corner.
		xEndAxis = canvasWidth - RIGHT_GAP;
		
		// axis width goes between start and end of axis.
		axisWidth = xEndAxis-xStartAxis;
		
		// last position for drawing a dot is to the left of the
		// axis end. This way, the dot never goes to the right of the axis.
		xEnd = xEndAxis-DOT_SIDE;
		
		// grid space is the range of values that can be the left coordinate
		// of a dot.
		gridWidth = xEnd-xStart;
		
		
		// y values increase as we go down screen. Leave a buffer at the
		// bottom of window.
		yStartAxis = canvasHeight-BOTTOM_GAP;
		
		// plus, we need to account for the fact that
		// the dots for the executions are drawn from x,y being the 
		// upper-left. Thus, to make all dots stay above axes, 
		// we move the start higher up by DOT_SIDE
		yStart = yStartAxis-2*DOT_SIDE;
		
		// we also need a buffer on the top 
		yEndAxis = GRID_OFFSET; 
		
		axisHeight = yStartAxis-yEndAxis;
		buildRowDecorations();
		buildHorizHashes();
	}
	
	public float getHorizCoord(long x) {
		float offset = x-min;
		float range = max-min;
		float ratio = offset/range;
		float res = xStart+ratio*gridWidth;
		return res;
	}
	
	public float getVertCoord(int y) {
		float ratio = ((float) y)/((float) rowCount);
		float res  =  yStart-ratio*axisHeight;
		return res;
	}
	
	public long getTime(int xCoord) {
		float offset = xCoord-xStart;
		float ratio = offset/gridWidth;
		return (long) (min+ratio*(max-min));
	}
	
	
	// yStart is the base for the _top_ of the dots. however, we want all dots to 
	// be above the axis line, so draw axis at yStart+DOT_SIZE
	public void drawAxes(Graphics2D g) {
		
		Paint oldcolor = g.getPaint();
		Stroke oldStroke = g.getStroke();
		g.setPaint(AXIS_COLOR);
		g.setStroke(axisStroke);

		// horiz
		g.drawLine(xStartAxis,yStartAxis,xEndAxis,yStartAxis);
		
		//vert
		g.drawLine(xStartAxis,yStartAxis,xStartAxis,yEndAxis);
	
		buildHorizHashes();
		buildRowDecorations();
		drawRowDecorations(g);
		//hashes
		g.setStroke(hashStroke);
		drawHorizHashes(g);

		g.setPaint(oldcolor);
		g.setStroke(oldStroke);
	}
	
	private void buildRowDecorations() {
		int stripeCount = model.getMajorRowCount();
		if (stripeCount ==0)
			return;
		// remember, y increases as we go down the screen,
		// so yStartAxis is larger value.
		rowDecorations = new Vector();
		double ratio = ((double)axisHeight)/((double) stripeCount);
		int stripeSize = (int) Math.ceil(ratio);
		
		int xStart = xStartAxis+(int)AXIS_STROKE_WIDTH;
		Color color = Constants.ALT_BACKGROUND_COLOR;
		int count = 0;
		int height = stripeSize;
		for (int y = yStartAxis; y >=yEndAxis; y = y- stripeSize) {
	
			// first one
			int top = y-height;
			if (top <=yEndAxis) {
				top = yEndAxis;
				height = y-yEndAxis;
				// to indicate I'm at top.
				y=yEndAxis;
			}
			String label = model.getMajorRowLabel(count);
			AxisRowDecoration decor = new
				AxisRowDecoration(xStart,top,axisWidth,height,color,label);
			rowDecorations.add(decor);
			
			// alternate colors
			if ((count %2) == 0 )
				color = Constants.CANVAS_BACKGROUND_COLOR;
			else
				color = Constants.ALT_BACKGROUND_COLOR;
			count++;
		}
	}

	private void drawRowDecorations(Graphics2D g) {
		if (rowDecorations == null)
			buildRowDecorations();
		Iterator iter = rowDecorations.iterator();
		AxisRowDecoration decor;
		while (iter.hasNext()) {
			decor = (AxisRowDecoration) iter.next();
			decor.paint(g);
		}
	}
	
	
	private void buildHorizHashes() {
		//start at xStartAxis, end at xEndAxis. 
		// evenly space. 
		
		axisHashes = new Vector();
		int hashBottom = yStartAxis+HASH_LENGTH;
		
		
		// how many hashes do we have?
		// want to have them a certain spacing apart,
		// but also want to have a limit on the number.
		int count = (int) Math.floor(axisWidth/HASH_SPACE);
		if (count > HASH_COUNT)
			count = HASH_COUNT;
		int spacing = (int) Math.floor(axisWidth/count);
		int x;
		int labelCount = 0;
		for(x =xStartAxis; x <= xEndAxis-spacing; x+= spacing) {
			long time = getTime(x);
			AxisHash h = new AxisHash(this,x,yStartAxis,hashBottom,time,spacing);
			axisHashes.add(h);
			// draw label every third hash, as long as it's not 
			// right next to end
			if ((labelCount % 3) == 0 && x+2*spacing <xEndAxis) {
				h.setDrawLabel(true);
			}
			labelCount++;
		}
		AxisHash h = new AxisHash(this,xEndAxis,yStartAxis,hashBottom,
				getTime(xEndAxis),spacing);
		h.setDrawLabel(true);
		axisHashes.add(h);
		// last label
	}
	
	private void drawHorizHashes(Graphics2D g ) {
		if (axisHashes == null)
			buildHorizHashes();
		Iterator iter = axisHashes.iterator();
		AxisHash hash;
		while (iter.hasNext()) {
			hash = (AxisHash) iter.next();
			hash.paint(g);
		}
	}
	
	public void drawSliderGuides(Graphics2D g,int sliderLeft,int sliderRight) {
		// top is yStartAxis
		// bottom is canvasHeight
		// left is xStartAxis
		// right is xEndAxis
	int rightslider = sliderRight+LABEL_SIZE+GRID_OFFSET;
		int xpts[] = new int[4];
		int ypts[] = new int[4];
		xpts[0] =xStartAxis;
		ypts[0] = yStartAxis;
		xpts[1] = sliderLeft+LABEL_SIZE+GRID_OFFSET;
		ypts[1] = canvasHeight;
		xpts[2] = rightslider;
		ypts[2] = canvasHeight;
		xpts[3] =  xEndAxis;
		ypts[3] = yStartAxis;
		
		Paint oldPaint  = g.getPaint();
		g.setPaint(GUIDE_COLOR);
		
		g.fillPolygon(xpts,ypts,4);
		g.setPaint(oldPaint);
		
	}

	public void clearDecorations() {
		buildRowDecorations();
	}
	
	public AxisHash getHashAt(int x,int y) {
		AxisHash res = null;
		
		Iterator iter = axisHashes.iterator();
		while (iter.hasNext()) {
			res = (AxisHash) iter.next();
			if (res.isAt(x,y) == true)
				return res;
		}
		return null;
	}
	
	public AxisRowDecoration getRowDecorationAt(int x,int y) {
		AxisRowDecoration res = null;
		
		Iterator iter = rowDecorations.iterator();
		while (iter.hasNext()) {
			res = (AxisRowDecoration) iter.next();
			if (res.isAt(x,y) == true)
				return res;
		}
		return null;
	}
	
	public int getHorizMax() {
		return xEndAxis;
	}
	
	public int getVertStart() {
		return yStartAxis;
	}
	
	public void setCanvas(ExecutionsCanvas canvas) {
		this.canvas = canvas;
	}

	
	public void stateChanged(ChangeEvent e) {
		Object o = e.getSource();
		if (!(o instanceof BoundedLongRangeModel))
			return;
		BoundedLongRangeModel blrm = (BoundedLongRangeModel) o;
		setExtent(blrm.getStart(),blrm.getEnd());
		if (canvas != null)
			canvas.repaint();
	}
}

