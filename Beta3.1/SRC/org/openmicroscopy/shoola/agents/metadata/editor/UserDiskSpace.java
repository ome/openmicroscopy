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
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;


//Third-party libraries
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;

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

	/** The title of the chart. */
	static final String TITLE = "Disk Space";

	/** Reference to the model. */
	//private EditorModel model;
	
	/** The collapse version of this component. */
	private JPanel		collapseComponent;
	
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
		setBorder(new TitledLineBorder(TITLE, getBackground()));
		setPreferredSize(new Dimension(300, 200));
	}
	
	/**
	 * Returns the {@link #collapseComponent}. Creates it if not.
	 * 
	 * @return See above.
	 */
	protected JPanel getCollapseComponent()
	{
		if (collapseComponent != null)
			return collapseComponent;
		collapseComponent = new JPanel();
		collapseComponent.setBorder(new TitledLineBorder(TITLE, 
						collapseComponent.getBackground()));
		return collapseComponent;
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
			JFreeChart freeChart = ChartFactory.createPieChart(TITLE, dataset, 
															false, true, false);
			add(new ChartPanel(freeChart), BorderLayout.CENTER);
		} else {
			JLabel label = new JLabel("Loading...");
			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
			p.add(label);
			JProgressBar bar =  new JProgressBar();
			bar.setIndeterminate(true);
			p.add(UIUtilities.buildComponentPanelRight(bar));
			add(p, BorderLayout.NORTH);
		}
		revalidate();
		repaint();
	}
	
}
