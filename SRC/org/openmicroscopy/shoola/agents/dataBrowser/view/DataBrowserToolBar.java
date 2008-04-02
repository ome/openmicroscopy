/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserToolBar 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.util.FilteringDialog;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.QuickSearch;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;

/** 
 * The tool bar of {@link DataBrowser}. 
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
class DataBrowserToolBar 
	extends JPanel
	implements ChangeListener
{

	/** Reference to the control. */
	private DataBrowserControl 	controller;
	
	/** Reference to the view. */
	private DataBrowserUI		view;
	
	/** Reference to the quick search. */
	private QuickSearch 		search;
	
	/** Slider to zoom the nodes. */
	private OneKnobSlider		zoomSlider;
	
	/** Button to bring up the filtering dialog. */
	private JButton				filterButton;
	
	/** The filtering dialog. */
	private JDialog				filteringDialog;
	
	/** 
	 * Brings up the filtering dialog. 
	 * 
	 * @param location The location of the mouse pressed.
	 */
	private void showFilteringDialog(Point location)
	{
		if (filteringDialog == null) {
			Registry reg = DataBrowserAgent.getRegistry();
			filteringDialog = new FilteringDialog(reg.getTaskBar().getFrame());
			filteringDialog.addPropertyChangeListener(controller);
		}
		SwingUtilities.convertPointToScreen(location, filterButton);
		filteringDialog.setLocation(location);
		filteringDialog.setVisible(true);
		
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		
		zoomSlider = new OneKnobSlider(OneKnobSlider.HORIZONTAL, 1, 10, 5);
		zoomSlider.setEnabled(true);
		zoomSlider.setShowArrows(true);
		zoomSlider.setToolTipText("Magnifies all thumbnails.");
		zoomSlider.setArrowsImageIcon(
				icons.getImageIcon(IconManager.ZOOM_IN), 
				icons.getImageIcon(IconManager.ZOOM_OUT));
		search = new QuickSearch();
		search.setSingleSelection(true);
		search.setDefaultSearchContext();
		search.addPropertyChangeListener(controller);
		filterButton = new JButton(icons.getIcon(IconManager.FILTERING));
		filterButton.addMouseListener(new MouseAdapter() {
		
			/**
			 * Brings up the filtering dialog.
			 */
			public void mouseReleased(MouseEvent e) {
				showFilteringDialog(e.getPoint());
			}
		
		});
		filterButton.addPropertyChangeListener(controller);
		UIUtilities.unifiedButtonLookAndFeel(filterButton);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel p = new JPanel();
		p.add(filterButton);
		p.add(search);
		
		//p.add(zoomSlider);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(p);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view 		Reference to the view. Mustn't be <code>null</code>.
	 * @param controller Reference to the control. Mustn't be <code>null</code>.
	 */
	DataBrowserToolBar(DataBrowserUI view, DataBrowserControl controller)
	{
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.controller = controller;
		this.view = view;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Returns the selected object in order to filter the node.
	 * 
	 * @return See above.
	 */
	SearchObject getSelectedFilter() { return search.getSelectedNode(); }

	/** 
	 * Zooms in or out the thumbnails.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		Object src = e.getSource();
		if (src == zoomSlider) {
			
		}
		
	}
	
}
