/*
 * 
 * org.openmicroscopy.shoola.agents.executions.ExecutionsCanvas
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
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.event.MouseInputListener;
import javax.swing.JPanel;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.MouseOverChainExecutionEvent;
import org.openmicroscopy.shoola.agents.executions.ui.model.ExecutionsModel;
import org.openmicroscopy.shoola.agents.executions.ui.model.GridModel;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.util.ui.Constants;

/** 
 * A panel for drawing executions..
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </smalbl>
 * @since OME2.2
 */
public class ExecutionsCanvas extends JPanel implements 
	MouseInputListener {
	
	public static final int WIDTH =400;
	public static final int HEIGHT=150;
	public static final int TIP_SPACING=5;
	
	public static Font TIPFONT = new Font("Helvetica",Font.PLAIN,9); 
	public static Font LABELFONT = new Font("Helvetica",Font.PLAIN,10); 
	/* the shoola registry */
	private Registry registry;
	
	/* the model of the grid */
	private GridModel gridModel;
	
	private Vector executionViews;
	
	/** 
	 *  for tool tips
	 */
	private int xLoc;
	private int yLoc;
	private ExecutionView currentExecution;
	
	private AxisHash currentHash;
	private AxisRowDecoration currentRowDecoration;
	/**
	 * Creates a new instance.
	 */
	public ExecutionsCanvas(ExecutionsModel model,Registry registry)
	{
		super();
		this.registry = registry;
		gridModel = model.getGridModel();
		gridModel.setCanvas(this);
		setBackground(Constants.CANVAS_BACKGROUND_COLOR);
		
		// build execution views
		executionViews = new Vector();
		Iterator iter = model.executionIterator();
		while (iter.hasNext()) {
			ChainExecutionData exec = (ChainExecutionData) iter.next();
			ExecutionView view = new ExecutionView(exec,gridModel,model);
			executionViews.add(view);
		}
		addMouseMotionListener(this);
		addMouseListener(this);
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(WIDTH,HEIGHT);
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	public void setBounds(int x,int y,int w,int h) {
		super.setBounds(x,y,w,h);
		if (gridModel != null) 
			gridModel.setDimensions(w,h);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		if (gridModel == null)
			return;
		gridModel.drawAxes(g2);
		drawExecutions(g2);
		if (currentExecution != null) {
			currentExecution.drawExecutionTip(g2,xLoc,yLoc);
		}
		if (currentHash != null)
			currentHash.drawHashTip(g2,xLoc,yLoc);
		if (currentRowDecoration != null)
			currentRowDecoration.drawFull(g2);
	}
	
	public void drawExecutions(Graphics2D g) {
		Iterator iter = executionViews.iterator();
		ExecutionView view;
		boolean current;
		while (iter.hasNext()) {
			view = (ExecutionView) iter.next();
			current = (view == currentExecution);
			view.paint(g,current);
		}
	}
	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mousePressed(MouseEvent e) {
		
	}
	
	public void mouseReleased(MouseEvent e) {
		
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}
	
	public void mouseDragged(MouseEvent e) {
		
	}
	
	public void mouseMoved(MouseEvent e) {
		displayHint(e);
	}
	
	
	private void displayHint(MouseEvent e) {
		xLoc = e.getX();
		yLoc = e.getY();
		ExecutionView exec = getViewAt(xLoc,yLoc);
		if ( exec != currentExecution) {
			currentExecution = exec;
			MouseOverChainExecutionEvent event;
			ChainExecutionData execution = null;
			if (exec != null)
				execution = exec.getChainExecution(); 
			registry.getEventBus().post(
					new MouseOverChainExecutionEvent(execution));
			// if we didn't mouse over an event
			
		}
		currentHash = null;
		if (currentExecution == null) {
			currentHash = gridModel.getHashAt(xLoc,yLoc);
			if (currentHash == null) 
				currentRowDecoration = gridModel.getRowDecorationAt(xLoc,yLoc);
		}
		repaint();
	}
	
	private ExecutionView getViewAt(int x,int y) {
		ExecutionView view;
		
		Iterator iter = executionViews.iterator();
		while (iter.hasNext()) {
			view = (ExecutionView) iter.next();
			if (view.isAt(x,y) == true)
				return view;
		}
		return null;
	}

	

		
	
	public void selectChain(AnalysisChainData chain) {
		Iterator iter = executionViews.iterator();
		while (iter.hasNext()) {
			ExecutionView execView =(ExecutionView) iter.next();
			if (chain != null &&
					chain.getID() == 
						execView.getChainExecution().getChain().getID())
				execView.setHighlighted(true);
			else
				execView.setHighlighted(false);
		}
		repaint();
	}
	
	public void selectDataset(DatasetData dataset) {
		Iterator iter = executionViews.iterator();
		while (iter.hasNext()) {
			ExecutionView execView =(ExecutionView) iter.next();
			if (dataset != null && 
					dataset.getID() == 
						execView.getChainExecution().getDataset().getID())
				execView.setHighlighted(true);
			else
				execView.setHighlighted(false);
		}
		repaint();
	}
}
