/*
 * org.openmicroscopy.shoola.agents.rnd.pane.ContrastStretchingDialog
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

package org.openmicroscopy.shoola.agents.rnd.pane;


//Java imports
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.codomain.ContrastStretchingContext;
/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ContrastStretchingDialog
	extends JDialog
{
	private static final int 				LB = 
											ContrastStretchingPanel.leftBorder,
											TB = 
											ContrastStretchingPanel.topBorder;
	private ContrastStretchingPanel			csPanel;
	private ContrastStretchingDialogManager	manager;

	ContrastStretchingDialog(QuantumPaneManager control, 
							ContrastStretchingContext ctx)
	{
		super(control.getReferenceFrame(), "Contrast Stretching", true);
		manager = new ContrastStretchingDialogManager(this, control, ctx);
		initPanel(control, ctx);
		manager.attachListeners();
		buildGUI();	
	}
	
	ContrastStretchingPanel getCSPanel() { return csPanel; }

	ContrastStretchingDialogManager getManager() { return manager; }
	
	/** Initialize the {@link ContrastStretchingPanel}. */
	private void initPanel(QuantumPaneManager control, 
							ContrastStretchingContext ctx)
	{
		int xStart, xEnd, yStart, yEnd;
		int s = control.getCodomainStart();
		int e = control.getCodomainEnd();
		xStart = LB+manager.convertRealIntoGraphics(ctx.getXStart(), e-s, s);
		xEnd = LB+manager.convertRealIntoGraphics(ctx.getXEnd(), e-s, s);
		yStart = TB+manager.convertRealIntoGraphics(ctx.getYStart(), s-e, e);
		yEnd = TB+manager.convertRealIntoGraphics(ctx.getYEnd(), s-e, e);
		manager.setRectangles(xStart, xEnd, yStart, yEnd);
		csPanel = new ContrastStretchingPanel(xStart, xEnd, yStart, yEnd);
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		super.getContentPane().add(csPanel);
		setSize(ContrastStretchingPanel.WIDTH, ContrastStretchingPanel.HEIGHT+
					ContrastStretchingPanel.bottomBorder);
		setResizable(false);
	}
	
}
