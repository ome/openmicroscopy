/*
 * org.openmicroscopy.shoola.agents.iviewer.view.ImViewerUI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.imviewer.view;



//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorModelAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.UnitBarSizeAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ViewerAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.agents.imviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.imviewer.util.CategoryEditor;
import org.openmicroscopy.shoola.agents.imviewer.util.ChannelColorMenuItem;
import org.openmicroscopy.shoola.agents.imviewer.util.HistoryItem;
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;
import org.openmicroscopy.shoola.agents.imviewer.util.SplitPanel;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.ColorCheckBoxMenuItem;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.lens.LensComponent;

/** 
* The {@link ImViewer} view.
* Embeds the {@link Browser}. Also provides a menu bar, a status bar and a 
* panel hosting various controls.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $ $Date: $)
* </small>
* @since OME2.2
*/
class ImViewerUI
 	extends TopWindow
{

	/** Number of pixels added to the height of an icon. */
	private static final int	ICON_EXTRA = 4;

	/** Indicates the percentage of the screen to use to display the viewer. */
	private static final double SCREEN_RATIO = 0.9;

	/** Indicates that only the image is displayed. */
	private static final int	NEUTRAL = 0;

	/** Indicates that the image and the history are displayed. */
	private static final int	HISTORY = 1;

	/** Indicates that the image and the renderer are displayed. */
	private static final int	RENDERER = 2;

	/** Indicates that the image, the history and the renderer are displayed. */
	private static final int	HISTORY_AND_RENDERER = 3;

	/** Identifies the <code>Indigo</code> color. */
	private static final Color INDIGO = new Color(75, 0, 130);

	/** Identifies the <code>Violet</code> color. */
	private static final Color VIOLET = new Color(238, 130, 238);

	/** The available colors for the unit bar. */
	private static Map<Color, String>	colors;

	/** The available colors for the background color of the canvas. */
	private static Map<Color, String>	backgrounds;

	static {
		colors = new LinkedHashMap<Color, String>();
		colors.put(ImagePaintingFactory.UNIT_BAR_COLOR, 
				ImagePaintingFactory.UNIT_BAR_COLOR_NAME);
		colors.put(Color.ORANGE, "Orange");
		colors.put(Color.YELLOW, "Yellow");
		colors.put(Color.BLACK, "Black");
		colors.put(INDIGO, "Indigo");
		colors.put(VIOLET, "Violet");
		colors.put(Color.RED, "Red");
		colors.put(Color.GREEN, "Green");
		colors.put(Color.BLUE, "Blue");
		colors.put(Color.WHITE, "White");

		backgrounds = new LinkedHashMap<Color, String>();
		backgrounds.put(ImagePaintingFactory.DEFAULT_BACKGROUND, 
				ImagePaintingFactory.DEFAULT_BACKGROUND_NAME);
		backgrounds.put(Color.WHITE, "White");
		backgrounds.put(Color.BLACK, "Black");
		backgrounds.put(Color.GRAY, "Grey");
		backgrounds.put(Color.LIGHT_GRAY, "Light Grey");
	}

	/** Reference to the Control. */
	private ImViewerControl 		controller;

	/** Reference to the Model. */
	private ImViewerModel   		model;

	/** The status bar. */
	private StatusBar       		statusBar;

	/** Lens component which will control all behaviour of the lens. */
	private LensComponent			lens;

	/** The tool bar. */
	private ToolBar         		toolBar;

	/** The control pane. */
	private ControlPane     		controlPane;

	/** Group hosting the items of the <code>Rate</code> menu. */
	private ButtonGroup     		ratingGroup;

	/** Group hosting the items of the <code>Zoom</code> menu. */
	private ButtonGroup     		zoomingGroup;

	/** Group hosting the items of the <code>Color Model</code> menu. */
	private ButtonGroup     		colorModelGroup;

	/** The loading window. */
	private LoadingWindow   		loadingWindow;

	/** Tabbed pane hosting the various panel. */
	private JTabbedPane				tabs;

	/** The component displaying the history. */
	private HistoryUI				historyUI;

	/**
	 * Split component used to display the image in the top section and the
	 * history component in the bottom one.
	 */
	private SplitPanel				historySplit;

	/**
	 * Split component used to display the renderer component on the left hand
	 * side of the pane.
	 */
	private JSplitPane				rendererSplit;

	/** 
	 * One out of the following list: 
	 * {@link #NEUTRAL}, {@link #HISTORY}, {@link #RENDERER} and
	 * {@link #HISTORY_AND_RENDERER}.
	 */
	private int						displayMode;

	/** Item used to control show or hide the renderer. */
	private JCheckBoxMenuItem		rndItem;

	/** The dimension of the main component i.e. the tabbed pane. */
	private Dimension				restoreSize;

	/** Listener to the bounds of the container. */
	private HierarchyBoundsAdapter	boundsAdapter;

	/** The height of the icons in the tabbed pane plus 2 pixels. */
	private int						tabbedIconHeight;

	/** The menu displaying the categories the image is categorised into. */
	private CategoriesPopupMenu		categoriesMenu;

	/** The default insets of a split pane. */
	private Insets					refInsets;
	
	/** The number of pixels added between the left and right components. */
	private int						widthAdded;
	
	/** The number of pixels added between the top and bottom components. */
	private int						heightAdded;

	/**
	 * Initializes and returns a split pane, either verical or horizontal 
	 * depending on the passed parameter.
	 * 
	 * @param orientation The orientation of the split pane.
	 * @return See above.
	 */
	private JSplitPane initSplitPane(int orientation)
	{
		int type;
		switch (orientation) {
			case JSplitPane.HORIZONTAL_SPLIT:
			case JSplitPane.VERTICAL_SPLIT:
				type = orientation;
				break;
			default:
				type = JSplitPane.HORIZONTAL_SPLIT;
		}
		JSplitPane pane = new JSplitPane(type);
		//pane.setOneTouchExpandable(true);
		pane.setContinuousLayout(true);
		pane.setResizeWeight(1.0);
		
		return pane;
	}

	/** Initializes the split panes. */
	private void initSplitPanes()
	{
		if (historyUI == null) historyUI = new HistoryUI(this, model);
		historySplit = new SplitPanel(SplitPanel.HORIZONTAL);
		rendererSplit = initSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
	}
    
	/** 
	 * Creates the menu bar.
	 * 
	 * @return The menu bar. 
	 */
	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar(); 
		menuBar.add(createControlsMenu());
		menuBar.add(createViewMenu());
		menuBar.add(createZoomMenu());
		createRatingMenu();
		TaskBar tb = ImViewerAgent.getRegistry().getTaskBar();
		menuBar.add(tb.getWindowsMenu());
		menuBar.add(createHelpMenu());
		return menuBar;
	}

	/**
	 * Helper method to create the background color sub-menu.
	 * 
	 * @return See above.
	 */
	private JMenuItem createBackgroundColorSubMenu()
	{
		JMenu menu = new JMenu("Background color");
		ButtonGroup group = new ButtonGroup();
		Iterator i = backgrounds.keySet().iterator();
		ColorCheckBoxMenuItem item;
		Color c;
		while (i.hasNext()) {
			c = (Color) i.next();
			item = new ColorCheckBoxMenuItem(c);
			item.setText(backgrounds.get(c)); 
			item.setSelected(c.equals(ImagePaintingFactory.DEFAULT_BACKGROUND));
			group.add(item);
			menu.add(item);
			item.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					ColorCheckBoxMenuItem src = 
						(ColorCheckBoxMenuItem) e.getSource();
					if (src.isSelected())
						model.getBrowser().setBackgroundColor(src.getColor());
				}
			});
		}
		return menu;
	}

	/**
	 * Helper method to create the unit bar color sub-menu.
	 * 
	 * @return See above.
	 */
	private JMenuItem createScaleBarColorSubMenu()
	{
		JMenu menu = new JMenu("Scale bar color");
		ButtonGroup group = new ButtonGroup();
		Iterator i = colors.keySet().iterator();
		ColorCheckBoxMenuItem item;
		Color c;
		while (i.hasNext()) {
			c = (Color) i.next();
			item = new ColorCheckBoxMenuItem(c);
			item.setText(colors.get(c)); 
			item.setSelected(c.equals(ImagePaintingFactory.UNIT_BAR_COLOR));
			group.add(item);
			menu.add(item);
			item.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					ColorCheckBoxMenuItem source = 
						(ColorCheckBoxMenuItem) e.getSource();
					if (source.isSelected())
						model.getBrowser().setUnitBarColor(source.getColor());
				}

			});
		}
		return menu;
	}

	/**
	 * Helper method to create the unit bar sub-menu.
	 * 
	 * @return See above.
	 */
	private JMenu createScaleBarLenghtSubMenu()
	{
		JMenu menu = new JMenu("Scale bar length " +
				"(in "+UIUtilities.NANOMETER+")");
		ButtonGroup group = new ButtonGroup();
		UnitBarSizeAction a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_ONE);
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(a);
		item.setSelected(a.isDefaultIndex());
		group.add(item);
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_TWO);
		item = new JCheckBoxMenuItem(a);
		item.setSelected(a.isDefaultIndex());
		group.add(item);
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_FIVE);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_FIVE));
		group.add(item);
		item.setSelected(a.isDefaultIndex());
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_TEN);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_TEN));
		group.add(item);
		item.setSelected(a.isDefaultIndex());
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_TWENTY);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_TWENTY));
		group.add(item);
		item.setSelected(a.isDefaultIndex());
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_FIFTY);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_FIFTY));
		group.add(item);
		item.setSelected(a.isDefaultIndex());
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_HUNDRED);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_HUNDRED));
		group.add(item);
		item.setSelected(a.isDefaultIndex());
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_CUSTOM);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_CUSTOM));
		group.add(item);
		item.setSelected(a.isDefaultIndex());
		menu.add(item);
		return menu;
	}

	/**
	 * Helper method to create the view menu.
	 * 
	 * @return The controls submenu.
	 */
	private JMenu createViewMenu()
	{
		JMenu menu = new JMenu("View");
		menu.setMnemonic(KeyEvent.VK_V);
		JCheckBoxMenuItem item = new JCheckBoxMenuItem();
		item.setSelected(model.isUnitBar());
		item.setAction(controller.getAction(ImViewerControl.UNIT_BAR));
		menu.add(item);
		menu.add(createScaleBarLenghtSubMenu());
		menu.add(createScaleBarColorSubMenu());
		menu.add(new JSeparator(JSeparator.HORIZONTAL));
		menu.add(createBackgroundColorSubMenu());
		menu.add(new JSeparator(JSeparator.HORIZONTAL));
		/*
		JMenuItem historyItem = new JMenuItem();
		if (isHistoryShown()) historyItem.setText(HIDE_HISTORY);
		else historyItem.setText(SHOW_HISTORY);
		historyItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean b = !isHistoryShown();
				JMenuItem item = (JMenuItem) e.getSource();
				if (b) item.setText(HIDE_HISTORY);
				else item.setText(SHOW_HISTORY);
				boolean rnd = isRendererShown();
				if (b) {
					if (rnd) displayMode = HISTORY_AND_RENDERER;
					else displayMode = HISTORY;
				} else {
					if (rnd) displayMode = RENDERER;
					else displayMode = NEUTRAL;
				}
				layoutComponents();
			}
		});
		menu.add(historyItem);
		*/
		return menu;
	}

	/**
	 * Helper method to create the help menu.
	 * 
	 * @return The controls submenu.
	 */
	private JMenu createHelpMenu()
	{
		JMenu menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		TaskBar bar = ImViewerAgent.getRegistry().getTaskBar();
		JMenuItem item = bar.getCopyMenuItem(TaskBar.COMMENT);
		if (item != null) menu.add(item);
		return menu;
	}

	/**
	 * Helper method to create the controls menu.
	 * 
	 * @return The controls submenu.
	 */
	private JMenu createControlsMenu()
	{
		JMenu menu = new JMenu("Controls");
		menu.setMnemonic(KeyEvent.VK_C);
		ViewerAction action = controller.getAction(ImViewerControl.RENDERER);

		rndItem = new JCheckBoxMenuItem();
		rndItem.setSelected(isRendererShown());
		rndItem.setAction(action);
		rndItem.setText(action.getName());
		menu.add(rndItem);

		action = controller.getAction(ImViewerControl.MOVIE);
		JMenuItem item = new JMenuItem(action);
		item.setText(action.getName());
		menu.add(item);
		action = controller.getAction(ImViewerControl.LENS);
		item = new JMenuItem(action);
		item.setText(action.getName());
		menu.add(item);
		//Color model
		colorModelGroup = new ButtonGroup();
		action = controller.getAction(ImViewerControl.GREY_SCALE_MODEL);
		item = new JCheckBoxMenuItem();
		String cm = model.getColorModel();
		item.setSelected(cm.equals(ImViewer.GREY_SCALE_MODEL));
		item.setAction(action);
		colorModelGroup.add(item);
		menu.add(item);
		action = controller.getAction(ImViewerControl.RGB_MODEL);
		item = new JCheckBoxMenuItem();
		item.setAction(action);
		item.setSelected(cm.equals(ImViewer.RGB_MODEL) || 
				cm.equals(ImViewer.HSB_MODEL));
		colorModelGroup.add(item);
		menu.add(item);

		//menu.add(createColorModelMenu());
		menu.add(new JSeparator());
		action = controller.getAction(ImViewerControl.SAVE);
		item = new JMenuItem(action);
		item.setText(action.getName());
		menu.add(item);
		action = controller.getAction(ImViewerControl.DOWNLOAD);
		item = new JMenuItem(action);
		item.setText(action.getName());
		//menu.add(item);
		return menu;
	}

	/**
	 * Helper methods to create the Zoom menu. 
	 * 
	 * @return The zoom submenu;
	 */
	private JMenu createZoomMenu()
	{
		JMenu menu = new JMenu("Zoom");
		menu.setMnemonic(KeyEvent.VK_Z);
		zoomingGroup = new ButtonGroup();
		ViewerAction action = controller.getAction(ImViewerControl.ZOOM_25);
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
		menu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_50);
		item = new JCheckBoxMenuItem(action);
		menu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_75);
		item = new JCheckBoxMenuItem(action);
		menu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_100);
		item = new JCheckBoxMenuItem();
		item.setAction(action); 
		menu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_125);
		item = new JCheckBoxMenuItem(action);
		menu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_150);
		item = new JCheckBoxMenuItem(action);
		menu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_175);
		item = new JCheckBoxMenuItem(action);
		menu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_200);
		item = new JCheckBoxMenuItem(action);
		menu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_225);
		item = new JCheckBoxMenuItem(action);
		menu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_250);
		item = new JCheckBoxMenuItem(action);
		menu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_275);
		item = new JCheckBoxMenuItem(action);
		menu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_300);
		item = new JCheckBoxMenuItem(action);
		menu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_FIT_TO_WINDOW);
		item = new JCheckBoxMenuItem(action);
		menu.add(item);
		zoomingGroup.add(item);
		setZoomFactor(ZoomAction.DEFAULT_ZOOM_FACTOR, 
						ZoomAction.DEFAULT_ZOOM_INDEX);
		return menu;
	}

	/**
	 * Helper methods to create the Rating menu. 
	 * 
	 * @return The rating submenu;
	 */
	private JMenu createRatingMenu()
	{
		JMenu menu = new JMenu("Rating");
		menu.setMnemonic(KeyEvent.VK_R);
		ratingGroup = new ButtonGroup();
		ViewerAction action = controller.getAction(ImViewerControl.RATING_ONE);
		JCheckBoxMenuItem item = new JCheckBoxMenuItem();
		item.setSelected(model.getRatingLevel() == ImViewerModel.RATING_ONE);
		item.setText(action.getName());
		item.setAction(action);
		menu.add(item);
		ratingGroup.add(item);
		action = controller.getAction(ImViewerControl.RATING_TWO);
		item = new JCheckBoxMenuItem();
		item.setSelected(model.getRatingLevel() == ImViewerModel.RATING_TWO);
		item.setAction(action);
		item.setText(action.getName());
		menu.add(item);
		ratingGroup.add(item);
		action = controller.getAction(ImViewerControl.RATING_THREE);
		item = new JCheckBoxMenuItem();
		item.setSelected(model.getRatingLevel() == ImViewerModel.RATING_THREE);
		item.setAction(action);
		item.setText(action.getName());
		menu.add(item);
		ratingGroup.add(item);
		action = controller.getAction(ImViewerControl.RATING_FOUR);
		item = new JCheckBoxMenuItem();
		item.setSelected(model.getRatingLevel() == ImViewerModel.RATING_FOUR);
		item.setAction(action);
		item.setText(action.getName());
		menu.add(item);
		ratingGroup.add(item);
		action = controller.getAction(ImViewerControl.RATING_FIVE);
		item = new JCheckBoxMenuItem();
		item.setSelected(model.getRatingLevel() == ImViewerModel.RATING_FIVE);
		item.setAction(action);
		item.setText(action.getName());
		menu.add(item);
		ratingGroup.add(item);
		return menu;
	}

	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		Browser browser = model.getBrowser();
		browser.setComponentsSize(model.getMaxX(), model.getMaxY());
		tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);

		JPanel p = new JPanel();
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, 
				{TableLayout.FILL, TableLayout.PREFERRED}};
		p.setLayout(new TableLayout(tl));
		p.add(controlPane, "0, 0");
		p.add(browser.getUI(), "1, 0");
		p.add(controlPane.getTimeSliderPane(ImViewer.VIEW_INDEX), "1, 1");
		tabbedIconHeight = browser.getIcon().getIconHeight()+ICON_EXTRA;
		tabs.insertTab(browser.getTitle(), browser.getIcon(), p, "", 
				ImViewer.VIEW_INDEX);
		browser.layoutAnnotator(controlPane.buildAnnotatorComponent(), 
				controlPane.getTimeSliderPane(ImViewer.ANNOTATOR_INDEX));
		tabs.insertTab(browser.getAnnotatorTitle(), browser.getAnnotatorIcon(), 
				browser.getAnnotator(), "", ImViewer.ANNOTATOR_INDEX);

		p = new JPanel();
		p.setLayout(new TableLayout(tl));
		p.add(controlPane.buildGridComponent(), "0, 0");
		p.add(browser.getGridView(), "1, 0");
		p.add(controlPane.getTimeSliderPane(ImViewer.GRID_INDEX), "1, 1");

		tabs.insertTab(browser.getGridViewTitle(), browser.getGridViewIcon(), p, 
				"", ImViewer.GRID_INDEX);
		Container container = getContentPane();
		container.setLayout(new BorderLayout(0, 0));
		container.add(toolBar, BorderLayout.NORTH);
		container.add(tabs, BorderLayout.CENTER);
		container.add(statusBar, BorderLayout.SOUTH);
		tabs.addChangeListener(controller);
		tabs.setEnabledAt(ImViewer.GRID_INDEX, model.getMaxC() != 1);
		//attach listener to the frame border
		boundsAdapter = new HierarchyBoundsAdapter() {

			/**
			 * Stores the size of the tabbed pane when the frame is resized.
			 * @see HierarchyBoundsListener#ancestorResized(HierarchyEvent)
			 */
			public void ancestorResized(HierarchyEvent e) {
				if (tabs != null) restoreSize = tabs.getSize();
			}
		};
		container.addHierarchyBoundsListener(boundsAdapter);
	}

	/**
	 * Returns the size this widget should have to display the image
	 * before adding the split panes.
	 * 
	 * @param w The width of the component added to the center of the 
	 * 			container.
	 * @param h	The height of the component added to the center of the 
	 * 			container.
	 * @return See above.
	 */
	private Dimension getIdealSize(int w, int h)
	{
		Dimension sz = new Dimension();
		Dimension tbDim = toolBar.getPreferredSize();
		Dimension statusDim = statusBar.getPreferredSize();
		Insets frameInsets = getInsets();
		Insets stInsets = statusBar.getInsets();
		sz.width = w+frameInsets.left+frameInsets.right+stInsets.left
					+stInsets.right;
		sz.height = h+tbDim.height+statusDim.height+frameInsets.top
					+frameInsets.bottom+tabbedIconHeight+stInsets.top
					+stInsets.bottom;
		return sz;
	}

	/**
	 * Adds a left and right component to the specified pane.
	 * 
	 * @param pane	The split pane to add the components to.
	 * @param left	The left component to add.
	 * @param right The right component to add.
	 */
	private void addComponents(JSplitPane pane, JComponent left, 
								JComponent right)
	{
		Component c = pane.getLeftComponent();
		if (c != null) pane.remove(c);
		c = pane.getRightComponent();
		if (c != null) pane.remove(c); 
		pane.setLeftComponent(left);
		pane.setRightComponent(right);
	}
	
	/** Lays out the components composing main panel. */
	private void layoutComponents()
	{
		//initSplitPanes();
		Dimension d;
		int diff;
		Container container = getContentPane();
		container.removeHierarchyBoundsListener(boundsAdapter);
		container.removeAll();
		container.add(toolBar, BorderLayout.NORTH);
		container.add(statusBar, BorderLayout.SOUTH);
		int width = 0, height = 0;
		JComponent rightComponent;
		//int divider = 0;
		switch (displayMode) {
			case HISTORY:
				historyUI.doGridLayout();
				height = restoreSize.height;
				width = restoreSize.width;
				d = historyUI.getIdealSize();
				//divider += d.height;
				height += d.height;
				heightAdded = historySplit.getDividerSize()+
								(refInsets.top+refInsets.bottom);
				height += historySplit.getDividerSize()+
							2*(refInsets.top+refInsets.bottom);
				//divider += heightAdded;
				historyUI.setPreferredSize(new Dimension(width, d.height));
				//addComponents(historySplit, tabs, historyUI);
				//if (historyMove == -1) 
				//historyMove = (height-divider);
				//historySplit.setDividerLocation(historyMove);
				
				container.add(historySplit, BorderLayout.CENTER);
				break;
			case RENDERER:
				rightComponent = model.getRenderer().getUI();
				d = rightComponent.getPreferredSize();
				height = restoreSize.height;
				diff = d.height-restoreSize.height;
				if (diff > 0) height += diff;
				//heightAdded = (refInsets.top+refInsets.bottom);
				height += 2*heightAdded;
				width = restoreSize.width+d.width;
				widthAdded = rendererSplit.getDividerSize();
				width += rendererSplit.getDividerSize()+
							2*(refInsets.left+refInsets.right);
				addComponents(rendererSplit, tabs, rightComponent);
				/*
				if (rendererMove != -1 && rendererMove < width)
					rendererSplit.setDividerLocation(rendererMove);
				else {
					rendererMove = -1;
					rendererSplit.setDividerLocation(-1);
				}
				*/
				container.add(rendererSplit, BorderLayout.CENTER);
				break;
			case HISTORY_AND_RENDERER:
				historyUI.doGridLayout();
				rightComponent = model.getRenderer().getUI();
				addComponents(rendererSplit, tabs, rightComponent);
				//addComponents(historySplit, rendererSplit, historyUI);
				historySplit.removeAll();
				historySplit.setLeftComponent(rendererSplit);
				historySplit.setRightComponent(historyUI);
				d = rightComponent.getPreferredSize();
				//height = restoreSize.height;
				height = restoreSize.height;
				diff = d.height-restoreSize.height;
				if (diff > 0) height += diff;
				width = restoreSize.width+d.width;
				//heightAdded = (refInsets.top+refInsets.bottom);
				height += 2*heightAdded;
				widthAdded = rendererSplit.getDividerSize();
				width += rendererSplit.getDividerSize()+
						(refInsets.left+refInsets.right);
				d = historyUI.getIdealSize();
				//divider += d.height;
				height += d.height;
				heightAdded += historySplit.getDividerSize();
				height += historySplit.getDividerSize()+
							(refInsets.top+refInsets.bottom);
				//divider += historySplit.getDividerSize()
				//				+(refInsets.top+refInsets.bottom);
				historyUI.setPreferredSize(new Dimension(width, d.height));
				//if (historyMove == -1 || historyMove < height) 
				//historyMove = (height-divider);
				//historySplit.setDividerLocation(historyMove);
				//historySplit.setResizeWeight(1.0);
				/*
				if (rendererMove != -1 && rendererMove < width)
					rendererSplit.setDividerLocation(rendererMove);
				else {
					rendererMove = -1;
					rendererSplit.setDividerLocation(-1);
				}
				*/
				container.add(historySplit, BorderLayout.CENTER);
				break;
			case NEUTRAL:
			default:
				container.add(tabs, BorderLayout.CENTER);
				width = restoreSize.width-widthAdded;
				height = restoreSize.height-heightAdded;
				widthAdded = 0;
				heightAdded = 0;
				break;
		}
		//setSize(getIdealSize(width, height));
		d = getIdealSize(width, height);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int) (screen.width*SCREEN_RATIO);
		int h = (int) (screen.height*SCREEN_RATIO);
		if (d.width > w || d.height > h) {
			setSize(width, height);
		} else setSize(d);
		container.addHierarchyBoundsListener(boundsAdapter);
	}

	/**
	 * Creates a new instance.
	 * The {@link #initialize(ImViewerControl, ImViewerModel) initialize} 
	 * method should be called straight after to link this View 
	 * to the Controller.
	 * 
	 * @param title The window title.
	 */
	ImViewerUI(String title)
	{
		super(title);
		loadingWindow = new LoadingWindow(this);
		displayMode = NEUTRAL;
	}

	/**
	 * Links this View to its Controller and Model.
	 * 
	 * @param controller    Reference to the Control.
	 *                      Mustn't be <code>null</code>.
	 * @param model         Reference to the Model.
	 *                      Mustn't be <code>null</code>.
	 */
	void initialize(ImViewerControl controller, ImViewerModel model)
	{
		if (controller == null) throw new NullPointerException("No control.");
		if (model == null) throw new NullPointerException("No model.");
		this.controller = controller;
		this.model = model;
		toolBar = new ToolBar(this, controller);
		controlPane = new ControlPane(controller, model, this); 
		statusBar = new StatusBar();
		initSplitPanes();
		refInsets = rendererSplit.getInsets();
		widthAdded = 0;
		heightAdded = 0;
		addComponentListener(controller);
	}

	/** 
	 * This method should be called straight after the metadata and the
	 * rendering settings are loaded.
	 */
	void buildComponents()
	{
		setJMenuBar(createMenuBar());
		toolBar.buildComponent();
		controlPane.buildComponent();
		buildGUI();
	}

	/**
	 * Updates UI components when a zooming factor is selected.
	 * 
	 * @param factor	The magnification factor.
	 * @param zoomIndex The index of the selected zoomFactor.
	 */
	void setZoomFactor(double factor, int zoomIndex)
	{
		statusBar.setRigthStatus("x"+factor);
		JCheckBoxMenuItem b;
		Enumeration e;
		Action a;
		for (e = zoomingGroup.getElements(); e.hasMoreElements();) {
			b = (JCheckBoxMenuItem) e.nextElement();
			a = b.getAction();
			if (a instanceof ZoomAction) {
				b.removeActionListener(a);
				b.setSelected(((ZoomAction) a).getIndex() == zoomIndex);
				b.setAction(a);
			}
		}
		controlPane.setZoomFactor(zoomIndex);
	}

	/**
	 * Updates UI components when a rating factor is selected.
	 * 
	 * @param action    The selected action embedding the rating factor
	 *                  information.
	 */
	void setRatingFactor(ViewerAction action)
	{
		controlPane.setRatingFactor(action);
		AbstractButton b;
		Enumeration e;
		for (e = ratingGroup.getElements(); e.hasMoreElements();) {
			b = (AbstractButton) e.nextElement();
			if ((b.getAction()).equals(action)) {
				b.removeActionListener(action);
				//b.setSelected(true);
				b.setAction(action);
			}
		}
	}

	/**
	 * Updates UI components when a new color model is selected.
	 * 
	 * @param key The index of the color model.
	 */
	void setColorModel(int key)
	{
		controlPane.setColorModel();
		AbstractButton b;
		Action a;
		Enumeration e;
		for (e = colorModelGroup.getElements(); e.hasMoreElements();) {
			b = (AbstractButton) e.nextElement();
			a = b.getAction();
			if (a instanceof ColorModelAction) {
				b.removeActionListener(a);
				b.setSelected(((ColorModelAction) a).getIndex() == key);
				b.setAction(a);
			}
		}
	}

	/**
	 * Updates UI components when a new z-section is selected.
	 * 
	 * @param z The selected z-section.
	 */
	void setZSection(int z) { controlPane.setZSection(z); }

	/**
	 * Updates UI components when a new timepoint is selected.
	 * 
	 * @param t The selected timepoint.
	 */
	void setTimepoint(int t) { controlPane.setTimepoint(t); }

	/**
	 * Returns the {@link #loadingWindow}.
	 * 
	 * @return See above.
	 */
	LoadingWindow getLoadingWindow() { return loadingWindow; }

	/** 
	 * Reacts to {@link ImViewer} change events.
	 * 
	 * @param b Pass <code>true</code> to enable the UI components, 
	 *          <code>false</code> otherwise.
	 */
	void onStateChange(boolean b) { controlPane.onStateChange(b); }

	/**
	 * Updates status bar.
	 * 
	 * @param description   The text to display.
	 */
	void setStatus(String description)
	{
		statusBar.setLeftStatus(description);
	}

	/**
	 * Updates the buttons' selection when a new button is selected or 
	 * deselected.
	 */
	void setChannelsSelection() { controlPane.setChannelsSelection(); }

	/** 
	 * Sets whether or not the tabbed pane is enabled.
	 * 
	 * @param enabled 	Pass <code>true</code> to enable the component, 
	 * 					<code>false</code> otherwise.
	 */
	void playChannelMovie(boolean enabled) { tabs.setEnabled(enabled); }
	
	/** 
	 * Sets the color of the specified channel. 
	 * 
	 * @param index The channel index. 
	 * @param c     The color to set.
	 */
	void setChannelColor(int index, Color c)
	{
		controlPane.setChannelColor(index, c);
	}

	/** Resets the defaults. */
	void resetDefaults() { controlPane.resetDefaults(); }

	/**
	 * Sets the image in the lens to the plane image shown on the screen
	 * depending on the selected tabbed pane.
	 */
	void setLensPlaneImage()
	{
		if (lens == null) return;
		switch (model.getTabbedIndex()) {
		case ImViewer.VIEW_INDEX:
			lens.setPlaneImage(model.getOriginalImage());
			break;
		case ImViewer.GRID_INDEX:
			lens.setPlaneImage(model.getGridImage());
			break;
		case ImViewer.ANNOTATOR_INDEX:
			lens.setPlaneImage(model.getAnnotateImage());
			break;
		}
	}

	/**
	 * Creates a zoomed version of the passed image.
	 * 
	 * @param image The image to zoom.
	 * @return See above.
	 */
	BufferedImage createZoomedLensImage(BufferedImage image)
	{
		if (lens == null) return null;
		return lens.createZoomedImage(image);
	}

	/**
	 * Returns <code>true</code> if the lens is visible, <code>false</code>
	 * otherwise.
	 * 
	 * @return see above.
	 */
	boolean isLensVisible()
	{
		if (lens != null) return lens.isVisible();
		return false;
	}

	/**
	 * Returns <code>true</code> if the lens exists, <code>otherwise</code>.
	 * 
	 * @return See above
	 */
	boolean hasLensImage() { return (lens != null); }

	/**
	 * Sets the lens's visibility. If the lens hasn't previously created, 
	 * we first create the lens.
	 * 
	 * @param b 			Pass <code>true</code> to display the lens, 
	 * 						<code>false</code> otherwise.
	 * @param historyIndex	The index of the tabbed pane. 
	 */
	void setLensVisible(boolean b, int historyIndex)
	{
		boolean firstTime = false;
		if (lens == null) {
			if (b) {
				firstTime = true;
				lens = new LensComponent(this);
				lens.setXYPixelMicron(model.getPixelsSizeX(), 
						model.getPixelsSizeY());
				lens.addPropertyChangeListener(
						LensComponent.LENS_LOCATION_PROPERTY, controller);
			} else return;

		} else {
			Browser browser = model.getBrowser();
			JComponent c = lens.getLensUI();
			browser.removeComponent(c, ImViewer.VIEW_INDEX);
			browser.removeComponent(c, ImViewer.GRID_INDEX);
			browser.removeComponent(c, ImViewer.ANNOTATOR_INDEX);
		}
		if (!b) {
			lens.setVisible(b);
			repaint();
			return;
		}
		//depending on the previous selected tabbed pane, 
		//we reset the location of the lens

		int maxX = model.getMaxX();
		int maxY = model.getMaxY();
		float f = 1.0f;
		BufferedImage img;
		int index = model.getTabbedIndex();
		switch (index) {
		case ImViewer.VIEW_INDEX:
			default:
				f = (float) model.getZoomFactor();
				img = model.getOriginalImage();
				break;
			case ImViewer.GRID_INDEX:
				img = model.getGridImage();
				break;
			case ImViewer.ANNOTATOR_INDEX:
				img = model.getOriginalImage();
				f = (float) model.getBrowser().getRatio();
				break;
		}
		int width = lens.getLensUI().getWidth();
		int height = lens.getLensUI().getHeight();
		Point p = lens.getLensLocation();
		int lensX = p.x;
		int lensY = p.y;
		if (maxX < width || maxY < height) return;
		if (firstTime) {
			//firstTimeLensShown = false;
			int diffX = maxX-width;
			int diffY = maxY-height;
			lensX = diffX/2;
			lensY = diffY/2;
			if (lensX+width > maxX) lensX = diffX;
			if (lensY+height > maxY) lensY = diffY;
		} else {
			switch (historyIndex) {
				case ImViewer.GRID_INDEX:
					if (historyIndex != index) {
						Point point = model.getBrowser().isOnImageInGrid(
								lens.getLensScaledBounds());
						if (point == null) {
							int diffX = maxX-width;
							int diffY = maxY-height;
							lensX = diffX/2;
							lensY = diffY/2;
							if (lensX+width > maxX) lensX = diffX;
							if (lensY+height > maxY) lensY = diffY;
						} else {
							double r = model.getBrowser().getRatio();
							lensX = (int) (point.x/r);
							lensY = (int) (point.y/r);
						}
					}
					break;
				case ImViewer.VIEW_INDEX:
				case ImViewer.ANNOTATOR_INDEX:
					if (index == ImViewer.GRID_INDEX) {
						double r = model.getBrowser().getRatio();
						lensX = (int) (lensX*r);
						lensY = (int) (lensY*r);
					}
			}
		}
		lens.resetLens(img, f, lensX, lensY);  
		model.getBrowser().addComponent(lens.getLensUI(), index);
		scrollLens();
		UIUtilities.setLocationRelativeTo(this, lens.getZoomWindowUI());
		lens.setVisible(b);
		repaint();
	}

	/**
	 * Returns the <code>zoomedImage</code> from the lens component
	 * or <code>null</code> if the lens is <code>null</code>.
	 * 
	 * @return See above.
	 */
	BufferedImage getZoomedLensImage()
	{ 
		if (lens == null) return null;
		return lens.getZoomedImage(); 
	}

	/**
	 * Sets the lens magnification factor.
	 * 
	 * @param factor The value to set.
	 */
	void setImageZoomFactor(float factor)
	{ 
		if (lens == null) return;
		lens.setImageZoomFactor(factor); 
	}

	/** Hides the lens when the window is iconified. */
	void onIconified()
	{
		if (lens == null) return;
		lens.setVisible(false);
		repaint();
	}

	/**
	 * Creates the color picker menu and brings it on screen.
	 * 
	 * @param menuID    The id of the menu. One out of the following constants:
	 *                  {@link ImViewer#COLOR_PICKER_MENU},
	 *                  {@link ImViewer#CATEGORY_MENU}.
	 * @param source	The component that requested the popup menu.
	 * @param location	The point at which to display the menu, relative to the
	 *                  <code>component</code>'s coordinates.
	 */
	void showMenu(int menuID, Component source, Point location)
	{
		switch (menuID) {
			case ImViewer.COLOR_PICKER_MENU:
				ChannelMetadata[] data = model.getChannelData();
				ChannelMetadata d;
				JPopupMenu menu = new JPopupMenu();
				ChannelColorMenuItem item;
				for (int j = 0; j < data.length; j++) {
					d = data[j];
					item = new ChannelColorMenuItem(
							"Wavelength "+d.getEmissionWavelength(), 
							model.getChannelColor(j), j);
					menu.add(item);
					item.addPropertyChangeListener(controller);
				}
				menu.show(source, location.x, location.y);
				break;
	
			case ImViewer.CATEGORY_MENU:
				//if (categoriesMenu == null)
				categoriesMenu = new CategoriesPopupMenu(this, model);
				categoriesMenu.showMenu(source, location.x, location.y);
				break;
		}
	}

	/**
	 * Scrolls to display the lens when the user drags the lens.
	 * 
	 * @param bounds The lens' bounds.
	 */
	void scrollToNode(Rectangle bounds)
	{
		if (lens == null) return;
		if (!lens.isVisible()) return;
		model.getBrowser().scrollTo(bounds, true);
	}

	/** Displays the lens on screen when the image is zoomed. */
	void scrollLens()
	{
		if (lens == null) return;
		model.getBrowser().scrollTo(lens.getLensScaledBounds(), false);
	}

	/**
	 * Selects the tabbed pane specified by the passed index.
	 * 
	 * @param index The index.
	 */
	void selectTabbedPane(int index)
	{
		switch (index) {
			case ImViewer.VIEW_INDEX:
			case ImViewer.ANNOTATOR_INDEX:
			case ImViewer.GRID_INDEX:
				tabs.setSelectedIndex(index);
				break;
			default:
				return;
		}
	}
	
	/** 
	 * Sets the selected pane.
	 * 
	 * @param index The index of the selected tabbed pane.
	 */
	void setSelectedPane(int index)
	{
		int oldIndex = model.getTabbedIndex();
		model.setTabbedIndex(index);
		model.getBrowser().setSelectedPane(index);
		setLensVisible(isLensVisible(), oldIndex);
	}

	/** Centers the image when the user maximized the viewer. */
	void maximizeWindow()
	{
		JComponent c = model.getBrowser().getUI();
		c.setBounds(c.getBounds());
	}

	/**
	 * Sets the <code>enable</code> flag of the slider used to select
	 * the current z-section and timepoint.
	 * 
	 * @param b Pass <code>true</code> to enable the sliders,
	 * 			<code>false</code> otherwise.
	 */
	void enableSliders(boolean b) { controlPane.enableSliders(b); }

	/**
	 * Returns <code>true</code> if the rendering settings are 
	 * saved before closing the viewer, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean saveSettingsOnClose() { return toolBar.saveSettingsOnClose(); }

	/**
	 * Sets the specified channel to active.
	 * 
	 * @param index The channel's index.
	 */
	void setChannelActive(int index)
	{
		controlPane.setChannelActive(index);
	}

	/** 
	 * Adds a new item to the history. 
	 * 
	 * @param node The node to add.
	 */
	void addHistoryItem(HistoryItem node)
	{
		if (!isHistoryShown() || historyUI == null) return;
		historyUI.addHistoryItem(node);
	}

	/**
	 * Removes the item from the list.
	 * 
	 * @param node The node to remove.
	 */
	void removeHistoryItem(HistoryItem node)
	{
		if (node ==  null) return;
		model.removeHistoryItem(node);
		if (historyUI == null) return;
		historyUI.doGridLayout();
	}

	/** Shows or hides the renderer. */
	void showRenderer()
	{
		boolean show = !isRendererShown();
		//boolean b = isHistoryShown();
		if (show) {
			//if (b) displayMode = HISTORY_AND_RENDERER;
			//else displayMode = RENDERER;
			displayMode = HISTORY_AND_RENDERER; 
		} else {
			//if (b) displayMode = HISTORY;
			//else displayMode = NEUTRAL;
			displayMode = NEUTRAL;
			//rendererMove = rendererSplit.getDividerLocation();
		}
		rndItem.setSelected(isRendererShown());
		toolBar.displayRenderer();
		layoutComponents();
	}

	/**
	 * Returns <code>true</code> if the renderer is shown, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean isRendererShown()
	{
		switch (displayMode) {
			case RENDERER:
			case HISTORY_AND_RENDERER:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if the history is shown, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean isHistoryShown()
	{
		switch (displayMode) {
			case HISTORY:
			case HISTORY_AND_RENDERER:
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * Returns the {@link #restoreSize}.
	 * 
	 * @return See above.
	 */
	Dimension geRestoreSize() { return restoreSize; }

	/** 
	 * Declassifies the image from the specified category.
	 * 
	 * @param categoryID The category to handle.
	 */
	void declassify(long categoryID)
	{
		controller.declassify(categoryID);
	}

	/** Creates a new category and adds the image to the category. */
	void createCategory()
	{
		CategoryEditor editor = new CategoryEditor(this, 
				model.getAvailableCategories(), 
				model.getCategories(),
				model.getPopulatedCategoryGroups());
		editor.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(editor);
	}

	/** 
	 * Shows or hides the local history.
	 * 
	 * @param b Pass <code>true</code> to display the history,
	 * 			<code>false</code> otherwise.
	 */
	void showHistory(boolean b)
	{
		boolean rnd = isRendererShown();
		if (b) {
			if (rnd) displayMode = HISTORY_AND_RENDERER;
			else displayMode = HISTORY;
		} else {
			//historyMove = historySplit.getDividerLocation();
			if (rnd) {
				displayMode = RENDERER;
				//rendererMove = rendererSplit.getDividerLocation();
			}
			else displayMode = NEUTRAL;
		}
		layoutComponents();
	}
	
	/** 
	 * Overridden to the set the location of the {@link ImViewer}.
	 * @see TopWindow#setOnScreen() 
	 */
	public void setOnScreen()
	{
		if (model != null) {
			Browser browser = model.getBrowser();
			if (browser != null) {
				Dimension size = browser.getUI().getPreferredSize();
				Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				int width = (int) (screen.width*SCREEN_RATIO);
				int height = (int) (screen.height*SCREEN_RATIO);
				if (size.width > width || size.height > height) {
					setSize(width, height);
				} else pack();
			} else pack();
			UIUtilities.incrementRelativeToAndShow(model.getRequesterBounds(), 
					this);
		} else {
			pack();
			UIUtilities.incrementRelativeToAndShow(null, this);
		}
	}

}
