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
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

//Third-party libraries
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;

import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserDatasetSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserProjectSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.MainWindow;

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

public class ProjectSelectionCanvas extends PCanvas implements ContentComponent {
	
	private static final int HEIGHT=50;
	private static final int MAXHEIGHT=150;
	private static final int MAXWIDTH=1000;
	private static final double HGAP=20;  
	private static final double VGAP=10;
	private static final double VSEP=5;
	
	
	private PLayer layer;
	
	
	private MainWindow panel;
	
	private int lastHeight; // last window height
	
	private BrowserProjectSummary selectedProject;
	
	private BrowserDatasetSummary selectedDataset;
	
	
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
	
	public void setContents(Object obj) {
		Collection projects = (Collection) obj;
		ProjectLabel pl;
		Iterator iter = projects.iterator();
		while (iter.hasNext()) {
			BrowserProjectSummary p = (BrowserProjectSummary) iter.next();
			pl = new ProjectLabel(p,this);
			layer.addChild(pl);
		}

	}

	public void completeInitialization() {
		System.err.println("completing initialization of project selection canvas");
		addInputEventListener(new GenericEventHandler());
	}
		
	private boolean reentrant = false;
	
	public void layoutContents() {
		System.err.println("laying out contents of project selection canvas");
		setMinimumSize(new Dimension(PConstants.BROWSER_SIDE,HEIGHT));
		setPreferredSize(new Dimension(PConstants.BROWSER_SIDE,HEIGHT));
		setMaximumSize(new Dimension(MAXWIDTH,MAXHEIGHT));
		doLayout();
	}
	
	public void doLayout() {
	
		if (reentrant == true)
			return;
		reentrant = true;	
		double x=0;
		double y =VSEP;
		ProjectLabel pl;

		int width = getWidth();
		System.err.println("displaying project list. width of window is "+width);
		System.err.println("height is "+getHeight());
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
		//if (height > lastHeight) {
			Dimension d= new Dimension(width,height);
			setMinimumSize(d);
			setPreferredSize(d);
			panel.setDividerLocation(height);
			lastHeight = height;
		//}
		reentrant = false;
	}
	
		
	public void setRolloverDataset(BrowserDatasetSummary rolled) {
		setLabelPainting(rolled,null);
		doLayout();
	}
			
 	public void setRolloverProject(BrowserProjectSummary proj) {
		
		System.err.println("\n\n\nsetting rollover project..");
		if (proj != null)
			System.err.println("to .."+proj.getName());
		if (selectedProject != null)
			System.err.println("selected is ..."+selectedProject.getName());
		setLabelPainting(null,proj);
		panel.setRolloverProject(proj);
		doLayout();
 	}
 	
 	
	public void setSelectedProject(BrowserProjectSummary selected) {
		if (selected == selectedProject) // no change
			return;
		
		if (selected != null)
			System.err.println("\nset selected project to ..."+selected.getName());
		else
			System.err.println("\nset selected project to null");
		selectedProject = selected;
		if (selected != null && !selected.hasDataset(selectedDataset))
			selectedDataset = null;
		setLabelPainting(null,null);
		panel.setSelectedProject(selected);
		doLayout();
	} 
	
	public void setSelectedDataset(BrowserDatasetSummary dataset) {
		System.err.println("project canvas got selection of dataset .."+
				dataset);
		if (dataset == selectedDataset) //no change
			return;
		selectedDataset = dataset;
		
		setLabelPainting(null,null);
	}
	
	public void setLabelPainting(BrowserDatasetSummary rolloverDataset,
			BrowserProjectSummary rolloverProject) {
		Iterator iter = layer.getChildrenIterator();
		ProjectLabel pLabel;
		BrowserProjectSummary proj;
		
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof ProjectLabel) {
				pLabel = (ProjectLabel) obj;
				proj = pLabel.getProject();
				if (proj== selectedProject)
					pLabel.setSelected();
				else if (proj == rolloverProject || 
						(rolloverDataset !=null) && 
							proj.hasDataset(rolloverDataset))
					pLabel.setRollover(true);
				else if (proj.hasDataset(selectedDataset) ||
						proj.sharesDatasetsWith(rolloverProject) ||
						(selectedProject != null &&
						(selectedProject.sharesDatasetsWith(proj))))
					pLabel.setActive();
				else
					pLabel.setNormal();
			}
		}
	}
}




