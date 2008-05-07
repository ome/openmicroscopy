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
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.util.FilteringDialog;
import org.openmicroscopy.shoola.agents.dataBrowser.util.ObjectEditor;
import org.openmicroscopy.shoola.agents.dataBrowser.util.QuickFiltering;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.RatingComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.QuickSearch;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;

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
	implements ActionListener
{

	/** The text of the menu. */
	private static final String	ITEMS_PER_ROW_TEXT = "Images per row";
	
	/** ID to bring up the add thumbnail view to the node.. */
	private static final int	ROLL_OVER = 10;
	
	/** ID to bring up the add thumbnail view to the node.. */
	private static final int	MOUSE_OVER = 11;
	
	/** ID to bring up the metadata browser. */
	private static final int	NEW_OBJECT = 12;
	
	/** ID to bring a slide show view with the displayed images. */
	private static final int	SLIDE_SHOW_IMAGES = 13;
	
	/** ID to bring a slide show view with the selected images. */
	private static final int	SLIDE_SHOW_SELECTION = 14 ;
	
	/** ID to bring up the metadata browser. */
	private static final int	ITEMS_PER_ROW = 15;

	/** Reference to the control. */
	private DataBrowserControl 	controller;
	
	/** Reference to the view. */
	private DataBrowserUI		view;
	
	/** Reference to the quick search. */
	private QuickFiltering 		search;
	
	/** Button to bring up the filtering dialog. */
	private JButton				filterButton;
	
	/** The filtering dialog. */
	private FilteringDialog		filteringDialog;
	
	/** Button to select the thumbnails view. */
	private JToggleButton		thumbView;
	
	/** Button to select the table view. */
	private JToggleButton		columnsView;
	
	/** Button to order alphabetically. */
	private JToggleButton		orderByName;
	
	/** Button to order by imported date. */
	private JToggleButton		orderByDate;
	
	/** Button to select the slide show view. */
	private JButton				slideShowView;
	
	/** Button to add the metadata. */
	private JButton				managementButton;

	/** Button to refresh the display. */
	private JButton				refreshButton;
	
	/** Menu displaying the annotated options. */
	private JPopupMenu			slideViewMenu;
	
	/** Menu displaying the annotated options. */
	private JPopupMenu			manageMenu;
	
	/** The item used to select the roll over mode. */
	private JCheckBoxMenuItem 	rollOverItem;
	
	/** The item used to select the mouse over mode. */
	private JCheckBoxMenuItem 	mouseOverItem;
	
	/** TextField hosting the number of items per row. */
	private JTextField			itemsPerRow;
	
	/** Indicates how many images are shown. */
	private JLabel				filteringLabel;
	
	/**
	 * Creates the menu displaying various management options options.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createManageMenu()
	{
		if (manageMenu != null) return manageMenu;
		IconManager icons = IconManager.getInstance();
		manageMenu = new JPopupMenu();
		
		JMenuItem menuItem = new JMenuItem("New dataset");
		menuItem.setToolTipText("Create a dataset.");
		menuItem.setIcon(icons.getIcon(IconManager.CREATE));
		menuItem.addActionListener(this);
		menuItem.setActionCommand(""+NEW_OBJECT);
		manageMenu.add(menuItem);
		
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JLabel l = new JLabel(icons.getIcon(IconManager.TRANSPARENT));
		l.setText(ITEMS_PER_ROW_TEXT);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(l);
		itemsPerRow = new JTextField(3);
		itemsPerRow.addActionListener(this);
		itemsPerRow.setActionCommand(""+ITEMS_PER_ROW);
		panel.add(itemsPerRow);
		manageMenu.add(panel);
		//manageMenu.add(itemsPerRow);
		
		rollOverItem = new JCheckBoxMenuItem();
		rollOverItem.setIcon(icons.getIcon(IconManager.ROLL_OVER));
		rollOverItem.setText("Mouse over and Magnify");
		rollOverItem.addActionListener(this);
		rollOverItem.setActionCommand(""+ROLL_OVER);
		manageMenu.add(rollOverItem);
		mouseOverItem = new JCheckBoxMenuItem();
		mouseOverItem.setIcon(icons.getIcon(IconManager.ROLL_OVER));
		mouseOverItem.setText("Mouse over and Magnify");
		mouseOverItem.addActionListener(this);
		mouseOverItem.setActionCommand(""+MOUSE_OVER);
		//manageMenu.add(mouseOverItem);
		return manageMenu;
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
		JMenuItem item = new JMenuItem("View Selected Images");
		item.addActionListener(this);
		item.setActionCommand(""+SLIDE_SHOW_SELECTION);
		slideViewMenu.add(item);
		item = new JMenuItem("View Available Images");
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
		filteringLabel = new JLabel();
		filteringLabel.setFont(filteringLabel.getFont().deriveFont(Font.BOLD));
		IconManager icons = IconManager.getInstance();
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
		//UIUtilities.unifiedButtonLookAndFeel(slideShowView);
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
		managementButton = new JButton(icons.getIcon(IconManager.MANAGER));
		managementButton.addMouseListener(new MouseAdapter() {
			
			/**
			 * Brings up the filtering dialog.
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e) {
				createManageMenu().show(managementButton, e.getX(), e.getY());
			}
		
		});
		//UIUtilities.unifiedButtonLookAndFeel(managementButton);
		
		//
		group = new ButtonGroup();
		orderByName = new JToggleButton(
				icons.getIcon(IconManager.SORT_BY_NAME));
		orderByName.addActionListener(this);
		orderByName.setActionCommand(""+DataBrowserUI.SORT_BY_NAME);
		orderByName.setSelected(true);
		group.add(orderByName);
		orderByDate = new JToggleButton(
				icons.getIcon(IconManager.SORT_BY_DATE));
		orderByDate.addActionListener(this);
		orderByDate.setActionCommand(""+DataBrowserUI.SORT_BY_DATE);
		group.add(orderByDate);
		
		refreshButton = new JButton(controller.getAction(
								DataBrowserControl.REFRESH));
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
		bar.add(orderByName);
		bar.add(orderByDate);
		bar.add(Box.createHorizontalStrut(2));
		bar.add(new JSeparator(JSeparator.VERTICAL));
		bar.add(Box.createHorizontalStrut(2));
		bar.add(slideShowView);
		bar.add(managementButton);
		bar.add(refreshButton);
		//bar.add(Box.createHorizontalStrut(2));
		//bar.add(new JSeparator(JSeparator.VERTICAL));
		return bar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		JPanel p = new JPanel();
		p.add(filterButton);
		p.add(search);
		p.add(buildViewsBar());
		content.add(p);
		JPanel labelPane = new JPanel();
		labelPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		labelPane.add(filteringLabel);
		content.add(labelPane);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(content);
	}
	
	/** Sets the number of items per row. */
	private void parseItemsPerRow()
	{
		int row = -1;
		try {
			row = Integer.parseInt(itemsPerRow.getText());
		} catch(NumberFormatException nfe) {}
		
		if (row < 0) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid number", "Please enter a valid number " +
            		"of images per row.");
		} else {
			view.setItemsPerRow(row);
			manageMenu.setVisible(false);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view 			Reference to the view. Mustn't be <code>null</code>.
	 * @param controller 	Reference to the control. 
	 * 						Mustn't be <code>null</code>.
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
		//else 
		search.setTags(tags);
	}

	/**
	 * Sets the number of images.
	 * 
	 * @param value The number of images displayed.
	 * @param total	The total number of images.
	 */
	void setNumberOfImages(int value, int total)
	{
		String s = "Showing "+value+" of "+total+" image";
		if (total > 1) s += "s";
		filteringLabel.setText(s);
		filteringLabel.repaint();
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
			case ROLL_OVER:
				view.setRollOver(rollOverItem.isSelected());
				break;
			case NEW_OBJECT:
				Registry reg = DataBrowserAgent.getRegistry();
				ObjectEditor dialog = new ObjectEditor(
										reg.getTaskBar().getFrame(), 
										ObjectEditor.DATASET);
				dialog.addPropertyChangeListener(controller);
				UIUtilities.centerAndShow(dialog);
				break;
			case ITEMS_PER_ROW:
				parseItemsPerRow();
				break;
			case SLIDE_SHOW_IMAGES:
				view.slideShowView(true, true);
				break;	
			case SLIDE_SHOW_SELECTION:
				view.slideShowView(true, false);
				break;	
			case DataBrowserUI.SORT_BY_NAME:
			case DataBrowserUI.SORT_BY_DATE:
				view.sortBy(index);
		}
	}
	
}
