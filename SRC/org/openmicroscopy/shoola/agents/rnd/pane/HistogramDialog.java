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
class HistogramDialog
	extends JDialog
{
	private static final int		HEIGHT_WIN = 280;

	private HistogramPanel			histogramPanel;
	
	private HistogramDialogManager 	manager;
	
	HistogramDialog(QuantumMappingManager control, int mini, int maxi, 
					int start, int end, int yStart, int yEnd, 
					Object[] histogramData)
	{
		histogramPanel = new HistogramPanel(mini, maxi, start, end,
											yStart, yEnd, histogramData);
		manager = new HistogramDialogManager(this, control, yStart, yEnd);
		manager.attachListeners();
		//histogramPanel.setSize(HistogramPanel.WIDTH, HistogramPanel.HEIGHT);
		Container contentPane = super.getContentPane();
		contentPane.add(histogramPanel);
		setSize(HistogramPanel.WIDTH, HEIGHT_WIN);
		setResizable(false);
	}

	public HistogramPanel getHistogramPanel()
	{
		return histogramPanel;
	}
	
	public HistogramDialogManager getManager()
	{
		return manager;
	}

}
