/*
 * org.openmicroscopy.shoola.agents.treeviewer.profile.DiskSpace 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.profile;



//Java imports
import java.awt.BorderLayout;
import java.text.NumberFormat;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;


//Third-party libraries
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Application-internal dependencies

/** 
 * Displays the used and free disk space on the file system.
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
class DiskSpace
	extends JPanel
{

	/** The title of the chart. */
	private static final String TITLE = "Server Disk Space";

	/**
	 * Converts the passed value into a string in Mb and returns a string 
	 * version of it.
	 * 
	 * @param v The value to convert.
	 * @return See above.
	 */
	private String convertValue(long v)
	{
		long value = v;///1000;
		NumberFormat.getInstance().format(v);
		if (value > 1000) value = value/1000;
		else return NumberFormat.getInstance().format(value)+" Kb";
		return NumberFormat.getInstance().format(value)+" Mb";
	}
	
	/** Builds and lays out the GUI. */
	void buildGUI()
	{
		removeAll();
		setLayout(new BorderLayout());
		JLabel label = new JLabel("Loading and building graph");
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(label);
		JProgressBar bar =  new JProgressBar();
		bar.setIndeterminate(true);
		p.add(UIUtilities.buildComponentPanelRight(bar));
		add(p, BorderLayout.NORTH);
		add(new JPanel(), BorderLayout.CENTER);
	}
	
	/** Creates a new instance. */
	DiskSpace()
	{
		//buildGUI();
	}
	
	/**
	 * Builds the graph displaying the used and free space in kilobytes on the 
	 * file system.
	 * 
	 * @param used	The used space in kilobytes on the file system.
	 * @param free	The free space in kilobytes on the file system.
	 */
	void buildGraph(long used, long free)
	{
		DefaultPieDataset dataset = new DefaultPieDataset();
		dataset.setValue("Free "+convertValue(free), free);
		dataset.setValue("Used "+convertValue(used), used);
		JFreeChart freeChart = ChartFactory.createPieChart(TITLE, dataset, 
														false, true, false);
		removeAll();
		add(new ChartPanel(freeChart), BorderLayout.CENTER);
		validate();
		repaint();
	}
	
}
