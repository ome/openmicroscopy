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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.util.FilteringDialog;
import org.openmicroscopy.shoola.agents.dataBrowser.util.QuickFiltering;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.RatingComponent;
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
	implements ActionListener, ChangeListener
{

	/** ID to bring up the metadata browser. */
	private static final int	METADATA_SELECTION = 3;
	
	/** ID to bring up the metadata browser. */
	private static final int	METADATA_IMAGES = 4;
	
	/** ID to bring up the metadata browser. */
	private static final int	METADATA_CHILDREN = 5;
	
	/** ID to bring a slide show view with the displayed images. */
	private static final int	SLIDE_SHOW_IMAGES = 6;
	
	/** ID to bring a slide show view with the selected images. */
	private static final int	SLIDE_SHOW_SELECTION = 7;
	
	/** Reference to the control. */
	private DataBrowserControl 	controller;
	
	/** Reference to the view. */
	private DataBrowserUI		view;
	
	/** Reference to the quick search. */
	private QuickFiltering 		search;
	
	/** Slider to zoom the nodes. */
	private OneKnobSlider		zoomSlider;
	
	/** Button to bring up the filtering dialog. */
	private JButton				filterButton;
	
	/** The filtering dialog. */
	private FilteringDialog		filteringDialog;
	
	/** Button to select the thumbnails view. */
	private JToggleButton		thumbView;
	
	/** Button to select the table view. */
	private JToggleButton		columnsView;
	
	/** Button to select the slide show view. */
	private JButton				slideShowView;
	
	/** Button to add the metadata. */
	private JButton				metadataButton;
	
	/** Menu displaying the annotated options. */
	private JPopupMenu			annotateMenu;
	
	/** Menu displaying the annotated options. */
	private JPopupMenu			slideViewMenu;
	
	/**
	 * Creates the menu displaying the annotation options.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createAnnotateMenu()
	{
		if (annotateMenu != null) return annotateMenu;
		annotateMenu = new JPopupMenu();
		JMenuItem item = new JMenuItem("Annotate selection");
		item.addActionListener(this);
		item.setActionCommand(""+METADATA_SELECTION);
		annotateMenu.add(item);
		item = new JMenuItem("Annotate images");
		item.addActionListener(this);
		item.setActionCommand(""+METADATA_IMAGES);
		annotateMenu.add(item);
		item = new JMenuItem("Annotate images in selection");
		item.addActionListener(this);
		item.setActionCommand(""+METADATA_CHILDREN);
		annotateMenu.add(item);
		return annotateMenu;
	}
	
	/**
	 * Creates the menu displaying the annotation options.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createSlideVieweMenu()
	{
		if (slideViewMenu != null) return slideViewMenu;
		slideViewMenu = new JPopupMenu();
		JMenuItem item = new JMenuItem("View selected images");
		item.addActionListener(this);
		item.setActionCommand(""+SLIDE_SHOW_SELECTION);
		slideViewMenu.add(item);
		item = new JMenuItem("View displayed images");
		item.addActionListener(this);
		item.setActionCommand(""+SLIDE_SHOW_IMAGES);
		slideViewMenu.add(item);
		return slideViewMenu;
	}
	
	/** Sets the value of the filtering dialog. */
	private void setFilteringValue()
	{
		SearchObject filter = search.getSelectedNode();
		if (filter == null) return;
		switch (filter.getIndex()) {
			case QuickSearch.UNTAGGED:
				filteringDialog.setTagsText("");
				break;
			case QuickSearch.TAGS:
				filteringDialog.setTagsText(search.getSearchValue());
				break;
			case QuickSearch.UNCOMMENTED:
				filteringDialog.setCommentsText("");
				break;
			case QuickSearch.COMMENTS:
				filteringDialog.setCommentsText(search.getSearchValue());
				break;
			case QuickSearch.RATED_ONE_OR_BETTER:
				filteringDialog.setRatingLevel(DataBrowser.RATE_ONE);
				break;
			case QuickSearch.RATED_TWO_OR_BETTER:
				filteringDialog.setRatingLevel(DataBrowser.RATE_TWO);
				break;
			case QuickSearch.RATED_THREE_OR_BETTER:
				filteringDialog.setRatingLevel(DataBrowser.RATE_THREE);
				break;
			case QuickSearch.RATED_FOUR_OR_BETTER:
				filteringDialog.setRatingLevel(DataBrowser.RATE_FOUR);
				break;
			case QuickSearch.RATED_FIVE:
				filteringDialog.setRatingLevel(DataBrowser.RATE_FIVE);
				break;
			case QuickSearch.UNRATED:
				filteringDialog.setRatingLevel(RatingComponent.MIN_VALUE);
		}
	}
	
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
			filteringDialog.setTags(view.getExistingTags());
		}
		setFilteringValue();
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
		search = new QuickFiltering();
		search.addPropertyChangeListener(controller);
		filterButton = new JButton(icons.getIcon(IconManager.FILTERING));
		filterButton.addMouseListener(new MouseAdapter() {
		
			/**
			 * Brings up the filtering dialog.
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e) {
				showFilteringDialog(e.getPoint());
			}
		
		});
		filterButton.addPropertyChangeListener(controller);
		UIUtilities.unifiedButtonLookAndFeel(filterButton);
		
		ButtonGroup group = new ButtonGroup();
		int index = view.getSelectedView();
		thumbView = new JToggleButton(
					icons.getIcon(IconManager.THUMBNAIL_VIEW));
		thumbView.addActionListener(this);
		thumbView.setActionCommand(""+DataBrowserUI.THUMB_VIEW);
		thumbView.setSelected(index == DataBrowserUI.THUMB_VIEW);
		group.add(thumbView);
		columnsView = new JToggleButton(
				icons.getIcon(IconManager.COLUMN_VIEW));
		columnsView.addActionListener(this);
		columnsView.setActionCommand(""+DataBrowserUI.COLUMNS_VIEW);
		columnsView.setSelected(index == DataBrowserUI.COLUMNS_VIEW);
		group.add(columnsView);
		slideShowView = new JButton(
				icons.getIcon(IconManager.SLIDE_SHOW_VIEW));
		slideShowView.setToolTipText("Slideshow");
		UIUtilities.unifiedButtonLookAndFeel(slideShowView);
		slideShowView.addMouseListener(new MouseAdapter() {
			
			/**
			 * Brings up the filtering dialog.
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e) {
				createSlideVieweMenu().show(slideShowView, e.getX(), e.getY());
			}
		
		});
		//group.add(slideShowView);
		metadataButton = new JButton(icons.getIcon(IconManager.METADATA));
		metadataButton.addMouseListener(new MouseAdapter() {
			
			/**
			 * Brings up the filtering dialog.
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e) {
				createAnnotateMenu().show(metadataButton, e.getX(), e.getY());
			}
		
		});
		UIUtilities.unifiedButtonLookAndFeel(metadataButton);
	}
	
	/**
	 * Builds the tool bar with the various control for the view.
	 * 
	 * @return See above.
	 */
	private JToolBar buildViewsBar()
	{
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setBorder(null);
		bar.setRollover(true);
		bar.add(thumbView);
		bar.add(columnsView);
		bar.add(Box.createHorizontalStrut(2));
		bar.add(new JSeparator(JSeparator.VERTICAL));
		bar.add(Box.createHorizontalStrut(2));
		bar.add(slideShowView);
		bar.add(metadataButton);
		return bar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel p = new JPanel();
		p.add(filterButton);
		p.add(search);
		p.add(buildViewsBar());
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
	 * Sets the selected view index.
	 * 
	 * @param index The value to set.
	 */
	void setSelectedViewIndex(int index)
	{
		thumbView.removeActionListener(this);
		columnsView.removeActionListener(this);
		thumbView.setSelected(index == DataBrowserUI.THUMB_VIEW);
		columnsView.setSelected(index == DataBrowserUI.COLUMNS_VIEW);
		thumbView.addActionListener(this);
		columnsView.addActionListener(this);
	}
	
	/**
	 * Sets the enable flag of the {@link #slideShowView}.
	 * 
	 * @param enable The value to set.
	 */
	void enableSlideShow(boolean enable) { slideShowView.setEnabled(enable); }
	
	/**
	 * Returns the selected object in order to filter the node.
	 * 
	 * @return See above.
	 */
	SearchObject getSelectedFilter() { return search.getSelectedNode(); }

	/**
	 * Sets the collection of tags.
	 * 
	 * @param tags
	 */
	void setTags(Collection tags)
	{
		if (filteringDialog != null && filteringDialog.isVisible()) 
			filteringDialog.setTags(tags);
		else 
			search.setTags(tags);
	}
	
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

	/** 
	 * Sets the specified view.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case DataBrowserUI.THUMB_VIEW:
			case DataBrowserUI.COLUMNS_VIEW:
				view.setSelectedView(index);
				break;
			case METADATA_IMAGES:
				controller.annotate(DataBrowser.ANNOTATE_IMAGES);
				break;
			case METADATA_SELECTION:
				controller.annotate(DataBrowser.ANNOTATE_SELECTION);
				break;
			case METADATA_CHILDREN:
				controller.annotate(DataBrowser.ANNOTATE_CHILDREN);
				break;
			case SLIDE_SHOW_IMAGES:
				view.slideShowView(true, true);
				break;	
			case SLIDE_SHOW_SELECTION:
				view.slideShowView(true, false);
				break;	
		}
	}
	
}
