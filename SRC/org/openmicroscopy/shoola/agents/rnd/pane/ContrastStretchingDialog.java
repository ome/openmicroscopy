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
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.codomain.ContrastStretchingContext;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

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
	
	private static final String				TEXT = "Increase the dynamic " +
											"range of the gray levels.";
	
	private ContrastStretchingPanel			csPanel;
	
	private ContrastStretchingDialogManager	manager;

	private JLayeredPane					layeredPane;
	
	ContrastStretchingDialog(QuantumPaneManager control, 
							ContrastStretchingContext ctx)
	{
		super(control.getReferenceFrame(), "Contrast Stretching", true);
		manager = new ContrastStretchingDialogManager(this, control, ctx);
		initPanel(control, ctx);
		manager.attachListeners();
		buildGUI(control.getRegistry());	
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
		xStart = ContrastStretchingPanel.leftBorder+
				manager.convertRealIntoGraphics(ctx.getXStart(), e-s, s);
		xEnd = ContrastStretchingPanel.leftBorder+
				manager.convertRealIntoGraphics(ctx.getXEnd(), e-s, s);
		yStart = ContrastStretchingPanel.topBorder+
				manager.convertRealIntoGraphics(ctx.getYStart(), s-e, e);
		yEnd = ContrastStretchingPanel.topBorder+
				manager.convertRealIntoGraphics(ctx.getYEnd(), s-e, e);
		manager.setRectangles(xStart, xEnd, yStart, yEnd);
		csPanel = new ContrastStretchingPanel(xStart, xEnd, yStart, yEnd);
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI(Registry registry)
	{
		IconManager im = IconManager.getInstance(registry);
		buildLayeredPane();
		TitlePanel tp = new TitlePanel("Contrast Stretching", TEXT, 
										QuantumPane.NOTE,
										im.getIcon(IconManager.STRETCHING_BIG));
		getContentPane().add(tp, BorderLayout.NORTH);
		getContentPane().add(layeredPane, BorderLayout.CENTER);
		setResizable(false);
		pack();
	}
	
	/** 
	 * Builds a layeredPane containing the GraphicsRepresentation.
	 *
	 * @return the above mentioned.
	 */   
	private	void buildLayeredPane()
	{
		layeredPane = new JLayeredPane();
		Dimension d = new Dimension(3*ContrastStretchingPanel.WIDTH/2, 
										3*ContrastStretchingPanel.HEIGHT/2);
		layeredPane.setPreferredSize(d);
		layeredPane.add(csPanel);
	}
	
}
