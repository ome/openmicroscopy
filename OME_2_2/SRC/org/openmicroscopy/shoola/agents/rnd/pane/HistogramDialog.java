/*
 * org.openmicroscopy.shoola.agents.rnd.pane.HistogramDialog
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
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStatsEntry;
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
class HistogramDialog
	extends JDialog
{
	
	private HistogramPanel			histogramPanel;
	
	private HistogramDialogManager 	manager;
	
	private JLayeredPane			layeredPane;
	
	HistogramDialog(QuantumPaneManager control, int mini, int maxi, 
					int start, int end, PixelsStatsEntry[] histogramData)
	{
		super(control.getReferenceFrame(), "Histogram", false);
		manager = new HistogramDialogManager(this, control);
		int yStart, yEnd;
		yStart = manager.convertRealIntoGraphics(start);
		yEnd = manager.convertRealIntoGraphics(end);
		histogramPanel = new HistogramPanel(manager, mini, maxi, start, end, 
											yStart, yEnd, histogramData);
		manager.initRectangles(yStart, yEnd);										
		manager.attachListeners();
		buildGUI(control.getRegistry());	
	}

	HistogramPanel getHistogramPanel() { return histogramPanel; }
	
	HistogramDialogManager getManager() { return manager; }

	/** Build and lay out the GUI. */
	void buildGUI(Registry registry)
	{	
		IconManager im = IconManager.getInstance(registry);
		buildLayeredPane();
		TitlePanel tp = new TitlePanel("Histogram", 
										" Select the pixels intensity " +
										"interval across time.", 
										im.getIcon(IconManager.HISTOGRAM_BIG));
		getContentPane().add(tp, BorderLayout.NORTH);
		getContentPane().add(layeredPane, BorderLayout.CENTER);
		//setResizable(false);
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
		layeredPane.setPreferredSize(new Dimension(histogramPanel.getWidthWin(), 
									HistogramPanel.HEIGHT));
		layeredPane.add(histogramPanel);
	}
	
}
