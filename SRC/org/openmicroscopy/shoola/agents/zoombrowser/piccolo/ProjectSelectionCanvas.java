/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.ProjectSelectionCanvas
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


//java imports
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.Paint;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.Timer;

//Third-party libraries
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;

import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserDatasetSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserProjectSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.events.SelectionEvent;
import 
	org.openmicroscopy.shoola.agents.zoombrowser.events.SelectionEventListener;
import org.openmicroscopy.shoola.agents.zoombrowser.MainWindow;
import org.openmicroscopy.shoola.agents.zoombrowser.SelectionState;

/** 
 * A Piccolo canvas for selecting projects.
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class ProjectSelectionCanvas extends PCanvas implements 
	SelectionEventListener, ContentComponent {
	
	private static final int HEIGHT=50;
	private static final int MAXHEIGHT=150;
	private static final int MAXWIDTH=1000;
	private static final double HGAP=20;  
	private static final double VGAP=10;
	private static final double VSEP=5;
	
	
	private PLayer layer;
	
	
	private MainWindow panel;
	
	private int lastHeight; // last window height
	
	
	
	public ProjectSelectionCanvas(MainWindow panel) {
		super();
		this.panel = panel;
		layer = getLayer();
		setMinimumSize(new Dimension(PConstants.BROWSER_SIDE,HEIGHT));
		setPreferredSize(new Dimension(PConstants.BROWSER_SIDE,HEIGHT));
		setMaximumSize(new Dimension(MAXWIDTH,MAXHEIGHT));
		removeInputEventListener(getPanEventHandler());
		removeInputEventListener(getZoomEventHandler());
	
	
	}
	
	public void setContents(Collection projects) {
		ProjectLabel pl;
		Iterator iter = projects.iterator();
		while (iter.hasNext()) {
			BrowserProjectSummary p = (BrowserProjectSummary) iter.next();
			pl = new ProjectLabel(p,this);
			layer.addChild(pl);
		}

	}

	public void completeInitialization() {
		addInputEventListener(new ProjectLabelEventHandler());
		SelectionState.getState().addSelectionEventListener(this);
	}
		
	private boolean reentrant = false;
	
	public void layoutContents() {
	
		if (reentrant == true)
			return;
		reentrant = true;	
		double x=0;
		double y =VSEP;
		ProjectLabel pl;

		int width = getWidth();
		Rectangle bounds = getBounds();
		
		Vector rows = new Vector();
		Vector row = new Vector();
		Vector widths = new Vector();
		PBounds b;
		double rowWidth;
		
		Iterator iter = layer.getChildrenIterator();
		
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof ProjectLabel) {
				pl = (ProjectLabel) obj;
			
				double labelWidth = pl.getScaledMaxWidth();
				if (x+labelWidth > width) {
					rows.add(row);
					widths.add(new Double(x));
					x =0;
					row = new Vector();
				}
				row.add(pl);
				x += labelWidth;
			}
		}
		rows.add(row);
		widths.add(new Double(x));
		double rowHeight  = 0;
		double spacing = 0;
		Iterator iter2;
		for (int i = 0; i < rows.size(); i++) {
			row = (Vector) rows.elementAt(i);
			Double rowW = (Double) widths.elementAt(i);
			rowWidth = 	rowW.doubleValue();
			iter = row.iterator();
			//  calculate space between items.
			// leftover is width - rowWidth
			double remainder = width-rowWidth;
			// divide that by n-1 
			if (row.size() >1)
				spacing = remainder/(row.size()+1);
			else 
				spacing = 0;
			x = 0;
			rowHeight = 0;
			while (iter.hasNext()) {
				pl = (ProjectLabel) iter.next();
				// place this
				pl.setOffset(x,y);
				b = pl.getGlobalFullBounds();
				x += pl.getScaledMaxWidth()+spacing;
				if (pl.getScaledMaxHeight() > rowHeight) 
					rowHeight= pl.getScaledMaxHeight();
			}
			y+= rowHeight;
		}
		
		int height  = (int) (y+VSEP);
		if (height > lastHeight) {
			Dimension d= new Dimension(width,height);
			setMinimumSize(d);
			setPreferredSize(d);
			panel.setDividerLocation(height);
			lastHeight = height;
		}
		reentrant = false;
	}
	
	
	
	public void selectionChanged(SelectionEvent e) {
		SelectionState state = e.getSelectionState();
	 	if (e.isEventOfType(SelectionEvent.SET_SELECTED_PROJECT)) {
	 			setSelectedProject();
	 	}
		else if (e.isEventOfType(SelectionEvent.SET_ROLLOVER_DATASET)) {
			BrowserDatasetSummary rolled = state.getRolloverDataset();

			setRollover(rolled);
		}
		else if (e.isEventOfType(SelectionEvent.SET_ROLLOVER_PROJECT)) {
			setRollover(state.getRolloverProject());
		}
	}
	
	public int getEventMask() {
		return SelectionEvent.SET_SELECTED_PROJECT |
			SelectionEvent.SET_ROLLOVER_PROJECT |
			SelectionEvent.SET_ROLLOVER_DATASET;
	}
	
	public void setRollover(BrowserDatasetSummary rolled) {
		Iterator iter = layer.getChildrenIterator();
		ProjectLabel pLabel=null;
		SelectionState state = SelectionState.getState();
		
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof ProjectLabel) {
				pLabel = (ProjectLabel) obj;
				BrowserProjectSummary p = pLabel.getProject();
				if (rolled != null && p.hasDataset(rolled)) 
					pLabel.setRollover(true);
				else if (p.sharesDatasetsWith(state.getSelectedProject()))
					pLabel.setActive();
				else  
					pLabel.setNormal();
			}
		}
		layoutContents();
	}
			
 	public void setRollover(BrowserProjectSummary proj) {
		Iterator iter = layer.getChildrenIterator();
		ProjectLabel pLabel;
		SelectionState state = SelectionState.getState();
		
		while (iter.hasNext()) {
		Object obj = iter.next();
			if (obj instanceof ProjectLabel) {
				pLabel = (ProjectLabel) obj;
				BrowserProjectSummary p = pLabel.getProject();
				if (pLabel.getProject() == proj) {
					pLabel.setRollover(true);
				}
				else if (p.sharesDatasetsWith(proj) ||
					p.hasDataset(state.getSelectedDataset())) {
					pLabel.setActive();
				}
				else
					pLabel.setNormal();

			}
		}
		layoutContents();
 	}
 	
	public void setSelectedProject() {
		SelectionState state = SelectionState.getState();
		BrowserProjectSummary selected = state.getSelectedProject();
		Iterator iter = layer.getChildrenIterator();
		ProjectLabel pLabel;
		BrowserProjectSummary proj;
				
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof ProjectLabel) {
				pLabel = (ProjectLabel) obj;
				proj = pLabel.getProject();
				if (proj== selected)
					pLabel.setSelected();
				else if (proj.hasDataset(state.getSelectedDataset()))
					pLabel.setActive();
				else if (selected == null)
					pLabel.setNormal();
				else if (selected.sharesDatasetsWith(proj))
					pLabel.setActive();
				else
					pLabel.setNormal();
			}
		}
		layoutContents();
	} 
}


class ProjectLabel extends PText  {
	
	public static final double NORMAL_SCALE=1;	
    public static final double ROLLOVER_SCALE=1.25;
	public static final double SELECTED_SCALE=1.5;
	public BrowserProjectSummary project;
	
	private double previousScale =NORMAL_SCALE;
	private Paint previousPaint;
	ProjectSelectionCanvas canvas;
	
	
	
	ProjectLabel(BrowserProjectSummary project,ProjectSelectionCanvas canvas) {
		super();
		this.project = project;
		this.canvas = canvas;
		setText(project.getName());
		setFont(PConstants.PROJECT_LABEL_FONT);
		
	}
	
	public double getScaledMaxWidth() {
		PBounds b = getGlobalFullBounds();
		return b.getWidth()*SELECTED_SCALE/getScale();
	}

	public double getScaledMaxHeight() {
		PBounds b = getGlobalFullBounds();
		return b.getHeight()*SELECTED_SCALE/getScale();
	}
	
	
	public BrowserProjectSummary getProject() {
		return project;
	}
	
	
	public void setNormal() {
		if (project == SelectionState.getState().getSelectedProject())
			return; 
		setScale(NORMAL_SCALE);
		setPaint(PConstants.DEFAULT_COLOR);
	}
	
	public void setActive() {
		if (project == SelectionState.getState().getSelectedProject())
					return; 
		setScale(NORMAL_SCALE);
		setPaint(PConstants.PROJECT_ACTIVE_COLOR);
	}
	
	public void setSelected() {
		setScale(SELECTED_SCALE);
		setPaint(PConstants.PROJECT_SELECTED_COLOR);
		
	}
	
	public void setRollover(boolean v) {
		if (project == SelectionState.getState().getSelectedProject())
			return;
		if (v == true) {
			setScale(ROLLOVER_SCALE);
			setPaint(PConstants.PROJECT_ROLLOVER_COLOR);
		}
		else  {
			setNormal();
		}
	}
	
	
}

class ProjectLabelEventHandler extends PBasicInputEventHandler implements 
	ActionListener {
	
	private int leftButtonMask = MouseEvent.BUTTON1_MASK;
	private final Timer timer =new Timer(300,this);
	private PInputEvent cachedEvent;
	
	ProjectLabelEventHandler() {
		super();
		
	}
	
	public void mouseEntered(PInputEvent e) {
		if (e.getPickedNode() instanceof ProjectLabel) {
			ProjectLabel pl = (ProjectLabel) e.getPickedNode();
			BrowserProjectSummary p = pl.getProject();
			if (p.hasDatasets())
				SelectionState.getState().setRolloverProject(p); 
		}
	}
	
	public void mouseExited(PInputEvent e) {
		if (e.getPickedNode() instanceof ProjectLabel) {
			ProjectLabel p = (ProjectLabel) e.getPickedNode();
			SelectionState.getState().setRolloverProject(null);
		}
	}
	
	public void mouseClicked(PInputEvent e) {
		
		if ((e.getModifiers() & leftButtonMask) !=
				leftButtonMask)
			return;
		if (timer.isRunning()) {
			timer.stop();
			doMouseDoubleClicked(e);
		}
		else {
			timer.restart();
			cachedEvent = e;
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (cachedEvent != null)
			doMouseClicked(cachedEvent);
		cachedEvent = null;
		timer.stop();
	}
	
	public void doMouseClicked(PInputEvent e) {
		if (e.getPickedNode() instanceof ProjectLabel) {
			ProjectLabel pl = (ProjectLabel) e.getPickedNode();
			BrowserProjectSummary project = pl.getProject();
			SelectionState state = SelectionState.getState();
			
			BrowserProjectSummary selected = state.getSelectedProject();
			if (selected == project) { 
				// if i've just clicked on what was selected,
				// clear dataset selection
				state.setSelectedDataset(null);
				
			}
			else if (pl.getProject().hasDatasets())
				state.setSelectedProject(project);
		}
	}
	
    public void doMouseDoubleClicked(PInputEvent e) {
    	SelectionState state = SelectionState.getState();
    	if (state.getSelectedProject() != null) {
    		SelectionState.getState().setSelectedProject(null);
    	}
    }
    
	public void mouseReleased(PInputEvent e) {
		if (e.isPopupTrigger()) {
			e.setHandled(true);
			handlePopup(e);
		}
	}
	
	public void mousePressed(PInputEvent e) {
		mouseReleased(e);
	}
	
	public void handlePopup(PInputEvent e) {
		if (e.getPickedNode() instanceof ProjectLabel) {
			ProjectLabel pl = (ProjectLabel) e.getPickedNode();
			BrowserProjectSummary picked = pl.getProject();
			BrowserProjectSummary selected = 
				SelectionState.getState().getSelectedProject();
			if (picked == selected) 
				SelectionState.getState().setSelectedProject(null);			
		}
	}
 
}