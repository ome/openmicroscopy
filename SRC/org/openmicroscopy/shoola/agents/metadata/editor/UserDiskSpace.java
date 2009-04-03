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
import java.util.List;
import javax.swing.JPanel;


//Third-party libraries
import org.jdesktop.swingx.JXBusyLabel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

//Application-internal dependencies
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
	extends JPanel
{

	/** Reference to the view. */
	private UserUI		view;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view Reference to the view. Mustn't be <code>null</code>.
	 */
	UserDiskSpace(UserUI view)
	{
		this.view = view;
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(300, 200));
		setBackground(UIUtilities.BACKGROUND_COLOR);
	}

	/** Builds and lays out the GUI. */
	void buildGUI()
	{
		removeAll();
		List list = view.isDiskSpaceLoaded();
		if (list != null) {
			DefaultPieDataset dataset = new DefaultPieDataset();
			long free = (Long) list.get(0);
			long used = (Long) list.get(1);
			dataset.setValue("Free "+UIUtilities.formatFileSize(free), free);
			dataset.setValue("Used "+UIUtilities.formatFileSize(used), used);
			JFreeChart freeChart = ChartFactory.createPieChart("", 
					dataset, false, true, false);
			add(new ChartPanel(freeChart), BorderLayout.CENTER);
		} else {
			JXBusyLabel busyLabel = new JXBusyLabel();
			busyLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
			busyLabel.setEnabled(true);
			busyLabel.setBusy(true);
			JPanel p = UIUtilities.buildComponentPanelCenter(busyLabel);
			p.setBackground(UIUtilities.BACKGROUND_COLOR);
			add(p, BorderLayout.CENTER);
		}
		revalidate();
		repaint();
	}
	
}
