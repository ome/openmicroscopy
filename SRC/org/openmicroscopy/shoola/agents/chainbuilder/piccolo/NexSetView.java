/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.NexSetView;
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package org.openmicroscopy.shoola.agents.chainbuilder.piccolo;

//Java imports
import java.util.Collection;
import java.util.Iterator;

//Third-party libraries
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainNodeExecutionData;
import org.openmicroscopy.shoola.util.ui.Constants;

/** 
 * A Piccolo node that wraps around individual or aggregate views of nex sets.
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public class NexSetView extends PNode {
	
	private static final int BAR_THRESHOLD=500;
	private static final float BAR_HEIGHT=30.0f;
	
	private static final int MEX_GAP=1;
	
	private ChainNodeExecutionData nex;
	
	private PNode viewsNode;
	
	private PPath viewsBar;

	public NexSetView(Collection nexes, float width,int maxCount) {
		super();
		addNexViews(nexes,width,maxCount);
		if (nexes.size() > BAR_THRESHOLD) {
			System.err.println("showing bar. hiding individuals.");
			//viewsNode.setVisible(false);
			addNexBar(nexes,width,maxCount);
		} 
		
	}
	
	private void addNexViews(Collection nexes, float width,int maxCount) {
		
		viewsNode = new PNode();
//		 set up the mex node
		double x = 0;
		double y = 0;
		// get the width of the node thus far.
		
		// add the mexes.
		ChainNodeExecutionData nex;
		Iterator iter = nexes.iterator();
		NexView view;
		
		while (iter.hasNext()) {
			nex = (ChainNodeExecutionData) iter.next();
			view = new NexView(nex);
			viewsNode.addChild(view);
			if (x + view.getWidth() > width) {
				// move to next row
				x =0;
				y += view.getHeight()+MEX_GAP;
			}
			view.setOffset(x,y);
			x += view.getWidth()+MEX_GAP;
		}
		addChild(viewsNode);
		viewsNode.setBounds(getUnionOfChildrenBounds(null));
	}
	
	private void addNexBar(Collection nexes, float width,int maxCount) {
		// find rect. width  - scale width by ratio of # of nexes
		// to max # of nexes
		float barWidth = width *nexes.size()/maxCount;
		viewsBar = PPath.createRectangle(0,0,barWidth,BAR_HEIGHT);
		viewsBar.setPaint(null);
		viewsBar.setStrokePaint(Constants.NEX_COLOR);
		addChild(viewsBar);
		
	}
	
	public void setPosition() {
		setBounds(getUnionOfChildrenBounds(null));
		setOffset(0,-getHeight());
		if (viewsBar != null) {
			System.err.println("views node height is "+viewsNode.getHeight()+", ");
			System.err.println("views bar height is" +viewsBar.getHeight());
			viewsBar.setOffset(0,viewsNode.getHeight()-viewsBar.getHeight());
		}
		
	}
	
	public void showNexViews() {
		showNexViews(true);
	}
	
	public void showNexBar() {
		showNexViews(false);
	}
	
	private void showNexViews(boolean b) {
		viewsNode.setVisible(b);
		viewsBar.setVisible(!b);
	}
}
