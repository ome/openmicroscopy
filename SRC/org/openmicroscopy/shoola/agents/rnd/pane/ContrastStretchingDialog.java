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
import java.awt.Container;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies

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
	//TO BE REMOVED when we retrieve user setting from DB.
	private static final int				HEIGHT_WIN = 280;

	private ContrastStretchingPanel			csPanel;
	private ContrastStretchingDialogManager	manager;

	/**
	 * 
	 * @param control
	 * @param csDef
	 */
	//TODO: retrieve contrastStretching settings from DB.
	ContrastStretchingDialog(QuantumMappingManager control)
	{
		manager = new ContrastStretchingDialogManager(this, control);
		initPanel(control);
		manager.attachListeners();
		buildGUI();	
	}
	
	public ContrastStretchingPanel getCSPanel()
	{
		return csPanel;	
	}

	public ContrastStretchingDialogManager getManager()
	{
		return manager;
	}
	
	/** Inialize the {@link ContrastStretchingPanel}. */
	private void initPanel(QuantumMappingManager control)
	{
		int xStart, xEnd, yStart, yEnd;
		int lb = ContrastStretchingPanel.leftBorder;
		int tb = ContrastStretchingPanel.topBorder;
		int s = control.getCurOutputStart();
		int e = control.getCurOutputEnd();
		//TODO: to be modified, retrieve user settings from DB.
		xStart = lb+manager.convertRealIntoGraphics(s, e-s, s);
		xEnd = lb+manager.convertRealIntoGraphics(e, e-s, s);
		yStart = tb+manager.convertRealIntoGraphics(s, s-e, e);
		yEnd = tb+manager.convertRealIntoGraphics(e, s-e, e);
		/*
		xStart = lb+manager.convertRealIntoGraphics(csDef.getXStart(), e-s, s);
		xEnd = lb+manager.convertRealIntoGraphics(csDef.getXEnd(), e-s, s);
		yStart = tb+manager.convertRealIntoGraphics(csDef.getYStart(), s-e, e);
		yEnd = tb+manager.convertRealIntoGraphics(csDef.getYStart(), s-e, e);
		*/
		manager.setRectangles(xStart, xEnd, yStart, yEnd);
		csPanel = new ContrastStretchingPanel(xStart, xEnd, yStart, yEnd);
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		Container contentPane = super.getContentPane();
		contentPane.add(csPanel);
		setSize(ContrastStretchingPanel.WIDTH, HEIGHT_WIN);
		setResizable(false);
	}
	
}
