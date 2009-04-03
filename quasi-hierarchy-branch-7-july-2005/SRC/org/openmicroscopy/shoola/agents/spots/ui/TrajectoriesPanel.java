/*
 * org.openmicroscopy.shoola.agents.spots.ui.TrajectoriesPanel
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

package org.openmicroscopy.shoola.agents.spots.ui;

//Java imports
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectorySet;
import org.openmicroscopy.shoola.agents.spots.range.AxisBoundedRangeModel;
import org.openmicroscopy.shoola.agents.spots.range.RangeModelMediator;
import org.openmicroscopy.shoola.agents.spots.ui.java3d.Spots3DCanvas;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * The panel containing all three projection {@link TrajectoryPanel} instances
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */





// a panel containing the three projected views

public class TrajectoriesPanel  extends JPanel {
	
	private TrajectoryPanel xypanel;
	private TrajectoryPanel yzpanel;
	private TrajectoryPanel zxpanel;
	
	private StatusPanel statusPanel;
	
	public TrajectoriesPanel(Registry registry,
			Spots3DCanvas canvas,SpotsTrajectorySet tSet) {
		super();
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		// do xy
		
		xypanel = new TrajectoryPanel(registry,
				this,SpotsTrajectory.X,SpotsTrajectory.Y,true,tSet);
		add(xypanel);
		
		
		yzpanel = new TrajectoryPanel(registry,
				this,SpotsTrajectory.Y,SpotsTrajectory.Z,false,tSet);
	
		add(yzpanel);
		
		
		// zx
		zxpanel = new TrajectoryPanel(registry,
				this,SpotsTrajectory.Z,SpotsTrajectory.X,false,tSet);
	
		add(zxpanel);
				 
		statusPanel = new StatusPanel();
		add(statusPanel);
		setPreferredSize(new Dimension(200,500));
		
		// more on listeners
		AxisBoundedRangeModel xmodel = xypanel.getModel();
		AxisBoundedRangeModel ymodel = yzpanel.getModel();
		AxisBoundedRangeModel zmodel = zxpanel.getModel();
		
		xmodel.addChangeListener(canvas);
		ymodel.addChangeListener(canvas);
		zmodel.addChangeListener(canvas);
		RangeModelMediator xymed = new RangeModelMediator(xmodel,ymodel,
				xypanel.getCanvas());
		RangeModelMediator yzmed = new RangeModelMediator(ymodel,zmodel,
				yzpanel.getCanvas());
		RangeModelMediator zxmed = new RangeModelMediator(zmodel,xmodel,
				zxpanel.getCanvas());
	}
	
	public void initialize(SpotsTrajectorySet tSet) {
		xypanel.initialize(tSet);
		yzpanel.initialize(tSet);
		zxpanel.initialize(tSet);
		statusPanel.setCount(tSet.getTrajectoryCount());
	}
	
	public void repaintCanvases() {
		xypanel.repaint();
		yzpanel.repaint();
		zxpanel.repaint();
	}

	
	public void setVals(int visible) {
		statusPanel.setVals(visible);
	}
}