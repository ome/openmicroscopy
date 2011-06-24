/*
 * org.openmicroscopy.shoola.agents.metadata.editor.UserDiskSpace 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


//Third-party libraries
import org.jdesktop.swingx.JXBusyLabel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.Rotation;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Builds a graph with the used and free space.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class UserDiskSpace
	extends JScrollPane
{

	/** Reference to the view. */
	private UserUI		view;
	
	/** The component hosting the data. */
	private JPanel		data;
	
	/** Builds UI so the chart is not displayed.*/
	private void buildChartNotAvailable()
	{
		JLabel l = UIUtilities.setTextFont("Unable to create chart");
	    data.add(UIUtilities.buildComponentPanelCenter(l), 
				BorderLayout.CENTER);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view Reference to the view. Mustn't be <code>null</code>.
	 */
	UserDiskSpace(UserUI view)
	{
		this.view = view;
		data = new JPanel();
		data.setLayout(new BorderLayout());
		data.setPreferredSize(new Dimension(280, 270));
		data.setBackground(UIUtilities.BACKGROUND_COLOR);
		setPreferredSize(new Dimension(300, 270));
		getViewport().add(data);
	}

	/** Builds and lays out the GUI. */
	void buildGUI()
	{
		data.removeAll();
		DiskQuota quota = view.isDiskSpaceLoaded();
		if (quota != null) {
			DefaultPieDataset dataset = new DefaultPieDataset();
			long free = quota.getAvailableSpace();
			long used = quota.getUsedSpace();
			if (free < 0 || used < 0) {
				buildChartNotAvailable();
				return;
			}
			dataset.setValue("Free "+UIUtilities.formatFileSize(free), free);
			dataset.setValue("Used "+UIUtilities.formatFileSize(used), 
					used);
			try {
				JFreeChart chart = ChartFactory.createPieChart3D("", 
						dataset, false, true, false);
				PiePlot3D plot = (PiePlot3D) chart.getPlot();
				plot.setDirection(Rotation.CLOCKWISE);
			    plot.setForegroundAlpha(0.55f);
				data.add(new ChartPanel(chart), BorderLayout.CENTER);
			} catch (Exception e) {
				buildChartNotAvailable();
			}
			
		} else {
			JXBusyLabel busyLabel = new JXBusyLabel();
			busyLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
			busyLabel.setEnabled(true);
			busyLabel.setBusy(true);
			JPanel p = UIUtilities.buildComponentPanelCenter(busyLabel);
			p.setBackground(UIUtilities.BACKGROUND_COLOR);
			data.add(p, BorderLayout.CENTER);
		}
		revalidate();
		repaint();
	}
	
	/** Clears data. */
	void clearDisplay() { data.removeAll(); }
	
}
