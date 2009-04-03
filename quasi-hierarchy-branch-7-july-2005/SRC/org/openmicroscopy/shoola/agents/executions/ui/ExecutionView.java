/*
 * org.openmicroscopy.shoola.agents.executions.ExecutionView
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
import java.awt.geom.Ellipse2D;
import java.awt.Graphics2D;
import java.text.SimpleDateFormat;
import java.util.Date;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.agents.executions.ui.model.GridModel;
import org.openmicroscopy.shoola.agents.executions.ui.model.ExecutionsModel;
import org.openmicroscopy.shoola.util.ui.Constants;

/** 
 * a visual representation of a chain execution.
 * 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </smalbl>
 * @since OME2.2
 */
public class ExecutionView extends Ellipse2D.Float {
	
	public static final int PICK_RANGE=10;
	
	private ChainExecutionData execution;
	private GridModel gridModel;
	private ExecutionsModel execsModel;
	
	private boolean highlighted;
	
	public ExecutionView(ChainExecutionData execution,GridModel gridModel,
			ExecutionsModel execsModel) {
		super();
		this.execution = execution;
		this.gridModel = gridModel;
		this.execsModel = execsModel;
	}
	
	
	
	public void paint(Graphics2D g,boolean current) {
		long time = execution.getDate().getTime();
		if (execsModel.isInRange(time)) {
		
			Color oldColor = g.getColor();
			if (current == true)
				g.setColor(Constants.EXECUTION_SELECTED_FILL);
			else if (highlighted == true)
				g.setColor(Constants.HIGHLIGHT_COLOR);
			else {
			
				g.setColor(Constants.DEFAULT_COLOR);
			}
			int x = (int) gridModel.getHorizCoord(time);
			int y = (int) gridModel.getVertCoord(execsModel.getRow(execution));
			setFrame(x,y,GridModel.DOT_SIDE,GridModel.DOT_SIDE);
			g.fill(this);
			g.setColor(oldColor);
		}	
	}

	
	public boolean isAt(int x,int y) {
		double left = x-PICK_RANGE/2;
		double top = y -PICK_RANGE/2;
		return intersects(left,top,PICK_RANGE,PICK_RANGE);
		
	}
	
	public ChainExecutionData getChainExecution() {
		return execution;
	}
	
	// highlighted if the appropriate chain/dataset has been moused
	// over in a different view.
	public void setHighlighted(boolean v) {
		highlighted = v;
	}
	
	public void drawExecutionTip(Graphics2D g,int xLoc,int yLoc) {
		
		ChainExecutionData exec= getChainExecution();
		String chain = exec.getChain().getName();
		String dataset = exec.getDataset().getName();
		
		g.setFont(ExecutionsCanvas.TIPFONT);
		FontMetrics metrics = g.getFontMetrics(ExecutionsCanvas.TIPFONT);
		
		int height = 3* metrics.getHeight();
		int width = metrics.stringWidth(chain);
		int newWidth = metrics.stringWidth(dataset);
		if (newWidth > width)
			width = newWidth;
		
		// date string
		Date date = exec.getDate();
		// formaat is like "Sat Jan 24 2004"
		SimpleDateFormat strFormat = new SimpleDateFormat("EEE MMM dd yyyy");
		
		String date1 = strFormat.format(date);
		newWidth = metrics.stringWidth(date1.toString());
		if (newWidth > width)
			width = newWidth;
		
		//this format is "18:41:41 EST" 
		strFormat = new SimpleDateFormat("kk:mm:ss zzz");
		String date2 = strFormat.format(date);
		newWidth = metrics.stringWidth(date2.toString());
		if (newWidth > width)
			width = newWidth;
		

		int x = xLoc;
		int y = yLoc;
		
		//		 eventually, adjust xLoc,yLoc
		// to account for going over side
		if (x+width > gridModel.getHorizMax())
			x -= width;
		else // give it some spacing to the right, as
			// cursor goes to the right
			x += ExecutionsCanvas.TIP_SPACING;
			
		
		if (y+height > gridModel.getVertStart())
			y -= height;
			
		g.drawString(chain,x,y);
		y+=metrics.getHeight();
		g.drawString(dataset,x,y);
		y+=metrics.getHeight();
		g.drawString(date1,x,y);
		y+=metrics.getHeight();
		g.drawString(date2,x,y);
		
	}
}
	