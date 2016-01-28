/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.util.FilteringDialog;
import org.openmicroscopy.shoola.agents.dataBrowser.util.QuickFiltering;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.util.filter.file.ExcelFilter;
import org.openmicroscopy.shoola.util.ui.RatingComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;
import omero.gateway.model.DatasetData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TextualAnnotationData;

/** 
 * The tool bar of {@link DataBrowser}. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class DataBrowserToolBar 
	extends JPanel
	implements ActionListener, PropertyChangeListener
{

	/** The text of the menu. */
	private static final String	ITEMS_PER_ROW_TEXT = "Set the number of " +
			"elements per row.";
	
	/** The text of the menu. */
	private static final String	FILTER_BY = "Filter by: ";
	
	/** ID to bring up the add thumbnail view to the node.. */
	private static final int	ROLL_OVER = 10;
	
	/** ID to create a new dataset. */
	private static final int	NEW_OBJECT = 11;
	
	/** ID to bring a slide show view with the displayed images. */
	private static final int	SLIDE_SHOW_IMAGES = 12;
	
	/** ID to bring a slide show view with the selected images. */
	private static final int	SLIDE_SHOW_SELECTION = 13 ;
	
	/** ID to bring up the metadata browser. */
	private static final int	ITEMS_PER_ROW = 14;

	/** ID to bring up the metadata browser. */
	private static final int	REPORT = 15;
	
	/** ID to select an existing dataset. */
	private static final int	EXISTING_OBJECT = 16;
	
	/** ID to load the tags to filter by. */
	private static final int	TAG_FILTER = 17;
	
	/** Reference to the control. */
	private DataBrowserControl 	controller;
	
	/** Reference to the view. */
	private DataBrowserUI		view;
	
	/** Reference to the Model. */
	private DataBrowserModel	model;
	
	/** Reference to the quick search. */
	private QuickFiltering 		search;
	
	/** Button to bring up the filtering dialog. */
	private JButton				filterButton;
	
	/** Button to bring up menu. */
	private JButton				filterByMenuButton;
	
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
	
	/** Button to refresh the display. */
	private JButton				saveButton;
	
	/** Menu displaying the annotated options. */
	private JPopupMenu			slideViewMenu;
	
	/** Menu displaying the annotated options. */
	private JPopupMenu			manageMenu;
	
	/** The item used to select the roll over mode. */
	private JCheckBoxMenuItem 	rollOverItem;
	
	/** 
	 * Button to display a magnified thumbnail if selected when 
	 * the user mouses over a node.
	 */
	private JToggleButton 		rollOverButton;
	
	/** Button to create a dataset. */
	private JButton				createDatasetButton;
	
	/** TextField hosting the number of items per row. */
	private JTextField			itemsPerRow;
	
	/** The component hosting the {@link #itemsPerRow}.*/
	private JPanel              itemsPerRowPane;
	
	/** Indicates how many images are shown. */
	private JLabel				status;
	
	/** Indicates how many images are shown. */
	private JLabel				filteringLabel;
	
	/** Button to create an image Tags report. */
	private JButton				reportButton;
	
	/** The menu displaying options to create objects. */
	private JPopupMenu			createMenu;
	
	/** The menu displaying options to filter by. */
	private JPopupMenu			filterByMenu;
	
	/** 
	 * Flag indicating that the loading of the tags was done via the code
	 * completion or not.
	 */
	private boolean				codeCompletion;
	
	/** 
	 * Displays the available tags and allows the users to select them. 
	 * 
	 * @param existingTags The tags already saved. 
	 */
	private void showTagsWizard(Collection existingTags)
	{
		if (existingTags == null) return;
		IconManager icons = IconManager.getInstance();
		String title = "Filter By Tags";
		String text = "Select the Tags to filter by.";
		Collection selected = new ArrayList<TagAnnotationData>();
		Iterator i = existingTags.iterator();
		TagAnnotationData tag;
		List<String> l = SearchUtil.splitTerms(search.getSearchValue(), 
				SearchUtil.COMMA_SEPARATOR);
		Collection available = new ArrayList<TagAnnotationData>();
		
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (l.contains(tag.getTagValue()))
				selected.add(tag);
			else available.add(tag);
		}
		SelectionWizard wizard = new SelectionWizard(
				DataBrowserAgent.getRegistry().getTaskBar().getFrame(), 
				available, selected, TagAnnotationData.class, false,
				DataBrowserAgent.getUserDetails());
		wizard.setTitle(title, text, icons.getIcon(IconManager.TAG_FILTER_48));
		wizard.addPropertyChangeListener(this);
		UIUtilities.centerAndShow(wizard);
	}
	
	/**
	 * Creates the menu displaying the options to load existing objects.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createFilterByMenu()
	{
		if (filterByMenu != null) return filterByMenu;
		filterByMenu = new JPopupMenu();
		IconManager icons = IconManager.getInstance();
		JMenuItem menuItem = new JMenuItem("Load Tags to filter by...");
		menuItem.setToolTipText("Load the existing Tags to filter by.");
		menuItem.setIcon(icons.getIcon(IconManager.TAG_FILTER));
		menuItem.addActionListener(this);
		menuItem.setActionCommand(""+TAG_FILTER);
		filterByMenu.add(menuItem);
		return filterByMenu;
	}
	
	/**
	 * Creates the menu displaying various create options.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createNewObjectMenu()
	{
		if (createMenu != null) return createMenu;
		IconManager icons = IconManager.getInstance();
		createMenu = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("New Dataset");
		menuItem.setToolTipText("Create a Dataset.");
		menuItem.setIcon(icons.getIcon(IconManager.CREATE));
		menuItem.addActionListener(this);
		menuItem.setActionCommand(""+NEW_OBJECT);
		menuItem.setEnabled(model.canLinkParent());
		createMenu.add(menuItem);
		menuItem = new JMenuItem("Existing Dataset");
		menuItem.setToolTipText("Select a dataset to add the images to.");
		menuItem.setIcon(icons.getIcon(IconManager.DATASET));
		menuItem.addActionListener(this);
		menuItem.setActionCommand(""+EXISTING_OBJECT);
		createMenu.add(menuItem);
		menuItem.setEnabled(model.canLinkParent());
		return createMenu;
	}
	
	/**
	 * Creates the menu displaying various management options.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createManageMenu()
	{
		if (manageMenu != null) return manageMenu;
		IconManager icons = IconManager.getInstance();
		manageMenu = new JPopupMenu();
		
		JMenuItem menuItem = new JMenuItem("New Dataset");
		menuItem.setToolTipText("Create a Dataset.");
		menuItem.setIcon(icons.getIcon(IconManager.CREATE));
		menuItem.addActionListener(this);
		menuItem.setActionCommand(""+NEW_OBJECT);
		menuItem.setEnabled(model.canLinkParent());
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
		return manageMenu;
	}
	
	/**
	 * Creates the menu displaying the annotation options.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createSlideViewMenu()
	{
		if (slideViewMenu != null) return slideViewMenu;
		slideViewMenu = new JPopupMenu();
		JMenuItem item = new JMenuItem("View Selected Images");
		item.addActionListener(this);
		item.setActionCommand(""+SLIDE_SHOW_SELECTION);
		slideViewMenu.add(item);
		item = new JMenuItem("View Displayed Images");
		item.addActionListener(this);
		item.setActionCommand(""+SLIDE_SHOW_IMAGES);
		slideViewMenu.add(item);
		return slideViewMenu;
	}
	
	/** Sets the value of the filtering dialog. */
	private void setFilteringValue()
	{
		SearchObject filter = search.getSelectedNode();
		if (filter == null || filter.getIndex()==QuickSearch.NONE) return;
		
		filteringDialog.unselectAll();
		
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
				break;
			case QuickSearch.HAS_ROIS:
    			        filteringDialog.setHasROIs();
    			        break;
			case QuickSearch.NO_ROIS:
			        filteringDialog.setNoROIs();
			        break;
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
			filteringDialog.setTags(view.getExistingTags(), false);
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
		status = new JLabel();
		status.setFont(status.getFont().deriveFont(Font.BOLD));
		IconManager icons = IconManager.getInstance();
		String text = null;
		if (model.getType() == DataBrowserModel.GROUP) text = "users";
		
		search = new QuickFiltering(text);
		search.setDefaultText(text);
		search.addPropertyChangeListener(controller);
		
		filterButton = new JButton(icons.getIcon(IconManager.FILTERING));
		filterButton.setToolTipText("Filter elements displayed in " +
				"the Workspace.");
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
		
		filterByMenuButton = new JButton(icons.getIcon(
				IconManager.FILTER_BY_MENU));
		filterByMenuButton.addMouseListener(new MouseAdapter() {
			
			/**
			 * Brings up the filtering dialog.
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e) {
				createFilterByMenu();
				Point p = e.getPoint();
				filterByMenu.show(filterByMenuButton, p.x, p.y);
			}
		
		});
		UIUtilities.unifiedButtonLookAndFeel(filterByMenuButton);
		
		ButtonGroup group = new ButtonGroup();
		int index = view.getSelectedView();
		thumbView = new JToggleButton(
					icons.getIcon(IconManager.THUMBNAIL_VIEW));
		thumbView.setToolTipText("View as Thumbnails.");
		thumbView.addActionListener(this);
		thumbView.setActionCommand(""+DataBrowserUI.THUMB_VIEW);
		thumbView.setSelected(index == DataBrowserUI.THUMB_VIEW);
		group.add(thumbView);
		columnsView = new JToggleButton(
				icons.getIcon(IconManager.COLUMN_VIEW));
		columnsView.setToolTipText("View as List.");
		columnsView.addActionListener(this);
		columnsView.setActionCommand(""+DataBrowserUI.COLUMNS_VIEW);
		columnsView.setSelected(index == DataBrowserUI.COLUMNS_VIEW);
		group.add(columnsView);
		slideShowView = new JButton(
				icons.getIcon(IconManager.SLIDE_SHOW_VIEW));
		slideShowView.setToolTipText("Show slideshow.");
		UIUtilities.unifiedButtonLookAndFeel(slideShowView);
		slideShowView.addActionListener(this);
		slideShowView.setActionCommand(""+SLIDE_SHOW_IMAGES);
		/*
		slideShowView.addMouseListener(new MouseAdapter() {
			
			public void mouseReleased(MouseEvent e) {
				createSlideViewMenu().show(slideShowView, e.getX(), e.getY());
			}
		
		});
		*/
		//group.add(slideShowView);
		managementButton = new JButton(icons.getIcon(IconManager.MANAGER));
		UIUtilities.unifiedButtonLookAndFeel(managementButton);
		managementButton.setToolTipText("Manage elements.");
		managementButton.addMouseListener(new MouseAdapter() {
			
			/**
			 * Brings up the filtering dialog.
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e) {
				createManageMenu().show(managementButton, e.getX(), e.getY());
			}
		
		});

		group = new ButtonGroup();
		orderByName = new JToggleButton(
				icons.getIcon(IconManager.SORT_BY_NAME));
		orderByName.setToolTipText("Order elements by name.");
		orderByName.addActionListener(this);
		orderByName.setActionCommand(""+DataBrowserUI.SORT_BY_NAME);
		orderByName.setSelected(true);
		group.add(orderByName);
		orderByDate = new JToggleButton(
				icons.getIcon(IconManager.SORT_BY_DATE));
		if (model.getType() != DataBrowserModel.GROUP) {
			orderByDate.setToolTipText("Order images by acquisition date.");
		} else 
			orderByDate.setToolTipText("Order users by creation date.");
		
		orderByDate.addActionListener(this);
		orderByDate.setActionCommand(""+DataBrowserUI.SORT_BY_DATE);
		group.add(orderByDate);
		
		refreshButton = new JButton(controller.getAction(
								DataBrowserControl.REFRESH));
		UIUtilities.unifiedButtonLookAndFeel(refreshButton);
		
		//
		rollOverButton = new JToggleButton();
		rollOverButton.setIcon(icons.getIcon(IconManager.ROLL_OVER));
		rollOverButton.setToolTipText("Turn on/off the magnification " +
				"of a thumbnail while mousing over it.");
		rollOverButton.addActionListener(this);
		rollOverButton.setActionCommand(""+ROLL_OVER);
		
		createDatasetButton = new JButton();
		createDatasetButton.setToolTipText("Create a new dataset containing " +
				"the displayed images.");
		UIUtilities.unifiedButtonLookAndFeel(createDatasetButton);
		createDatasetButton.setIcon(icons.getIcon(IconManager.DATASET));
		createDatasetButton.setEnabled(model.canLinkParent());
		createDatasetButton.addMouseListener(new MouseAdapter() {
			
			/**
			 * Brings up the create menu.
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e) {
				createNewObjectMenu().show(createDatasetButton, e.getX(), 
						e.getY());
			}
		
		});

		itemsPerRow = new JTextField(3);
		itemsPerRow.setToolTipText(ITEMS_PER_ROW_TEXT);
		itemsPerRow.addActionListener(this);
		itemsPerRow.setActionCommand(""+ITEMS_PER_ROW);
		
		saveButton = new JButton(
				controller.getAction(DataBrowserControl.SAVE_AS));
		UIUtilities.unifiedButtonLookAndFeel(saveButton);
		
		reportButton = new JButton();
		reportButton.setToolTipText("Create a report.");
		reportButton.setIcon(icons.getIcon(IconManager.REPORT));
		reportButton.addActionListener(this);
		reportButton.setActionCommand(""+REPORT);
		reportButton.setEnabled(model.canLinkParent());
		UIUtilities.unifiedButtonLookAndFeel(reportButton);
		addPropertyChangeListener(controller);
		codeCompletion = true;
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
		bar.add(refreshButton);
		bar.add(Box.createHorizontalStrut(2));
		bar.add(new JSeparator(JSeparator.VERTICAL));
		bar.add(Box.createHorizontalStrut(2));
		bar.add(orderByName);
		bar.add(orderByDate);
		bar.add(Box.createHorizontalStrut(2));
		bar.add(new JSeparator(JSeparator.VERTICAL));
		bar.add(Box.createHorizontalStrut(2));
		bar.add(rollOverButton);
		if (model.getType() != DataBrowserModel.GROUP) {
			bar.add(reportButton);
			bar.add(saveButton);
			bar.add(new JSeparator(JSeparator.VERTICAL));
		}
		itemsPerRowPane = new JPanel();
		itemsPerRowPane.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		itemsPerRowPane.add(new JLabel("# per row:"));
		itemsPerRowPane.add(itemsPerRow);
		itemsPerRowPane.setToolTipText(itemsPerRow.getToolTipText());
		bar.add(itemsPerRowPane);
		return bar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		JPanel p = new JPanel();
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setBorder(null);
		bar.setRollover(true);
		bar.add(filterButton);
		bar.add(filterByMenuButton);
		p.add(bar);
		p.add(search);
		p.add(buildViewsBar());
		content.add(p);
		JPanel text = new JPanel();
		
		text.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JPanel labelPane = new JPanel();
		labelPane.setLayout(new BoxLayout(labelPane, BoxLayout.X_AXIS));
		labelPane.add(status);
		labelPane.add(Box.createHorizontalStrut(10));
		labelPane.add(filteringLabel);
		text.add(labelPane);
		content.add(text);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(content);
	}
	
	/** Creates a report. */
	private void report()
	{
		List<FileFilter> filterList = new ArrayList<FileFilter>();
		FileFilter filter = new ExcelFilter();
		filterList.add(filter);
		JFrame frame = DataBrowserAgent.getRegistry().getTaskBar().getFrame();
		FileChooser chooser =
			new FileChooser(frame, FileChooser.SAVE, "Create a report", 
					"Create a tag report", filterList);
		IconManager icons = IconManager.getInstance();
		chooser.setTitleIcon(icons.getIcon(IconManager.REPORT_48));
		try {
			File f = UIUtilities.getDefaultFolder();
			if (f != null) chooser.setCurrentDirectory(f);
		} catch (Exception ex) {}
		int option = chooser.centerDialog();
		if (option != JFileChooser.APPROVE_OPTION) return;
		File  file = chooser.getFormattedSelectedFile();
		controller.createReport(file.getAbsolutePath());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model			Reference to the Model. Mustn't be <code>null</code>.
	 * @param view 			Reference to the view. Mustn't be <code>null</code>.
	 * @param controller 	Reference to the control. 
	 * 						Mustn't be <code>null</code>.
	 */
	DataBrowserToolBar(DataBrowserModel model,
			DataBrowserUI view, DataBrowserControl controller)
	{
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		this.controller = controller;
		this.view = view;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Returns the number of items per row.
	 * 
	 * @return See above.
	 */
	int getItemsPerRow()
	{
		int row = -1;
		try {
			row = Integer.parseInt(itemsPerRow.getText());
		} catch(NumberFormatException nfe) {}
		
		if (row < 0) row = 0;//LayoutUtils.DEFAULT_PER_ROW;
		return row;
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
		itemsPerRowPane.setVisible(index == DataBrowserUI.THUMB_VIEW);
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
		search.setTags(tags);
		if (filteringDialog != null && filteringDialog.isVisible()) 
			filteringDialog.setTags(tags, true);
		else {
			if (!codeCompletion) {
				codeCompletion = false;
				showTagsWizard(tags);
			}
		}
	}

	/** Invokes when the parent has been set. */
	void onExperimenterSet()
	{
		createDatasetButton.setEnabled(model.canLinkParent());
	}
	
	/**
	 * Sets the number of images.
	 * 
	 * @param value The number of images displayed.
	 * @param total	The total number of images.
	 */
	void setNumberOfImages(int value, int total)
	{
		String s = "Workspace: "+value+" of "+total;
		if (model.getType() == DataBrowserModel.GROUP) s += " user";
		else s += " image";
		if (total > 1) s += "s";
		status.setText(s);
		status.repaint();
	}
	
	/**
	 * Sets the text of the filtered label.
	 * 
	 * @param value The value to set.
	 */
	void setFilterLabel(String value)
	{
		if (value != null && value.length() > 0)
			filteringLabel.setText(FILTER_BY+value);
		else filteringLabel.setText("");
	}
	
	/**
	 * Sets the filtering status.
	 * 
	 * @param busy  Pass <code>true</code> if filtering, <code>false</code>
	 * 				otherwise.
	 */
	void setFilterStatus(boolean busy)
	{
		search.setFilteringStatus(busy);
	}
	
	/**
	 * Sets the filtering context.
	 * 
	 * @param context The context to handle.
	 */
	void filterByContext(FilterContext context)
	{
		if (context == null) return;
		List<String> terms;
		search.setSearchEnabled(true);
		switch (context.getContext()) {
			case FilterContext.MULTI:
				List<Integer> c = context.getContextList();
				String text = "";
				if (c.contains(FilterContext.RATE)) 
					text += SearchComponent.NAME_RATE;
				if (c.contains(FilterContext.TAG)) {
					if (text.length() != 0) text += ", ";
					text += SearchComponent.NAME_TAGS;
				}
				if (c.contains(FilterContext.COMMENT)) {
					if (text.length() != 0) text += ", ";
					text += SearchComponent.NAME_COMMENTS;
				}
				if (c.contains(FilterContext.NAME)) {
					if (text.length() != 0) text += ", ";
					text += SearchComponent.NAME_TEXT;
				}
				if (c.contains(FilterContext.ROI)) {
				        if (text.length() != 0) text += ", ";
				        text += SearchComponent.NAME_ROIS;
				}
				setFilterLabel(text);
				search.setSearchContext(QuickSearch.NONE);
				break;
			case FilterContext.NONE:
				search.setSearchContext(QuickSearch.SHOW_ALL);
				setFilterLabel("");
				search.setSearchEnabled(false);
				search.setFilteringStatus(false);
				break;
			case FilterContext.RATE:
				search.setSearchEnabled(false);
				setFilterLabel(SearchComponent.NAME_RATE);
				if (context.getRateIndex() != FilterContext.LOWER_EQUAL) {
					switch (context.getRate()) {
						case 0:
							setFilterLabel(SearchComponent.UNRATED);
							search.setSearchContext(QuickSearch.UNRATED);
							break;
						case 1:
							search.setSearchContext(
									QuickSearch.RATED_ONE_OR_BETTER);
							break;
						case 2:
							search.setSearchContext(
									QuickSearch.RATED_TWO_OR_BETTER);
							break;
						case 3:
							search.setSearchContext(
									QuickSearch.RATED_THREE_OR_BETTER);
							break;
						case 4:
							search.setSearchContext(
									QuickSearch.RATED_FOUR_OR_BETTER);
							break;
						case 5:
							search.setSearchContext(QuickSearch.RATED_FIVE);
							break;
					}
				}
			    break;
			case FilterContext.TAG:
				setFilterLabel(SearchComponent.NAME_TAGS);
				search.setSearchContext(QuickSearch.TAGS);
				terms = context.getAnnotation(TagAnnotationData.class);
				if (terms != null) {
					search.setSearchValue(terms, codeCompletion);
					//reset
					codeCompletion = true;
				}
					
			    break;
			case FilterContext.COMMENT:
				setFilterLabel(SearchComponent.NAME_COMMENTS);
				search.setSearchContext(QuickSearch.COMMENTS);
				terms = context.getAnnotation(TextualAnnotationData.class);
				if (terms != null) search.setSearchValue(terms, true);
				break;
			case FilterContext.NAME:
				setFilterLabel(SearchComponent.NAME_TEXT);
				search.setSearchContext(QuickSearch.FULL_TEXT);
				//terms = context.getAnnotation(TextualAnnotationData.class);
				//if (terms != null) search.setSearchValue(terms);
				break;
			case FilterContext.ROI:
			    if(context.getRoiIndex()==FilterContext.GREATER_EQUAL && context.getROIs()==1) {
			        search.setSearchContext(QuickSearch.HAS_ROIS);
			        setFilterLabel(SearchComponent.HAS_ROIS_TEXT);
			    }
			    else if(context.getRoiIndex()==FilterContext.EQUAL && context.getROIs()==0) {
			        search.setSearchContext(QuickSearch.NO_ROIS);
			        setFilterLabel(SearchComponent.NO_ROIS_TEXT);
			    }
			    else {
			        search.setSearchContext(QuickSearch.NONE);
			    }
			    break;
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
				if (index == DataBrowserUI.THUMB_VIEW &&
						!model.hasThumbnailsBeenLoaded())
					model.loadData(false, null);
				break;
			case ROLL_OVER:
				//view.setRollOver(rollOverItem.isSelected());
				view.setRollOver(rollOverButton.isSelected());
				break;
			case NEW_OBJECT:
				Registry reg = DataBrowserAgent.getRegistry();
				DatasetData d = new DatasetData();
				EditorDialog dialog = new EditorDialog(
						reg.getTaskBar().getFrame(), d, false);
				dialog.addPropertyChangeListener(controller);
				UIUtilities.centerAndShow(dialog);
				break;
			case ITEMS_PER_ROW:
				view.setItemsPerRow(getItemsPerRow());
				break;
			case SLIDE_SHOW_IMAGES:
			case SLIDE_SHOW_SELECTION:
				view.slideShowView(true);
				break;	
				//view.slideShowView(true, false);	
			case DataBrowserUI.SORT_BY_NAME:
			case DataBrowserUI.SORT_BY_DATE:
				view.sortBy(index);
				break;
			case REPORT:
				report();
				break;
			case EXISTING_OBJECT:
				controller.loadExistingDatasets();
				break;
			case TAG_FILTER:
				codeCompletion = false;
				if (search.getTags() == null) {
					firePropertyChange(QuickFiltering.TAG_LOADING_PROPERTY, 
							Boolean.valueOf(false), Boolean.valueOf(true));
				} else 
					showTagsWizard(search.getTags());
		}
	}

	/**
	 * Listens to properties this component listened to.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (SelectionWizard.SELECTED_ITEMS_PROPERTY.equals(name)) {
			Map m = (Map) evt.getNewValue();
			if (m == null || m.size() != 1) return;
			Set set = m.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Class type;
			codeCompletion = false;
			search.setSearchContext(QuickFiltering.TAGS);
			while (i.hasNext()) {
				entry = (Entry) i.next();
				type = (Class) entry.getKey();
				search.setSelectedTags((Collection) entry.getValue());
			}
		} else if (SelectionWizard.CANCEL_SELECTION_PROPERTY.equals(name)) {
			codeCompletion = false;
			search.setFilteringStatus(false);
		}
	}
	
}
