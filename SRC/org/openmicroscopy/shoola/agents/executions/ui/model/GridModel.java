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
import org.openmicroscopy.shoola.agents.executions.ui.ExecutionsCanvas;

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
	
	public static final int GRID_OFFSET=15;
	private static final Color AXIS_COLOR= new Color(150,150,240);
	public static final int DOT_SIDE=4;
	protected final static BasicStroke axisStroke = new BasicStroke(2.0f);
	protected final static BasicStroke hashStroke = new BasicStroke(1.0f);
	
	
	/** 
	 * at it's heart, the grid model tracks a range [min,max], which
	 * gets mapped on to the display of the given dimensions
	 */
	private long min;
	private long max;
	
	/** dimensions of the canvas in question */
	private int gridWidth;
	private int gridHeight;

	
	/** start points for drawing */
	private int xStart;
	private int yStart;
	
	
	private int xStartAxis;
	private int xEndAxis;	
	private int yStartAxis;
	private int yEndAxis;
	
	/** # of rows */
	private int rowCount;
	
	/** min row */
	private int rowMin = 0;
	
	/** the canvas in question */
	private ExecutionsCanvas canvas = null;
	
	private Vector axisHashes;
	
	public GridModel(long min,long max,int rowCount) {
		setExtent(min,max);
		this.rowCount = rowCount;
	}
	
	public void setExtent(long min,long max) {
		this.min = min;
		this.max = max;
	}
	
	public void setDimensions(int canvasWidth,int canvasHeight) {
		gridWidth = canvasWidth-GRID_OFFSET;
		
		// axes are GRID_OFFSET away from left and bottom. 
		// remember, in window's coordinate system, (0,0) is upper-left,
		// so canvasHeight is y-coord of window bottom.
		xStartAxis = GRID_OFFSET;
		yStartAxis = canvasHeight-2*GRID_OFFSET;
		
		//need a buffer at top as well as bottom, 
		//left as well as right.
		// plus, we need to account for the fact that
		// the dots for the executions are drawn from x,y being the 
		// upper-left. Thus, to make all dots stay above axes, 
		// we move the start higher up by DOT_SIDE
	
		gridWidth = canvasWidth-3*GRID_OFFSET-3*DOT_SIDE; 	
		gridHeight= canvasHeight-3*GRID_OFFSET-3*DOT_SIDE; 
	
		xStart = GRID_OFFSET+DOT_SIDE;
		yStart = yStartAxis-2*DOT_SIDE;
		
		// we also need the end of the horizontal axis to extend past the 
		// right side of the right-most dot.
		xEndAxis = xStart+gridWidth+DOT_SIDE;
		yEndAxis = yStart-gridHeight; // because in canvas coords.
								// y increases going downwards.
		
		
	}
	
	public float getHorizCoord(long x) {
		float offset = x-min;
		float range = max-min;
		float ratio = offset/range;
		float res = xStart+ratio*gridWidth;
		return res;
	}
	
	public float getVertCoord(int y) {
		float offset = y-rowMin;
		float ratio = offset/rowCount;
		float res  =  yStart-ratio*gridHeight;
		return res;
	}
	
	public long getTime(int xCoord) {
		float offset = xCoord-xStart;
		float ratio = offset/(xEndAxis-xStartAxis);
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
	
		//hashes
		g.setStroke(hashStroke);
		drawHorizHashes(g);

		g.setPaint(oldcolor);
		g.setStroke(oldStroke);
	}
	
	private void drawHorizHashes(Graphics2D g) {
		//start at xStartAxis, end at xEndAxis. 
		// evenly space. 
		
		axisHashes = new Vector();
		int hashBottom = yStartAxis+HASH_LENGTH;
		
		int width = xEndAxis-xStartAxis;
		
		// how many hashes do we have?
		// want to have them a certain spacing apart,
		// but also want to have a limit on the number.
		int count = (int) Math.floor(width/HASH_SPACE);
		if (count > HASH_COUNT)
			count = HASH_COUNT;
		int spacing = (int) Math.floor(width/count);
		int x;
		int labelCount = 0;
		for(x =xStartAxis; x <= xEndAxis-spacing; x+= spacing) {
			long time = getTime(x);
			AxisHash h = new AxisHash(this,x,yStartAxis,hashBottom,time);
			axisHashes.add(h);
			h.paint(g);
			// draw label every third hash, as long as it's not 
			// right next to end
			if ((labelCount % 3) == 0 && x+2*spacing <xEndAxis) {
				h.drawLabel(g,spacing);
			}
			labelCount++;
		}
		AxisHash h = new AxisHash(this,xEndAxis,yStartAxis,hashBottom,
				getTime(xEndAxis));
		axisHashes.add(h);
		h.paint(g);
		// last label
		h.drawLabel(g,spacing);
	}

	public Vector getAxisHashes() {
		return axisHashes;
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
		setExtent(blrm.getValue(),blrm.getMax());
		if (canvas != null)
			canvas.repaint();
	}
}

