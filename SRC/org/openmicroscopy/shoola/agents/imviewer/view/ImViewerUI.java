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
import java.awt.Cursor;
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
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
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

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorModelAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.UnitBarSizeAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ViewerAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomGridAction;
import org.openmicroscopy.shoola.agents.imviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.imviewer.util.ChannelColorMenuItem;
import org.openmicroscopy.shoola.agents.imviewer.util.HistoryItem;
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;
import org.openmicroscopy.shoola.agents.imviewer.util.SplitPanel;
import org.openmicroscopy.shoola.agents.imviewer.util.player.MoviePlayerDialog;
import org.openmicroscopy.shoola.agents.util.tagging.CategoryEditor;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.ColorCheckBoxMenuItem;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.lens.LensComponent;
import pojos.ExperimenterData;

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

	/** Indicates to update the channel buttons composing the grid view. */
	static final int 			GRID_ONLY = 0;
	
	/** Indicates to update the channel buttons composing the main view. */
	static final int 			VIEW_ONLY = 1;
	
	/**
	 *  Indicates to update the channel buttons composing the grid view
	 * and the main view. 
	 */
	static final int 			GRID_AND_VIEW = 2;
	
	/** Indicates that only the image is displayed. */
	static final int			NEUTRAL = 0;

	/** Indicates that the image and the history are displayed. */
	static final int			HISTORY = 1;

	/** Indicates that the image and the renderer are displayed. */
	static final int			RENDERER = 2;

	/** Indicates that the image, the history and the renderer are displayed. */
	static final int			HISTORY_AND_RENDERER = 3;

	/** Number of pixels added to the height of an icon. */
	private static final int	ICON_EXTRA = 4;

	/** Indicates the percentage of the screen to use to display the viewer. */
	private static final double SCREEN_RATIO = 0.9;

	/** Identifies the <code>Indigo</code> color. */
	private static final Color  INDIGO = new Color(75, 0, 130);

	/** Identifies the <code>Violet</code> color. */
	private static final Color  VIOLET = new Color(238, 130, 238);

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

	/** The menu displaying the users who viewed the image. */
	private UsersPopupMenu			usersMenu;
	
	/** The default insets of a split pane. */
	private Insets					refInsets;
	
	/** The number of pixels added between the left and right components. */
	private int						widthAdded;
	
	/** The number of pixels added between the top and bottom components. */
	private int						heightAdded;

	/** Group hosting the possible background colors. */
	private ButtonGroup 			bgColorGroup;
	
	/** Group hosting the possible scale bar length. */
	private ButtonGroup 			scaleBarGroup;
	
	/** The source invoking the {@link #usersMenu}. */
	private Component				source;
	
	/** The location where to pop up the {@link #usersMenu}. */
	private Point					location;
	
	/** The zoom menu. */
	private JMenu					zoomMenu;
	
	/** The zoom grid menu. */
	private JMenu					zoomGridMenu;
	
	/** Group hosting the items of the <code>ZoomGrid</code> menu. */
	private ButtonGroup     		zoomingGridGroup;
	
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
	 * @param pref The user preferences.
	 * @return The menu bar. 
	 */
	private JMenuBar createMenuBar(ViewerPreferences pref)
	{
		JMenuBar menuBar = new JMenuBar(); 
		menuBar.add(createControlsMenu(pref));
		menuBar.add(createViewMenu(pref));
		menuBar.add(createZoomMenu(pref));
		createRatingMenu();
		TaskBar tb = ImViewerAgent.getRegistry().getTaskBar();
		menuBar.add(tb.getWindowsMenu());
		menuBar.add(createHelpMenu());
		return menuBar;
	}

	/**
	 * Helper method to create the background color sub-menu.
	 * 
	 * @param pref The user preferences.
	 * @return See above.
	 */
	private JMenuItem createBackgroundColorSubMenu(ViewerPreferences pref)
	{
		JMenu menu = new JMenu("Background color");
		bgColorGroup = new ButtonGroup();
		Iterator i = backgrounds.keySet().iterator();
		ColorCheckBoxMenuItem item;
		Color c;
		Color refColor = ImagePaintingFactory.DEFAULT_BACKGROUND;
		if (pref != null) 
			refColor = pref.getBackgroundColor();
		if (refColor == null) 
			refColor = ImagePaintingFactory.DEFAULT_BACKGROUND;
		while (i.hasNext()) {
			c = (Color) i.next();
			item = new ColorCheckBoxMenuItem(c);
			item.setText(backgrounds.get(c)); 
			item.setSelected(c.equals(refColor));
			bgColorGroup.add(item);
			menu.add(item);
			item.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					ColorCheckBoxMenuItem src = 
						(ColorCheckBoxMenuItem) e.getSource();
					if (src.isSelected()) {
						controller.setPreferences();
						model.getBrowser().setBackgroundColor(src.getColor());
					}
						
				}
			});
		}
		return menu;
	}

	/**
	 * Helper method to create the unit bar color sub-menu.
	 * 
	 * @param pref The user preferences.
	 * @return See above.
	 */
	private JMenuItem createScaleBarColorSubMenu(ViewerPreferences pref)
	{
		JMenu menu = new JMenu("Scale bar color");
		ButtonGroup group = new ButtonGroup();
		Iterator i = colors.keySet().iterator();
		ColorCheckBoxMenuItem item;
		Color c;
		Color refColor = ImagePaintingFactory.UNIT_BAR_COLOR;
		if (pref != null) refColor = pref.getScaleBarColor();
		if (refColor == null)
			refColor = ImagePaintingFactory.UNIT_BAR_COLOR;
		while (i.hasNext()) {
			c = (Color) i.next();
			item = new ColorCheckBoxMenuItem(c);
			item.setText(colors.get(c)); 
			item.setSelected(c.equals(refColor));
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
	 * @param pref The user preferences.
	 * @return See above.
	 */
	private JMenu createScaleBarLengthSubMenu(ViewerPreferences pref)
	{
		JMenu menu = new JMenu("Scale bar length " +
				"(in "+UIUtilities.NANOMETER+")");
		scaleBarGroup = new ButtonGroup();
		int index = UnitBarSizeAction.DEFAULT_UNIT_INDEX;
		if (pref != null && pref.getScaleBarIndex() > 0)
			index = pref.getScaleBarIndex();
		UnitBarSizeAction a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_ONE);
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(a);
		item.setSelected(a.getIndex() == index);
		scaleBarGroup.add(item);
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_TWO);
		item = new JCheckBoxMenuItem(a);
		item.setSelected(a.getIndex() == index);
		scaleBarGroup.add(item);
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_FIVE);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_FIVE));
		scaleBarGroup.add(item);
		item.setSelected(a.getIndex() == index);
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_TEN);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_TEN));
		scaleBarGroup.add(item);
		item.setSelected(a.getIndex() == index);
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_TWENTY);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_TWENTY));
		scaleBarGroup.add(item);
		item.setSelected(a.getIndex() == index);
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_FIFTY);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_FIFTY));
		scaleBarGroup.add(item);
		item.setSelected(a.getIndex() == index);
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_HUNDRED);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_HUNDRED));
		scaleBarGroup.add(item);
		item.setSelected(a.getIndex() == index);
		menu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_CUSTOM);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_CUSTOM));
		scaleBarGroup.add(item);
		item.setSelected(a.getIndex() == index);
		menu.add(item);
		return menu;
	}

	/**
	 * Helper method to create the view menu.
	 * 
	 * @param pref The user preferences.
	 * @return The controls submenu.
	 */
	private JMenu createViewMenu(ViewerPreferences pref)
	{
		JMenu menu = new JMenu("View");
		menu.setMnemonic(KeyEvent.VK_V);
		JCheckBoxMenuItem item = new JCheckBoxMenuItem();
		item.setSelected(model.isUnitBar());
		item.setAction(controller.getAction(ImViewerControl.UNIT_BAR));
		menu.add(item);
		menu.add(createScaleBarLengthSubMenu(pref));
		menu.add(createScaleBarColorSubMenu(pref));
		menu.add(new JSeparator(JSeparator.HORIZONTAL));
		menu.add(createBackgroundColorSubMenu(pref));
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
	 * @param pref The user preferences.
	 * @return The controls submenu.
	 */
	private JMenu createControlsMenu(ViewerPreferences pref)
	{
		JMenu menu = new JMenu("Controls");
		menu.setMnemonic(KeyEvent.VK_C);
		ViewerAction action = controller.getAction(ImViewerControl.RENDERER);

		rndItem = new JCheckBoxMenuItem();
		rndItem.setSelected(isRendererShown());
		rndItem.setAction(action);
		rndItem.setText(action.getName());
		if (pref != null) rndItem.setSelected(pref.isRenderer());
		menu.add(rndItem);

		action = controller.getAction(ImViewerControl.MOVIE);
		JMenuItem item = new JMenuItem(action);
		item.setText(action.getName());
		menu.add(item);
		action = controller.getAction(ImViewerControl.LENS);
		item = new JMenuItem(action);
		item.setText(action.getName());
		menu.add(item);
		action = controller.getAction(ImViewerControl.MEASUREMENT_TOOL);
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
		
		action = controller.getAction(ImViewerControl.PREFERENCES);
		item = new JMenuItem(action);
		item.setText(action.getName());
		menu.add(item);
		return menu;
	}

	/**
	 * Helper methods to create the Zoom menu. 
	 * 
	 * @param pref The user preferences.
	 * @return The zoom submenu;
	 */
	private JMenu createZoomMenu(ViewerPreferences pref)
	{
		zoomMenu = new JMenu("Zoom");
		zoomMenu.setMnemonic(KeyEvent.VK_Z);
		zoomingGroup = new ButtonGroup();
		ViewerAction action = controller.getAction(ImViewerControl.ZOOM_25);
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
		zoomMenu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_50);
		item = new JCheckBoxMenuItem(action);
		zoomMenu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_75);
		item = new JCheckBoxMenuItem(action);
		zoomMenu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_100);
		item = new JCheckBoxMenuItem();
		item.setAction(action); 
		zoomMenu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_125);
		item = new JCheckBoxMenuItem(action);
		zoomMenu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_150);
		item = new JCheckBoxMenuItem(action);
		zoomMenu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_175);
		item = new JCheckBoxMenuItem(action);
		zoomMenu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_200);
		item = new JCheckBoxMenuItem(action);
		zoomMenu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_225);
		item = new JCheckBoxMenuItem(action);
		zoomMenu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_250);
		item = new JCheckBoxMenuItem(action);
		zoomMenu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_275);
		item = new JCheckBoxMenuItem(action);
		zoomMenu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_300);
		item = new JCheckBoxMenuItem(action);
		zoomMenu.add(item);
		zoomingGroup.add(item);
		action = controller.getAction(ImViewerControl.ZOOM_FIT_TO_WINDOW);
		item = new JCheckBoxMenuItem(action);
		zoomMenu.add(item);
		zoomingGroup.add(item);
		int index = ZoomAction.DEFAULT_ZOOM_INDEX;
		double factor = ZoomAction.DEFAULT_ZOOM_FACTOR;
		if (pref != null) {
			if (pref.isFieldSelected(ViewerPreferences.ZOOM_FACTOR)) {
				index = pref.getZoomIndex();
				factor = ZoomAction.getZoomFactor(index);
			}
		}
		setZoomFactor(factor, index);
		//Create zoom grid menu
		zoomGridMenu = new JMenu("Zoom");
		//zoomGridMenu.setMnemonic(KeyEvent.VK_Z);
		zoomingGridGroup = new ButtonGroup();
		
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.ZOOM_GRID_25));
		zoomGridMenu.add(item);
		zoomingGridGroup.add(item);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.ZOOM_GRID_50));
		zoomGridMenu.add(item);
		zoomingGridGroup.add(item);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.ZOOM_GRID_75));
		zoomGridMenu.add(item);
		zoomingGridGroup.add(item);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.ZOOM_GRID_100));
		zoomGridMenu.add(item);
		zoomingGridGroup.add(item);
		setGridMagnificationFactor(ZoomGridAction.DEFAULT_ZOOM_FACTOR);
		return zoomMenu;
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
	
	/** Lays out the components composing main panel. 
	 * 
	 * @param fromPreferences	Pass <code>true</code> to indicate that the 
	 * 							method is invoked while setting the user 
	 * 							preferences, <code>false</code> otherwise.
	 */
	private void layoutComponents(boolean fromPreferences)
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
				historyUI.setPreferredSize(new Dimension(width, d.height));
				container.add(historySplit, BorderLayout.CENTER);
				break;
			case RENDERER:
				rightComponent = model.getRenderer().getUI();
				d = rightComponent.getPreferredSize();
				height = restoreSize.height;
				diff = d.height-restoreSize.height;
				if (diff > 0) height += diff;
				height += 2*heightAdded;
				heightAdded += historySplit.getDividerSize();
				height += historySplit.getDividerSize()+
							(refInsets.top+refInsets.bottom);
				width = restoreSize.width+d.width;
				widthAdded = rendererSplit.getDividerSize();
				width += rendererSplit.getDividerSize()+
							2*(refInsets.left+refInsets.right);
				addComponents(rendererSplit, tabs, rightComponent);
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
		if (!fromPreferences) {
			d = getIdealSize(width, height);
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			int w = (int) (screen.width*SCREEN_RATIO);
			int h = (int) (screen.height*SCREEN_RATIO);
			if (d.width > w || d.height > h) {
				setSize(width, height);
			} else setSize(d);
		}
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
		//Retrieve the preferences.
		ViewerPreferences pref = ImViewerFactory.getPreferences();
		setJMenuBar(createMenuBar(pref));
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
		setMagnificationStatus(factor);
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
	 * Sets the magnification value in the status bar depending on the
	 * selected tabbedPane.
	 * 
	 * @param factor The value to set.
	 */
	void setMagnificationStatus(double factor)
	{
		if (factor != -1)
			statusBar.setRigthStatus("x"+factor);
		else statusBar.setRigthStatus(ZoomAction.ZOOM_FIT_NAME);
	}
	
	/**
	 * Returns the index associated to the zoom factor.
	 * 
	 * @return See above.
	 */
	int getZoomIndex()
	{
		JCheckBoxMenuItem b;
		Enumeration e;
		Action a;
		for (e = zoomingGroup.getElements(); e.hasMoreElements();) {
			b = (JCheckBoxMenuItem) e.nextElement();
			a = b.getAction();
			if (b.isSelected())
				return ((ZoomAction) a).getIndex();
		}
		return -2;
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
	void onStateChange(boolean b)
	{ 
		enableSliders(b);
		controlPane.onStateChange(b); 
	}

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
	 * 
	 * @param index One of the following constants {@link #GRID_ONLY},
	 * 				{@link #VIEW_ONLY} and {@link #GRID_AND_VIEW}.
	 */
	void setChannelsSelection(int index)
	{ 
		controlPane.setChannelsSelection(index); 
	}

	/**
	 * Sets the active channels in the grid view.
	 * 
	 * @param channels The collection of channel indexes.
	 */
	void setChannelsSelection(List channels)
	{
		controlPane.setChannelsSelection(channels); 
	}
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
		JMenuBar menuBar = getJMenuBar();
		Component[] items = menuBar.getComponents();
		Component item;
		int j = -1;
		for (int i = 0; i < items.length; i++) {
			item = items[i];
			if (item == zoomGridMenu || item == zoomMenu)
				j = i;
		}
		if (j != -1) menuBar.remove(j);
		switch (index) {
			case ImViewer.GRID_INDEX:
				if (j != -1) menuBar.add(zoomGridMenu, j);
				setMagnificationStatus(model.getBrowser().getGridRatio());
				break;
			case ImViewer.VIEW_INDEX:
				default:
				if (j != -1) menuBar.add(zoomMenu, j);
				setMagnificationStatus(model.getZoomFactor());
		}
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
	 * @param enable Pass <code>true</code> to enable the sliders,
	 * 			<code>false</code> otherwise.
	 */
	void enableSliders(boolean enable)
	{ 
		if (model.isPlayingMovie()) {
			switch (controller.getMoviePlayer().getMovieIndex()) {
				case MoviePlayerDialog.ACROSS_Z:
					controlPane.enableZSliders(false);
					controlPane.enableTSliders(true);
					break;
				case MoviePlayerDialog.ACROSS_T:
					controlPane.enableZSliders(true);
					controlPane.enableTSliders(false);
					break;
				case MoviePlayerDialog.ACROSS_ZT:
					controlPane.enableSliders(false);
					break;
			}
		} else {
			controlPane.enableSliders(enable);
		}
	}
	
	/**
	 * Sets the specified channel to active.
	 * 
	 * @param index   The channel's index.
	 * @param uiIndex One of the following constants 
     * 				  {@link ImViewerUI#GRID_ONLY} and 
	 * 				  {@link ImViewerUI#GRID_AND_VIEW}.
	 */
	void setChannelActive(int index, int uiIndex)
	{
		controlPane.setChannelActive(index, uiIndex);
	}
	
	/**
	 * Returns the collection of the acive channels in the grid view.
	 * 
	 * @return See above.
	 */
	List getActiveChannelsInGrid()
	{
		return controlPane.getActiveChannelsInGrid();
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
			if (rnd) displayMode = RENDERER;
			else displayMode = NEUTRAL;
		}
		layoutComponents(false);
	}
	
	/** Shows or hides the renderer. 
	 * 
	 * @param fromPreferences	Pass <code>true</code> to indicate that the 
	 * 							method is invoked while setting the user 
	 * 							preferences, <code>false</code> otherwise.
	 */
	void showRenderer(boolean fromPreferences)
	{
		boolean show = !isRendererShown();
		boolean b = isHistoryShown();
		if (show) {
			if (b) displayMode = HISTORY_AND_RENDERER;
			else displayMode = RENDERER;
			//displayMode = HISTORY_AND_RENDERER; 
		} else {
			if (b) displayMode = HISTORY;
			else displayMode = NEUTRAL;
			//displayMode = NEUTRAL;
			//rendererMove = rendererSplit.getDividerLocation();
		}
		rndItem.setSelected(isRendererShown());
		toolBar.displayRenderer();
		layoutComponents(fromPreferences);
	}
	
	/**
	 * Sets the compression flag.
	 * 
	 * @param compressionLevel 	One of the compression constants defined
	 * 							by the model.
	 */
	void setCompressionLevel(int compressionLevel)
	{
		switch (model.getCompressionLevel()) {
			case ToolBar.UNCOMPRESSED:
				model.setCompressionLevel(ImViewerModel.UNCOMPRESSED);
			case ToolBar.MEDIUM:
				model.setCompressionLevel(ImViewerModel.MEDIUM);
			case ToolBar.LOW:
				model.setCompressionLevel(ImViewerModel.LOW);
		}
		model.setCompressionLevel(compressionLevel);
	}
	
	/** 
	 * Returns the compression level.
	 * 
	 * @return See above.
	 */
	int getCompressionLevel() 
	{
		switch (model.getCompressionLevel()) {
			default:
			case ImViewerModel.UNCOMPRESSED:
				return ToolBar.UNCOMPRESSED;
			case ImViewerModel.MEDIUM:
				return ToolBar.MEDIUM;
			case ImViewerModel.LOW:
				return ToolBar.LOW;
		}
	}
	
	/**
	 * Returns <code>true</code> if the image is compressed, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isImageCompressed() { return model.isImageCompressed(); }
	
	/**
	 * Sets the restore dimension.
	 * 
	 * @param width  The width to set.
	 * @param height The height to set.
	 */
	void setRestoreSize(int width, int height)
	{
		restoreSize = new Dimension(width, height);
		//
	}
	
	/**
	 * Returns the color of the image's background.
	 * 
	 * @return See above.
	 */
	Color getBackgroundColor()
	{ 
		ColorCheckBoxMenuItem b;
		Enumeration e;
		for (e = bgColorGroup.getElements(); e.hasMoreElements();) {
			b = (ColorCheckBoxMenuItem) e.nextElement();
			if (b.isSelected())return b.getColor();
		}
		return null;
	}

	/**
	 * Returns the index of the scale bar.
	 * 
	 * @return See above.
	 */
	int getScaleBarIndex()
	{
		JCheckBoxMenuItem item;
		Enumeration e;
		for (e = scaleBarGroup.getElements(); e.hasMoreElements();) {
			item = (JCheckBoxMenuItem) e.nextElement();
			if (item.isSelected())
				 return ((UnitBarSizeAction) item.getAction()).getIndex();
		}
		return -1;
	}
	
	/** Shows the list of users who viewed the image.  */
	void showUsersList()
	{
		if (usersMenu == null) {
			usersMenu = new UsersPopupMenu(this, model);
			usersMenu.addPropertyChangeListener(controller);
		}
		usersMenu.show(source, location.x, location.y);
		source = null;
		location = null;
	}
	
	/**
	 * 
	 * @param experimenter
	 */
	void setUserSettings(ExperimenterData experimenter)
	{
		try {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			model.setUserSettings(experimenter);
			resetDefaults();
			controller.renderXYPlane();
		} catch (Exception e) {
			UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Set User rendering settings", "Could not apply " +
					"the settings set by "+experimenter.getFirstName()+
					" "+experimenter.getLastName());
		}
	}
	
	/**
	 * Sets the location and the source where to pop up the menu.
	 * 
	 * @param source	The source to set.
	 * @param location	The location to set.
	 */
	void setLocationAndSource(Component source, Point location)
	{
		this.source = source;
		this.location = location;
	}
	
	/** 
	 * Sets the magnification for the grid view.
	 * 
	 * @param factor The value to set.
	 */
	void setGridMagnificationFactor(double factor)
	{
		setMagnificationStatus(factor);
		JCheckBoxMenuItem b;
		Enumeration e;
		Action a;
		int zoomIndex = ZoomGridAction.getIndex(factor);
		for (e = zoomingGridGroup.getElements(); e.hasMoreElements();) {
			b = (JCheckBoxMenuItem) e.nextElement();
			a = b.getAction();
			if (a instanceof ZoomGridAction) {
				b.removeActionListener(a);
				b.setSelected(((ZoomGridAction) a).getIndex() == zoomIndex);
				b.setAction(a);
			}
		}
		controlPane.setGridMagnificationFactor((int) (factor*10));
	}
	
	/** 
	 * Overridden to the set the location of the {@link ImViewer}.
	 * @see TopWindow#setOnScreen() 
	 */
	public void setOnScreen()
	{
		if (model != null) {
			Browser browser = model.getBrowser();
			Rectangle r = null;
			if (browser != null) {
				JComponent comp = browser.getUI();
				Dimension size = comp.getPreferredSize();
				int w = size.width;
				int h = size.height;
				//Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				//int width = (int) (screen.width*SCREEN_RATIO);
				//int height = (int) (screen.height*SCREEN_RATIO);
				ViewerPreferences pref = ImViewerFactory.getPreferences();
				if (pref != null) {
					r = pref.getViewerBounds();
					w = r.width;
					h = r.height;
					if (w <= 0) w = size.width;
					if (h <= 0) h = size.height;
				}
				if (pref != null) {
					setBounds(r.x, r.y, w, h);
					setVisible(true);
				} else pack();
			} else pack();
			UIUtilities.incrementRelativeToAndShow(
							model.getRequesterBounds(), this);
		} else {
			pack();
			UIUtilities.incrementRelativeToAndShow(null, this);
		}
	}

}
