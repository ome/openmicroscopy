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
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStatsEntry;

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
	/** Height of the widget. */
	private static final int		HEIGHT_WIN = 280;

	private HistogramPanel			histogramPanel;
	
	private HistogramDialogManager 	manager;
	
	HistogramDialog(QuantumPaneManager control, int mini, int maxi, 
					int start, int end, PixelsStatsEntry[] histogramData)
	{
		super(control.getReferenceFrame(), "Histogram", true);
		manager = new HistogramDialogManager(this, control);
		int yStart, yEnd;
		yStart = manager.convertRealIntoGraphics(start);
		yEnd = manager.convertRealIntoGraphics(end);
		histogramPanel = new HistogramPanel(mini, maxi, start, end, yStart, 
											yEnd, histogramData);
		manager.initRectangles(yStart, yEnd);										
		manager.attachListeners();
		buildGUI();	
	}

	public HistogramPanel getHistogramPanel()
	{
		return histogramPanel;
	}
	
	public HistogramDialogManager getManager()
	{
		return manager;
	}

	/** Build and layout the GUI. */
	void buildGUI()
	{
		//histogramPanel.setSize(HistogramPanel.WIDTH, HistogramPanel.HEIGHT);
		super.getContentPane().add(histogramPanel);
		setSize(HistogramPanel.WIDTH, HEIGHT_WIN);
		setResizable(false);
	}
	
}
