/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.imviewer.view;


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
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import info.clearthought.layout.TableLayout;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

import ome.model.units.BigResult;
import omero.model.PlaneInfo;
import omero.model.Length;
import omero.model.LengthI;

import org.openmicroscopy.shoola.agents.imviewer.IconManager;
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
import org.openmicroscopy.shoola.agents.imviewer.util.PlaneInfoComponent;
import org.openmicroscopy.shoola.agents.imviewer.util.player.MoviePlayerDialog;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.rnd.data.ResolutionLevel;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.file.modulo.ModuloInfo;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPane;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPaneComponent;
import org.openmicroscopy.shoola.util.ui.ColorCheckBoxMenuItem;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.lens.LensComponent;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;

import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;

/** 
 * The {@link ImViewer} view.
 * Embeds the {@link Browser}. Also provides a menu bar, a status bar and a 
 * panel hosting various controls.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
class ImViewerUI
 	extends TopWindow
{

	/** Indicates to update the channel buttons composing the grid view. */
	static final int 			GRID_ONLY = 0;
	
	/** Indicates to update the channel buttons composing the main view. */
	static final int 			VIEW_ONLY = 1;
	
	/** Indicates to update the channel buttons composing the main view. */
	static final int 			PROJECTION_ONLY = 2;
	
	/**
	 * Indicates to update the channel buttons composing the grid view,
	 * the projection view and the main view. 
	 */
	static final int 			ALL_VIEW = 3;
	
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

	/** The available colors for the background color of the canvas. */
	private static Map<Color, String>	backgrounds;

	static {
		backgrounds = new LinkedHashMap<Color, String>();
		backgrounds.put(ImagePaintingFactory.DEFAULT_BACKGROUND, 
				ImagePaintingFactory.DEFAULT_BACKGROUND_NAME);
		backgrounds.put(Color.WHITE, "White");
		backgrounds.put(Color.BLACK, "Black");
		backgrounds.put(Color.GRAY, "Grey");
		backgrounds.put(Color.LIGHT_GRAY, "Light Grey");
	}

	private static final String SCALE_BAR_TEXT = "Scale bar length (in ";
	/** Reference to the Control. */
	private ImViewerControl 					controller;

	/** Two decimal places number format */
	private static final NumberFormat NUMBERFORMAT = new DecimalFormat("#.##");
	
	/** Reference to the Model. */
	private ImViewerModel   					model;

	/** The status bar. */
	private StatusBar       					statusBar;

	/** Lens component which will control all behavior of the lens. */
	private LensComponent						lens;

	/** The tool bar. */
	private ToolBar         					toolBar;

	/** The control pane. */
	private ControlPane     					controlPane;
	
	/** Group hosting the items of the <code>Zoom</code> menu. */
	private ButtonGroup     					zoomingGroup;

	/** Group hosting the items of the <code>Color Model</code> menu. */
	private ButtonGroup     					colorModelGroup;

	/** The loading window. */
	private LoadingWindow   					loadingWindow;

	/** Tab pane hosting the various panel. */
	private ClosableTabbedPane					tabs;

	/** The component displaying the history. */
	private HistoryUI							historyUI;

	/**
	 * Split component used to display the image in the top section and the
	 * history component in the bottom one.
	 */
	private JSplitPane							historySplit;
	
	/**
	 * Split component used to display the renderer component on the left hand
	 * side of the pane.
	 */
	private JSplitPane							rendererSplit;

	/** 
	 * One out of the following list: 
	 * {@link #NEUTRAL}, {@link #HISTORY}, {@link #RENDERER} and
	 * {@link #HISTORY_AND_RENDERER}.
	 */
	private int									displayMode;
 
	/** Item used to control show or hide the renderer. */
	private JCheckBoxMenuItem					rndItem;
	
	/** Item used to control show or hide the metadata. */
	private JCheckBoxMenuItem					metadataItem;
	
	/** Item used to control show or hide the history. */
	private JCheckBoxMenuItem					historyItem;

	/** The dimension of the main component i.e. the tab pane. */
	private Dimension							restoreSize;

	/** Listener to the bounds of the container. */
	private HierarchyBoundsAdapter				boundsAdapter;

	/** The height of the icons in the tab pane plus 2 pixels. */
	private int									tabbedIconHeight;

	/** The menu displaying the users who viewed the image. */
	private UsersPopupMenu						usersMenu;
	
	/** The default insets of a split pane. */
	private Insets								refInsets;

	/** Group hosting the possible background colors. */
	private ButtonGroup 						bgColorGroup;
	
	/** Group hosting the possible scale bar length. */
	private ButtonGroup 						scaleBarGroup;
	
	/** The source invoking the {@link #usersMenu}. */
	private Component							source;
	
	/** The location where to pop up the {@link #usersMenu}. */
	private Point								location;
	
	/** The zoom menu. */
	private JMenu								zoomMenu;
	
	/** The zoom grid menu. */
	private JMenu								zoomGridMenu;
	
	/** Group hosting the items of the <code>ZoomGrid</code> menu. */
	private ButtonGroup     					zoomingGridGroup;
	
	/** The panel hosting the view. */
	private ClosableTabbedPaneComponent			viewPanel;
	
	/** The panel hosting the view. */
	private ClosableTabbedPaneComponent			gridViewPanel;

	/** The panel hosting the view. */
	private ClosableTabbedPaneComponent			projectionViewPanel;
	
	/** The object displaying the plane information, one per channel. */
	private Map<Integer, PlaneInfoComponent>	planes;

	/** The central component. */
	private JComponent							mainComponent;

	/** The default index of the scale bar. */
	private int									defaultIndex;
	
	/** The viewer as a component. */
	private JComponent							component;
	
	/** The dialog displaying info about the image.*/
	private TinyDialog							infoDialog;
	
	/** The dialog displaying info about the image.*/
	private TinyDialog							channelDialog;
	
	/** The magnification factor for the big image.*/
	private double								bigImageMagnification;
	
	/** Item used to show or hide the unit bar. */
	private JCheckBoxMenuItem unitBarItem;
	
	/** Item used to show or hide the unit bar. */
	private JMenu scaleBarMenu;
	
	/**
	 * Initializes and returns a split pane, either vertical or horizontal 
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
		pane.setOneTouchExpandable(true);
		pane.setContinuousLayout(true);
		pane.setDividerLocation(-1);
		pane.setResizeWeight(0.0);
		return pane;
	}

	/** Initializes the split panes. */
	private void initSplitPanes()
	{
		if (historyUI == null) 
			historyUI = new HistoryUI(this, model, controller);
		//historySplit = new SplitPanel(SplitPanel.HORIZONTAL);
		historySplit = initSplitPane(JSplitPane.VERTICAL_SPLIT);
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
		zoomMenu = new JMenu("Zoom");
		zoomMenu.setMnemonic(KeyEvent.VK_Z);
		zoomingGroup = new ButtonGroup();
		//Create zoom grid menu
		zoomGridMenu = new JMenu("Zoom");
		zoomingGridGroup = new ButtonGroup();
		
		JMenuBar menuBar = new JMenuBar(); 
		menuBar.add(createControlsMenu(pref));
		menuBar.add(createViewMenu(pref));
		if (!model.isBigImage())
			menuBar.add(createZoomMenu(pref, true));
		menuBar.add(createShowViewMenu());
		TaskBar tb = ImViewerAgent.getRegistry().getTaskBar();
		//menuBar.add(tb.getWindowsMenu());
		menuBar.add(tb.getMenu(TaskBar.WINDOW_MENU));
		menuBar.add(tb.getMenu(TaskBar.HELP_MENU));
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
		Iterator<Entry<Color, String>> i = backgrounds.entrySet().iterator();
		ColorCheckBoxMenuItem item;
		Color c;
		Color refColor = ImagePaintingFactory.DEFAULT_BACKGROUND;
		if (pref != null) 
			refColor = pref.getBackgroundColor();
		if (refColor == null) 
			refColor = ImagePaintingFactory.DEFAULT_BACKGROUND;
		Entry<Color, String> entry;
		while (i.hasNext()) {
			entry = i.next();
			c = entry.getKey();
			item = new ColorCheckBoxMenuItem(c);
			item.setText(entry.getValue()); 
			item.setSelected(c.equals(refColor));
			bgColorGroup.add(item);
			menu.add(item);
			item.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					ColorCheckBoxMenuItem src = 
						(ColorCheckBoxMenuItem) e.getSource();
					if (src.isSelected()) {
						controller.setPreferences();
						if (lens != null) 
							lens.setBackgroundColor(src.getColor());
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
		JMenu menu = new JMenu("Scale bar/Text color");
		ButtonGroup group = new ButtonGroup();
		Iterator<Entry<Color, String>> i = EditorUtil.COLORS_BAR.entrySet().iterator();
		ColorCheckBoxMenuItem item;
		Color c;
		Color refColor = ImagePaintingFactory.UNIT_BAR_COLOR;
		if (pref != null) refColor = pref.getScaleBarColor();
		if (refColor == null)
			refColor = ImagePaintingFactory.UNIT_BAR_COLOR;
		Entry<Color, String> entry;
		while (i.hasNext()) {
			entry = i.next();
			c = entry.getKey();
			item = new ColorCheckBoxMenuItem(c);
			item.setText(entry.getValue()); 
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
		scaleBarMenu = new JMenu(SCALE_BAR_TEXT+model.getUnits()+")");
		scaleBarGroup = new ButtonGroup();
		if (pref != null && pref.getScaleBarIndex() > 0)
			defaultIndex = pref.getScaleBarIndex();
		UnitBarSizeAction a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_ONE);
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(a);
		item.setSelected(a.getIndex() == defaultIndex);
		scaleBarGroup.add(item);
		scaleBarMenu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_TWO);
		item = new JCheckBoxMenuItem(a);
		item.setSelected(a.getIndex() == defaultIndex);
		scaleBarGroup.add(item);
		scaleBarMenu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_FIVE);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_FIVE));
		scaleBarGroup.add(item);
		item.setSelected(a.getIndex() == defaultIndex);
		scaleBarMenu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_TEN);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_TEN));
		scaleBarGroup.add(item);
		item.setSelected(a.getIndex() == defaultIndex);
		scaleBarMenu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_TWENTY);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_TWENTY));
		scaleBarGroup.add(item);
		item.setSelected(a.getIndex() == defaultIndex);
		scaleBarMenu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_FIFTY);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_FIFTY));
		scaleBarGroup.add(item);
		item.setSelected(a.getIndex() == defaultIndex);
		scaleBarMenu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_HUNDRED);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_HUNDRED));
		scaleBarGroup.add(item);
		item.setSelected(a.getIndex() == defaultIndex);
		scaleBarMenu.add(item);
		a = (UnitBarSizeAction) 
		controller.getAction(ImViewerControl.UNIT_BAR_CUSTOM);
		item = new JCheckBoxMenuItem(
				controller.getAction(ImViewerControl.UNIT_BAR_CUSTOM));
		scaleBarGroup.add(item);
		item.setSelected(a.getIndex() == defaultIndex);
		scaleBarMenu.add(item);
		return scaleBarMenu;
	}

	/**
	 * Helper method to create the view menu.
	 * 
	 * @param pref The user preferences.
	 * @return The controls sub-menu.
	 */
	private JMenu createViewMenu(ViewerPreferences pref)
	{
		JMenu menu = new JMenu("Display");
		menu.setMnemonic(KeyEvent.VK_V);
		unitBarItem = new JCheckBoxMenuItem();
		unitBarItem.setSelected(model.isUnitBar());
		unitBarItem.setAction(controller.getAction(ImViewerControl.UNIT_BAR));
		menu.add(unitBarItem);
		menu.add(createScaleBarLengthSubMenu(pref));
		menu.add(createScaleBarColorSubMenu(pref));
		menu.add(new JSeparator(JSeparator.HORIZONTAL));
		menu.add(createBackgroundColorSubMenu(pref));
		//menu.add(new JSeparator(JSeparator.HORIZONTAL));
		return menu;
	}

	/** Synchronizes the unit bar selection. */
	void handleUnitBar()
	{
		unitBarItem.removeActionListener(
				controller.getAction(ImViewerControl.UNIT_BAR));
		unitBarItem.setSelected(model.isUnitBar());
		unitBarItem.setAction(controller.getAction(ImViewerControl.UNIT_BAR));
		scaleBarMenu.setText(SCALE_BAR_TEXT+model.getUnits()+")");
	}
	
	/**
	 * Helper method to create the controls menu.
	 * 
	 * @param pref The user preferences.
	 * @return The controls sub-menu.
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
		//menu.add(rndItem);
		
		action = controller.getAction(ImViewerControl.METADATA);
		metadataItem = new JCheckBoxMenuItem();
		metadataItem.setSelected(isRendererShown());
		metadataItem.setAction(action);
		metadataItem.setText(action.getName());
		if (pref != null) metadataItem.setSelected(pref.isRenderer());
		//menu.add(metadataItem);
		
		action = controller.getAction(ImViewerControl.HISTORY);
		historyItem = new JCheckBoxMenuItem();
		historyItem.setSelected(isHistoryShown());
		historyItem.setAction(action);
		historyItem.setText(action.getName());
		if (pref != null) historyItem.setSelected(pref.isHistory());
		//menu.add(historyItem);
		
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
		menu.add(new JSeparator(JSeparator.HORIZONTAL));
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
		item.setSelected(cm.equals(ImViewer.RGB_MODEL));
		colorModelGroup.add(item);
		menu.add(item);
		
		menu.add(new JSeparator());
		action = controller.getAction(ImViewerControl.CHANNELS_ON);
		item = new JMenuItem(action);
		item.setText(action.getName());
		menu.add(item);
		action = controller.getAction(ImViewerControl.CHANNELS_OFF);
		item = new JMenuItem(action);
		item.setText(action.getName());
		menu.add(item);
		
		menu.add(new JSeparator());
		action = controller.getAction(ImViewerControl.SAVE);
		item = new JMenuItem(action);
		item.setText(action.getName());
		menu.add(item);
		action = controller.getAction(ImViewerControl.PREFERENCES);
		item = new JMenuItem(action);
		item.setText(action.getName());
		//menu.add(item);
		return menu;
	}

	/**
	 * Helper method to create the Zoom menu. 
	 * 
	 * @param pref  The user preferences.
	 * @param full  Pass <code>true</code> to create a full menu, 
	 * 				<code>false</code> to create a partial menu.
	 * @return See above.
	 */
	private JMenu createZoomMenu(ViewerPreferences pref, boolean full)
	{
		ViewerAction action;
		JCheckBoxMenuItem item;
		if (model.isBigImage()) {
			action = controller.getAction(ImViewerControl.ZOOM_100);
			item = new JCheckBoxMenuItem();
			item.setAction(action); 
			zoomMenu.add(item);
			action = controller.getAction(ImViewerControl.ZOOM_125);
			item = new JCheckBoxMenuItem();
			item.setAction(action); 
			zoomMenu.add(item);
			action = controller.getAction(ImViewerControl.ZOOM_150);
			item = new JCheckBoxMenuItem();
			item.setAction(action); 
			zoomMenu.add(item);
			action = controller.getAction(ImViewerControl.ZOOM_175);
			item = new JCheckBoxMenuItem();
			item.setAction(action); 
			zoomMenu.add(item);
			return zoomMenu;
		}
		action = controller.getAction(ImViewerControl.ZOOM_25);
		item = new JCheckBoxMenuItem(action);
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
		//if (full) {
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
		//}
		
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
	 * Helper method to create the show View menu. 
	 * 
	 * @return See above.
	 */
	private JMenu createShowViewMenu()
	{
		JMenu menu = new JMenu("View");
		menu.setMnemonic(KeyEvent.VK_S);
		JMenuItem item = new JMenuItem(
			controller.getAction(ImViewerControl.TAB_GRID));
		if (model.isBigImage() || (model.isLifetimeImage() &&
		        model.getModuloT() == null))
			item.setEnabled(false);
		else item.setEnabled(model.getMaxC() > 1);
		menu.add(item);
		item = new JMenuItem(
				controller.getAction(ImViewerControl.TAB_PROJECTION));
		item.setEnabled(model.getMaxZ() > 1 && !model.isBigImage());
		menu.add(item);
		return menu;
	}

	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		Browser browser = model.getBrowser();
		int sizeX = model.getTiledImageSizeX();
		int sizeY = model.getTiledImageSizeY();

		browser.setComponentsSize(sizeX, sizeY);
		tabs = new ClosableTabbedPane(JTabbedPane.TOP, 
									JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);

		viewPanel = new ClosableTabbedPaneComponent(ImViewer.VIEW_INDEX,
							browser.getTitle(), browser.getIcon(), "");
		viewPanel.setClosable(false);
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL},
				{TableLayout.FILL, TableLayout.PREFERRED, 
			TableLayout.PREFERRED}};
		viewPanel.setLayout(new TableLayout(tl));
		viewPanel.add(controlPane, "0, 0");
		viewPanel.add(browser.getUI(), "1, 0");
		viewPanel.add(controlPane.getTimeSliderPane(ImViewer.VIEW_INDEX),
						"1, 1");
		if (model.isLifetimeImage()) {
			viewPanel.add(controlPane.getLifetimeSliderPane(ImViewer.VIEW_INDEX),
			"1, 2");
		}
		tabbedIconHeight = browser.getIcon().getIconHeight()+ICON_EXTRA;
		
		tabs.insertTab(browser.getTitle(), browser.getIcon(), viewPanel, "",
				ImViewer.VIEW_INDEX);
		gridViewPanel = new ClosableTabbedPaneComponent(ImViewer.GRID_INDEX,
				browser.getGridViewTitle(),  browser.getGridViewIcon(), "");
		gridViewPanel.setLayout(new TableLayout(tl));

		gridViewPanel.add(controlPane.buildGridComponent(), "0, 0");
		gridViewPanel.add(browser.getGridView(), "1, 0");
		gridViewPanel.add(controlPane.getTimeSliderPane(ImViewer.GRID_INDEX),
						"1, 1");
		if (model.isLifetimeImage()) {
		    gridViewPanel.add(controlPane.getLifetimeSliderPane(ImViewer.GRID_INDEX),
		            "1, 2");
		}
		if (model.allowSplitView() && !model.isBigImage()) {
			tabs.insertTab(browser.getGridViewTitle(), 
					browser.getGridViewIcon(), gridViewPanel, "",
					ImViewer.GRID_INDEX);
		}
		
		double[][] tl2 = {{TableLayout.PREFERRED, TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.FILL, 
			TableLayout.PREFERRED}};
		
		projectionViewPanel = new ClosableTabbedPaneComponent(
				ImViewer.PROJECTION_INDEX, browser.getProjectionViewTitle(),
				browser.getProjectionViewIcon(), "");
		
		projectionViewPanel.setLayout(new TableLayout(tl2));
		projectionViewPanel.add(controlPane.buildProjectionToolBar(),
				"0, 0, 1, 0");
		projectionViewPanel.add(controlPane.buildProjectionComponent(), "0, 1");
		projectionViewPanel.add(browser.getProjectionView(), "1, 1");
		projectionViewPanel.add(
				controlPane.getTimeSliderPane(ImViewer.PROJECTION_INDEX),
						"1, 2");
		if (model.getMaxZ() > 0 && !model.isBigImage()) {
			tabs.insertTab(browser.getProjectionViewTitle(),
					browser.getProjectionViewIcon(),
					projectionViewPanel, "", ImViewer.PROJECTION_INDEX);
		}
		
		tabs.addChangeListener(controller);
		
		//mainComponent = tabs;
		rendererSplit.setLeftComponent(tabs);
		mainComponent = rendererSplit;

		Container container = getContentPane();
		container.setLayout(new BorderLayout(0, 0));
		container.add(toolBar, BorderLayout.NORTH);
		container.add(mainComponent, BorderLayout.CENTER);
		container.add(statusBar, BorderLayout.SOUTH);
			
		//attach listener to the frame border
		boundsAdapter = new HierarchyBoundsAdapter() {

			/**
			 * Stores the size of the tab pane when the frame is resized.
			 * @see HierarchyBoundsListener#ancestorResized(HierarchyEvent)
			 */
			public void ancestorResized(HierarchyEvent e) {
				if (tabs != null) restoreSize = tabs.getSize();
			}
		};
		container.addHierarchyBoundsListener(boundsAdapter);
		//restoreSize = new Dimension(0, 0);
		//layoutComponents(false);
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
					+stInsets.right+rendererSplit.getDividerSize();
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
	
	/** 
	 * Lays out the components composing main panel. 
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
		int width = 0, height = 0;
		JComponent rightComponent;
		//int divider = 0;
		int vExtra = 2;
		int addition;
		switch (displayMode) {
			case RENDERER:
				rightComponent = model.getMetadataViewer().getEditorUI();
				rendererSplit.setRightComponent(rightComponent);
				if (restoreSize == null) {
					rendererSplit.setResizeWeight(1.0);
					return;
				}
				d = model.getMetadataViewer().getIdealRendererSize();
				rightComponent.setMinimumSize(d);
				tabs.setMinimumSize(restoreSize);
				height = restoreSize.height;
				diff = d.height-restoreSize.height;
				if (diff > 0) height += diff;
				else height += vExtra;
				addition = rendererSplit.getDividerSize()+
							2*(refInsets.left+refInsets.right);
				
				width = restoreSize.width+d.width;
				width += 4*addition;
				break;
			case HISTORY:
				container.remove(mainComponent);
				historyUI.doGridLayout();
				addComponents(historySplit, tabs, historyUI);
				mainComponent = historySplit;
				container.add(mainComponent, BorderLayout.CENTER);
				container.validate();
				container.repaint();
				height = restoreSize.height;
				width = restoreSize.width;
				d = historyUI.getIdealSize();
				addition = historySplit.getDividerSize()+
					2*(refInsets.top+refInsets.bottom);
				height += d.height;
				historySplit.setResizeWeight(0.49);
				height += addition;
				break;
			case HISTORY_AND_RENDERER:
				historySplit.setResizeWeight(0.49);
				container.remove(mainComponent);
				historyUI.doGridLayout();
				rightComponent = model.getMetadataViewer().getEditorUI();
				addComponents(rendererSplit, tabs, rightComponent);
				addComponents(historySplit, rendererSplit, historyUI);
				mainComponent = historySplit;
				container.add(mainComponent, BorderLayout.CENTER);
				container.validate();
				container.repaint();
				
				d = model.getMetadataViewer().getIdealRendererSize();
				height = restoreSize.height;
				diff = d.height-restoreSize.height;
				if (diff > 0) height += diff;
				else height += vExtra;
				addition = rendererSplit.getDividerSize()+
							2*(refInsets.left+refInsets.right);
				
				width = restoreSize.width+d.width;
				width += 4*addition;
				d = historyUI.getPreferredSize();
				addition = historySplit.getDividerSize()+
					2*(refInsets.top+refInsets.bottom);
				height += d.height;
				height += addition;
				break;
			case NEUTRAL:
				rightComponent = model.getMetadataViewer().getEditorUI();
				rendererSplit.remove(rightComponent);
				if (restoreSize == null) return;
				width = restoreSize.width;
				height = restoreSize.height;
				break;
			default: 
		}
		//rendererSplit.setDividerLocation(-1);
		//rendererSplit.setResizeWeight(1.0);
		//historySplit.setDividerLocation(-1);
		d = getIdealSize(width, height);
		
		/* Need to review that code.
		 * Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int) (screen.width*SCREEN_RATIO);
		int h = (int) (screen.height*SCREEN_RATIO);
		if (d.width > w || d.height > h) {
			setSize(width, height);
		} else setSize(d);
		*/
		setSize(d);
		setPreferredSize(d);
		pack();
		container.addHierarchyBoundsListener(boundsAdapter);
	}
	
	/** Packs the window and resizes it if the screen is too small. */
	private void packWindow()
	{
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension size = getSize();
		int width = (int) (screenSize.width*SCREEN_RATIO);
		int height = (int) (screenSize.height*SCREEN_RATIO);
		int w = size.width;
		int h = size.height;
		boolean reset = false;
		if (w > width) {
			reset = true;
			w = width;
		}
		if (h > height) {
			reset = true;
			h = height;
		} 
		if (reset) {
			setSize(w, h);
		}
	}
	
	/**
	 * Removes all the elements from the passed menu and button group.
	 * 
	 * @param group The group to handle.
	 * @param menu  The menu to handle.
	 */
	private void clearZoomMenu(ButtonGroup group, JMenu menu)
	{
		menu.removeAll();
		for (Enumeration<AbstractButton> e = group.getElements(); e.hasMoreElements();)
			group.remove(e.nextElement()) ;
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
		loadingWindow.setTitle("Opening Image Viewer...");
		loadingWindow.setModal(false);
		defaultIndex = UnitBarSizeAction.DEFAULT_UNIT_INDEX;
		displayMode = NEUTRAL;
		bigImageMagnification = 1.0;
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
		statusBar = new StatusBar(model, this);
		initSplitPanes();
		refInsets = rendererSplit.getInsets();
		planes = new HashMap<Integer, PlaneInfoComponent>();
		ImageIcon icon = IconManager.getInstance().getImageIcon(
				IconManager.VIEWER);
		if (icon != null) setIconImage(icon.getImage());
		setName("image viewer window");
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
		List<ChannelData> data = model.getChannelData();
		ChannelData d;
		int index;
		PlaneInfoComponent comp;
		Iterator<ChannelData> i = data.iterator();
		while (i.hasNext()) {
			d = i.next();
			index = d.getIndex();
			comp = new PlaneInfoComponent(model.getChannelColor(index));
			comp.addPropertyChangeListener(
					PlaneInfoComponent.PLANE_INFO_PROPERTY, controller);
			planes.put(index, comp);
		}
	}

	/**
	 * Updates UI components when a zooming factor is selected.
	 * 
	 * @param factor	The magnification factor.
	 * @param zoomIndex The index of the selected zoomFactor.
	 */
	void setZoomFactor(double factor, int zoomIndex)
	{
		setMagnificationStatus(factor, zoomIndex);
		JCheckBoxMenuItem b;
		Enumeration e;
		Action a;
		if (zoomingGroup == null) return;
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
	 * Set the magnification status (basically refreshes the status bar)  
	 */
	void setMagnificationStatus() {
        int index = ZoomAction.getIndex(model.getZoomFactor());
        if (model.isBigImage())
            index = model.getSelectedResolutionLevel();
        setMagnificationStatus(model.getZoomFactor(), index);
	}
	
	/**
	 * Sets the magnification value in the status bar depending on the
	 * selected tabbedPane.
	 * 
	 * @param factor The value to set.
	 * @param zoomIndex The index of the selected zoomFactor.
	 */
	void setMagnificationStatus(double factor, int zoomIndex)
	{
		if (statusBar == null) return;
		if (factor != ZoomAction.ZOOM_FIT_FACTOR)
			statusBar.setRightStatus(
					Math.round(factor*model.getOriginalRatio()*100)+"%");
		else statusBar.setRightStatus(ZoomAction.ZOOM_FIT_NAME);
		if (model.isBigImage()) {
            ResolutionLevel level = model.getResolutionDescription();
            double mag = model.getNominalMagnification();
            bigImageMagnification = level.getRatio();
            if (mag > 0) {
                statusBar.setRightStatus(NUMBERFORMAT.format(level.getRatio()
                        * mag)
                        + "x");
            } else {
                double f = UIUtilities.roundTwoDecimals(level.getRatio() * 100);
                statusBar.setRightStatus(f + "%");
            }
		}
	}
	
	/**
	 * Returns the magnification factor for big images.
	 * 
	 * @return See above.
	 */
	double getBigImageMagnificationFactor()
	{
		if (!model.isBigImage()) return 1.0;
		return bigImageMagnification;
	}
	
	/**
	 * Returns the index associated to the zoom factor.
	 * 
	 * @return See above.
	 */
	int getZoomIndex()
	{
		if (zoomingGroup == null) return ZoomAction.ZOOM_FIT_TO_WINDOW;
		JCheckBoxMenuItem b;
		Enumeration e;
		Action a;
		for (e = zoomingGroup.getElements(); e.hasMoreElements();) {
			b = (JCheckBoxMenuItem) e.nextElement();
			a = b.getAction();
			if (b.isSelected())
				return ((ZoomAction) a).getIndex();
		}
		return ZoomAction.ZOOM_FIT_TO_WINDOW;
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
		Enumeration<AbstractButton> e;
		for (e = colorModelGroup.getElements(); e.hasMoreElements();) {
			b = e.nextElement();
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
	 * Updates UI components when a new bin is selected.
	 * 
	 * @param bin The selected bin.
	 */
	void setBin(int t) { controlPane.setBin(t); }

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
		model.getBrowser().onStateChange(b);
		if (tabs != null) {
			tabs.setEnabled(b);
			enableSliders(b);
			controlPane.onStateChange(b); 
			toolBar.onStateChange(b); 
		}
	}
	
	/** Sets the default text of the status bar. */
	void setLeftStatus()
	{
		int n;
		int max = model.getMaxZ();
		double d = model.getPixelsSizeZ();
		String units;
		Length o;
		StringBuffer buffer = new StringBuffer();
		if (model.getTabbedIndex() == ImViewer.PROJECTION_INDEX) {
			n = getProjectionStartZ();
			int m = getProjectionEndZ();
			buffer.append("Z range:"+(n+1));
			buffer.append("-"+(getProjectionEndZ()+1));
			if (d > 0 && max > 0) {
				o = UIUtilities.transformSize(n*d);
				buffer.append(" ("+UIUtilities.roundTwoDecimals(o.getValue()));
				buffer.append("-");
				o = UIUtilities.transformSize(m*d);
				units = ((LengthI)o).getSymbol();
				buffer.append(UIUtilities.roundTwoDecimals(o.getValue()));
				buffer.append(units+")");
			}
			buffer.append("/"+(model.getMaxZ()+1));
			controlPane.setRangeSliderToolTip(n, m);
		} else {
			n = model.getDefaultZ();
			buffer.append("Z="+(n+1));
			if (d > 0 && max > 0) {
				o = UIUtilities.transformSize(n*d);
				units = ((LengthI)o).getSymbol();
				buffer.append(" ("+UIUtilities.roundTwoDecimals(o.getValue()));
				buffer.append(units+")");
			}
				
			buffer.append("/"+(model.getMaxZ()+1));
		}
		buffer.append(" T="+(
		        model.getRealSelectedT()+1)+"/"+model.getRealT());
		if (model.isLifetimeImage()) {
		    int bin = model.getSelectedBin();
		    buffer.append(" ");
		    buffer.append(EditorUtil.SMALL_T_VARIABLE+"="+(bin+1));
		    buffer.append("/"+(model.getMaxLifetimeBin()));
		    //format the result
		    ModuloInfo info = model.getModuloT();
		    if (info != null) {
	            buffer.append(" (");
	            buffer.append(UIUtilities.roundTwoDecimals(
	                    info.getRealValue(bin)));
	            if (CommonsLangUtils.isNotBlank(info.getUnit())) {
	                buffer.append(info.getUnit());
	            }
	            buffer.append(")");
		    }
		}
		setLeftStatus(buffer.toString());
	}
	
	/**
	 * Updates status bar.
	 * 
	 * @param description   The text to display.
	 */
	void setLeftStatus(String description)
	{
		statusBar.setLeftStatus(description);
	}

	/**
	 * Displays the plane information.
	 * 
	 */
	void setPlaneInfoStatus()
	{
	    if (model.getTabbedIndex() == ImViewer.PROJECTION_INDEX) {
	        statusBar.setCenterStatus(new JLabel());
	        return;
	    }
	    List<Integer> indexes = model.getActiveChannels();
	    if (CollectionUtils.isEmpty(indexes)) {
	        statusBar.setCenterStatus(new JLabel());
	        return;
	    }

	    int z = model.getDefaultZ();
	    int t = model.getRealSelectedT();
	    PlaneInfo info;
	    String s, toolTipText;
	    Map<Integer, Color> colors = model.getChannelsColorMap();
	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	    Map<String, Object> details;
	    List<String> tips;
	    PlaneInfoComponent comp;

	    List<ChannelData> metadata = model.getChannelData();
	    Iterator<ChannelData> c = metadata.iterator();
	    int index;
	    List<String> notSet;
	    while (c.hasNext()) {
	        index = c.next().getIndex();
	        s = "";
	        toolTipText = "";
	        tips = new ArrayList<String>();
	        info = model.getPlane(z, index, t);
	        comp = planes.get(index);
	        if (info != null) {
	            details = EditorUtil.transformPlaneInfo(info);
	            notSet = (List<String>) details.get(EditorUtil.NOT_SET);
	            comp.setColor(colors.get(index));
	            if (!notSet.contains(EditorUtil.DELTA_T)) {
	                if(details.get(EditorUtil.DELTA_T) instanceof BigResult) {
	                    ImViewerAgent.logBigResultExeption(this, details.get(EditorUtil.DELTA_T), EditorUtil.DELTA_T);
	                    s += "N/A";
	                } else {
    	                s += EditorUtil.formatTimeInSeconds(
    	                        (Double) details.get(EditorUtil.DELTA_T));
	                }
	            }
	            if (!notSet.contains(EditorUtil.EXPOSURE_TIME)) {
	                toolTipText += EditorUtil.EXPOSURE_TIME+": ";
	                if(details.get(EditorUtil.EXPOSURE_TIME) instanceof BigResult) {
	                    ImViewerAgent.logBigResultExeption(this, details.get(EditorUtil.EXPOSURE_TIME), EditorUtil.EXPOSURE_TIME);
	                    toolTipText += "N/A";
	                } else {
    	                toolTipText += details.get(EditorUtil.EXPOSURE_TIME);
    	                toolTipText += EditorUtil.TIME_UNIT;
	                }
	                tips.add(toolTipText);
	            }
	            toolTipText = "";
	            toolTipText += "Stage coordinates: ";
                if (!notSet.contains(EditorUtil.POSITION_X)) {
                    if (details.get(EditorUtil.POSITION_X) instanceof BigResult) {
                        toolTipText += "x=N/A ";
                        ImViewerAgent.logBigResultExeption(this,
                                details.get(EditorUtil.POSITION_X),
                                EditorUtil.POSITION_X);
                    } else {
                        toolTipText += "x="
                                + details.get(EditorUtil.POSITION_X) + " ";
                    }
                }
                if (!notSet.contains(EditorUtil.POSITION_Y)) {
                    if (details.get(EditorUtil.POSITION_Y) instanceof BigResult) {
                        toolTipText += "y=N/A ";
                        ImViewerAgent.logBigResultExeption(this,
                                details.get(EditorUtil.POSITION_Y),
                                EditorUtil.POSITION_Y);
                    } else {
                        toolTipText += "y="
                                + details.get(EditorUtil.POSITION_Y) + " ";
                    }
                }
                if (!notSet.contains(EditorUtil.POSITION_Z)) {
                    if (details.get(EditorUtil.POSITION_Z) instanceof BigResult) {
                        toolTipText += "z=N/A ";
                        ImViewerAgent.logBigResultExeption(this,
                                details.get(EditorUtil.POSITION_Z),
                                EditorUtil.POSITION_Z);
                    } else {
                        toolTipText += "z="
                                + details.get(EditorUtil.POSITION_Z);
                    }
                }
	            tips.add(toolTipText);
	            comp.setToolTipText(UIUtilities.formatToolTipText(tips));
	            if (CommonsLangUtils.isEmpty(s)) s = "0s";
	            comp.setText(s);
	            panel.add(comp);
	        }
	    }
	    statusBar.setCenterStatus(panel);
	}
	
	/**
	 * Updates the buttons' selection when a new button is selected or 
	 * deselected.
	 * 
	 * @param index One of the following constants {@link #GRID_ONLY},
	 * 				{@link #VIEW_ONLY}, {@link #PROJECTION_ONLY} 
	 * 				and {@link #ALL_VIEW}.
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
		PlaneInfoComponent comp = planes.get(index);
		if (comp != null) comp.setColor(c);
		controlPane.setChannelColor(index, c);
	}

	/** Resets the defaults. */
	void resetDefaults() { controlPane.resetRndSettings(); }

	/** Resets the UI when switching to a new rendering control. */
	void switchRndControl() { controlPane.switchRndControl(); }
	
	/** Refreshes the view. */
	void refresh()
	{
		resetDefaults();
		model.refresh();
	}
	
	/**
	 * Sets the image in the lens to the plane image shown on the screen
	 * depending on the selected tab pane.
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
			case ImViewer.PROJECTION_INDEX:
				BufferedImage image = model.getProjectedImage();
				if (image != null) lens.setPlaneImage(image);
		}
	}

	/**
	 * Creates a zoomed version of the passed image.
	 * 
	 * @param image The image to zoom.
	 * @return See above.
	 * @throws Exception 
	 */
	BufferedImage createZoomedLensImage(BufferedImage image) 
	{
		if (lens == null) return null;
		try
		{
			return lens.createZoomedImage(image);
		}
		catch(Exception e)
		{
			return null;
		}
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
	 * @param historyIndex	The index of the tab pane. 
	 */
	void setLensVisible(boolean b, int historyIndex)
	{
		boolean firstTime = false;
		if (lens == null) {
			if (b) {
				firstTime = true;
				lens = new LensComponent(this);
				lens.setImageName(model.getImageName());
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
			browser.removeComponent(c, ImViewer.PROJECTION_INDEX);
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
		//BufferedImage img;
		int index = model.getTabbedIndex();
		
		JComponent c = lens.getLensUI();
		int width = c.getWidth();
		int height = c.getHeight();
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
				case ImViewer.PROJECTION_INDEX:
				case ImViewer.VIEW_INDEX:
					if (index == ImViewer.GRID_INDEX) {
						double r = model.getBrowser().getRatio();
						lensX = (int) (lensX*r);
						lensY = (int) (lensY*r);
					}
			}
		}
		BufferedImage img;
        switch (index) {
            case ImViewer.VIEW_INDEX:
            default:
                f = (float) model.getZoomFactor();
                img = model.getOriginalImage();
                break;
            case ImViewer.PROJECTION_INDEX:
                f = (float) model.getZoomFactor();
                img = model.getProjectedImage();
                break;
            case ImViewer.GRID_INDEX:
                img = model.getGridImage();
        }
        if (img != null) lens.resetLens(img, f, lensX, lensY);
		model.getBrowser().addComponent(c, index, true);
		scrollLens();
		UIUtilities.setLocationRelativeTo(this, lens.getZoomWindow());
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
	 * Creates the menus corresponding to the passed id and brings it on screen.
	 * 
	 * @param menuID    The id of the menu. One out of the following constants:
	 *                  {@link ImViewer#COLOR_PICKER_MENU},
	 *                  {@link ImViewer#CATEGORY_MENU}.
	 */
	void showMenu(int menuID)
	{
		showMenu(menuID, source, location);
	}
	
	/**
	 * Creates the menus corresponding to the passed id and brings it on screen.
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
				List<ChannelData> data = model.getChannelData();
				ChannelData d;
				JPopupMenu menu = new JPopupMenu();
				ChannelColorMenuItem item;
				Iterator<ChannelData> i = data.iterator();
				int index;
				while (i.hasNext()) {
					d = i.next();
					index = d.getIndex();
					item = new ChannelColorMenuItem(
							d.getChannelLabeling(), 
							model.getChannelColor(index), index);
					menu.add(item);
					item.addPropertyChangeListener(controller);
				}
				menu.show(source, location.x, location.y);
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
	 * Selects the tab pane specified by the passed index.
	 * 
	 * @param index The index.
	 */
	void selectTabbedPane(int index)
	{
		switch (index) {
			case ImViewer.VIEW_INDEX:
			case ImViewer.GRID_INDEX:
			case ImViewer.PROJECTION_INDEX:
				tabs.setSelectedIndex(index);
				break;
			default:
				return;
		}
	}
	
	/** 
	 * Sets the selected pane.
	 * 
	 * @param index The index of the selected tab pane.
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
		double f;
		switch (index) {
			case ImViewer.GRID_INDEX:
				if (j != -1) menuBar.add(zoomGridMenu, j);
				f = model.getBrowser().getGridRatio();
				setMagnificationStatus(f, ZoomAction.getIndex(f));
				break;
			case ImViewer.PROJECTION_INDEX:
			case ImViewer.VIEW_INDEX:
				default:
				if (j != -1) menuBar.add(zoomMenu, j);
				f = model.getZoomFactor();
				setMagnificationStatus(f, ZoomAction.getIndex(f));
		}
		int oldIndex = model.getTabbedIndex();
		model.setTabbedIndex(index);
		tabs.removeChangeListener(controller);
		int n = tabs.getTabCount();
		Component c;
		int tabbedIndex;
		for (int i = 0; i < n; i++) {
			c = tabs.getComponentAt(i);
			if (c instanceof ClosableTabbedPaneComponent) {
				tabbedIndex = ((ClosableTabbedPaneComponent) c).getIndex();
				if (tabbedIndex == index) 
					tabs.setSelectedIndex(i);
			}
		}
		
		tabs.addChangeListener(controller);
		setLeftStatus();
		setPlaneInfoStatus();
		model.getBrowser().setSelectedPane(index);
		setLensVisible(isLensVisible(), oldIndex);
		maximizeWindow();
	}
	
	/**
	 * Returns the color model of the pane currently selected.
	 * 
	 * @return See above.
	 */
	String getSelectedPaneColorModel()
	{
		return controlPane.getSelectedPaneColorModel();
	}
	
	/** Centers the image when the user maximized the viewer. */
	void maximizeWindow()
	{
		JComponent c = model.getBrowser().getUI();
		c.setBounds(c.getBounds());
	}

	/** Invokes when the component is resized.*/
	void onComponentResized()
	{
		model.getBrowser().onComponentResized();
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
	 * 				  {@link ImViewerUI#ALL_VIEW}.
	 */
	void setChannelActive(int index, int uiIndex)
	{
		controlPane.setChannelActive(index, uiIndex);
	}
	
	/**
	 * Returns the collection of the active channels in the grid view.
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
		historyItem.setSelected(isHistoryShown());
		toolBar.displayHistory();
		layoutComponents(false);
	}
	
	/** 
	 * Shows or hides the renderer. 
	 * 
	 * @param fromPreferences	Pass <code>true</code> to indicate that the 
	 * 							method is invoked while setting the user 
	 * 							preferences, <code>false</code> otherwise.
	 * @param index			    The index of the tab to select.
	 */
	void showRenderer(boolean fromPreferences, int index)
	{
		boolean show = !isRendererShown();
		boolean b = isHistoryShown();
		if (show) {
			if (b) displayMode = HISTORY_AND_RENDERER;
			else displayMode = RENDERER;
		} else {
			if (b) displayMode = HISTORY;
			else displayMode = NEUTRAL;
		}
		metadataItem.setSelected(isRendererShown());
		rndItem.setSelected(isRendererShown());
		toolBar.displayRenderer();
		layoutComponents(fromPreferences);
		if (show) {
			int v = MetadataViewer.RENDERER_TAB;
			if (index == ImViewer.METADATA_INDEX)
				v = MetadataViewer.GENERAL_TAB;
			model.getMetadataViewer().setSelectedTab(v);
		}
	}
	
	/**
	 * Sets the compression flag.
	 * 
	 * @param compressionLevel 	One of the compression constants defined
	 * 							by the model.
	 */
	void setCompressionLevel(int compressionLevel)
	{
		int oldCompression = convertCompressionLevel();
		switch (compressionLevel) {
			case ToolBar.UNCOMPRESSED:
				model.setCompressionLevel(ImViewer.UNCOMPRESSED);
				if (lens != null) lens.resetDataBuffered();
				break;
			case ToolBar.MEDIUM:
				if (lens != null && oldCompression == ToolBar.UNCOMPRESSED) 
					lens.resetDataBuffered();
				model.setCompressionLevel(ImViewer.MEDIUM);
				break;
			case ToolBar.LOW:
				if (lens != null && oldCompression == ToolBar.UNCOMPRESSED) 
					lens.resetDataBuffered();
				model.setCompressionLevel(ImViewer.LOW);
		}
	}
	
	/** 
	 * Returns the UI index corresponding to the current compression level.
	 * 
	 * @return See above.
	 */
	int convertCompressionLevel() 
	{
		return convertCompressionLevel(model.getCompressionLevel());
	}
	
	/** 
	 * Returns the UI index corresponding to the current compression level.
	 * 
	 * @param level The value to check.
	 * @return See above.
	 */
	int convertCompressionLevel(int level) 
	{
		switch (level) {
			default:
			case ImViewer.UNCOMPRESSED:
				return ToolBar.UNCOMPRESSED;
			case ImViewer.MEDIUM:
				return ToolBar.MEDIUM;
			case ImViewer.LOW:
				return ToolBar.LOW;
		}
	}
	 
	
	/**
	 * Returns the UI index of the currently selected compression level.
	 * 
	 * @return See above.
	 */
	int getUICompressionLevel() { return toolBar.getUICompressionLevel(); }
	
	
	/**
	 * Returns <code>true</code> if the image is compressed, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isImageCompressed() { return model.isImageCompressed(); }
	
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
		if (scaleBarGroup == null) return -1;
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
			usersMenu = new UsersPopupMenu(model);
			usersMenu.addPropertyChangeListener(controller);
		}
		usersMenu.show(source, location.x, location.y);
		source = null;
		location = null;
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
		if (model.getTabbedIndex() == ImViewer.GRID_INDEX)
			setMagnificationStatus(factor, ZoomAction.getIndex(factor));
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
     * Adds the component specified by the passed index to the display
     * 
     * @param index The index identifying the UI component.
     */
    void showView(int index)
    {
		switch (index) {
			case ImViewer.VIEW_INDEX:
				tabs.insertClosableComponent(viewPanel);
				break;
			case ImViewer.GRID_INDEX:
				tabs.insertClosableComponent(gridViewPanel);
				break;
			case ImViewer.PROJECTION_INDEX:
				tabs.insertClosableComponent(projectionViewPanel);
				break;
		}
		tabs.validate();
		tabs.repaint();
		restoreSize = tabs.getSize();
	}

    /**
     * Returns the id of the currently selected pixels.
     * 
     * @return See above.
     */
    long getPixelsID() { return model.getPixelsID(); }
    
	/**
	 * Returns <code>true</code> if there is some rendering settings to save,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
    boolean hasSettingsToPaste() { return model.hasRndToPaste(); }
    
	/**
	 * Sets the {@link #pasteButton} enable.
	 * 
	 * @param b Pass <code>true</code> to enable the button, <code>false</code>
	 * 			otherwise.
	 */
	void enablePasteButton(boolean b) { toolBar.enablePasteButton(b); }
	
	/** 
	 * Shows the plane information.
	 * 
	 * @param show 	Pass <code>true</code> to show the dialog, 
	 * 				<code>false</code> to hide it.
	 * @param comp The component to show.
	 */
	void showPlaneInfoDetails(PlaneInfoComponent comp)
	{
		if (comp == null) return;
		if (channelDialog != null) {
			JComponent c = channelDialog.getCanvas();
			channelDialog.closeWindow();
			hideAnimation();
			channelDialog = null;
			if (c == comp.getContent()) return;
		}
		if (infoDialog != null) {
			infoDialog.closeWindow();
			hideAnimation();
			infoDialog = null;
		}
		channelDialog = new TinyDialog(this, comp.getContent(), 
						TinyDialog.CLOSE_ONLY);
		channelDialog.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				if (TinyDialog.CLOSED_PROPERTY.equals(evt.getPropertyName())) {
					channelDialog.closeWindow();
					hideAnimation();
					channelDialog = null;
				}
			}
		});
		channelDialog.pack();
		Point p;
		JPanel glass = (JPanel) getGlassPane();
		if (glass.getLayout() == null) {
			Dimension d = channelDialog.getPreferredSize();
			d = new Dimension(d.width+20, d.height);
			channelDialog.setSize(d);
			channelDialog.setPreferredSize(d);
			p = new Point(0, statusBar.getPreferredSize().height);
		} else {
			p = new Point(0, 2*statusBar.getPreferredSize().height);
		}
		//setCloseAfter(true);
		showJDialogAsSheet(channelDialog, p, UP_MIDDLE);
	}
	
	/** 
	 * Displays information about the image.
	 * 
	 * @param comp
	 */
	void showImageInfo(JComponent comp)
	{
		if (comp == null) return;
		if (infoDialog != null) {
			infoDialog.closeWindow();
			hideAnimation();
			infoDialog = null;
			return;
		}
		if (channelDialog != null) {
			channelDialog.closeWindow();
			hideAnimation();
			channelDialog = null;
		}
		infoDialog = new TinyDialog(this, comp, 
						TinyDialog.CLOSE_ONLY);
		infoDialog.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				if (TinyDialog.CLOSED_PROPERTY.equals(evt.getPropertyName())) {
					infoDialog.closeWindow();
					hideAnimation();
					infoDialog = null;
				}
			}
		});
		infoDialog.pack();
		Point p;
		JPanel glass = (JPanel) getGlassPane();
		if (glass.getLayout() == null) {
			Dimension d = infoDialog.getPreferredSize();
			d = new Dimension(d.width, d.height+20);
			infoDialog.setSize(d);
			infoDialog.setPreferredSize(d);
			p = new Point(0, statusBar.getPreferredSize().height);
		} else {
			p = new Point(0, 2*statusBar.getPreferredSize().height);
			
		}
		//setCloseAfter(true);
		showJDialogAsSheet(infoDialog, p, UP_LEFT);
	}

	/**
	 * Returns the maximum number of z-sections.
	 * 
	 * @return See above.
	 */
	int getMaxZ() { return model.getMaxZ(); }
	
	/**
	 * Returns the index of the selected tab.
	 * 
	 * @return See above.
	 */
	int getTabbedIndex() { return model.getTabbedIndex(); }
	
	/**
	 * Returns the lower bound of the z-section to project.
	 * 
	 * @return See above.
	 */
	int getProjectionStartZ() { return controlPane.getProjectionStartZ(); }
	
	/**
	 * Returns the lower bound of the z-section to project.
	 * 
	 * @return See above.
	 */
	int getProjectionEndZ() { return controlPane.getProjectionEndZ(); }
	
	/**
	 * Returns the stepping used for the projection.
	 * 
	 * @return See above.
	 */
	int getProjectionStepping() { return controlPane.getProjectionStepping(); }
	
	/**
	 * Returns the type of projection.
	 * 
	 * @return See above.
	 */
	int getProjectionType() { return controlPane.getProjectionType(); }
	
	/**
	 * Returns a textual version of the type of projection.
	 * 
	 * @return See above.
	 */
	String getProjectionTypeName()
	{ 
		return controlPane.getProjectionTypeName();
	}
	
	/**
     * Sets to <code>true</code> if loading data, to <code>false</code>
     * otherwise.
     * 
     * @param busy 	Pass <code>true</code> while loading data, 
     * 				<code>false</code> otherwise.
     */
    void setStatus(boolean busy) { toolBar.setStatus(busy); }
    
    /** Clears the history. */
    void clearHistory() 
    {
    	if (historyUI != null) historyUI.clearHistory();
    }
    
    /**
     * Returns the last projection parameters.
     * 
     * @return See above.
     */
	ProjectionParam getLastProjRef() { return model.getLastProjRef(); }
	
    /** 
     * Creates an history item. 
	 * 
	 * @param ref The projection ref or <code>null</code>.
	 */
	void createHistoryItem(ProjectionParam ref)
	{
		/* 28/02 Back for Beta 4.1
		if (model.isPlayingChannelMovie()) return;
		String title = null;
		if (model.getHistory() == null) 
			title = "Initial "+ImViewer.TITLE_VIEW_INDEX;
		HistoryItem node = model.createHistoryItem();
		if (node == null) return;
		if (title != null) node.setTitle(title);
		node.setProjectionRef(ref);
		node.setDefaultT(model.getDefaultT());
		node.addPropertyChangeListener(controller);
		//add Listener to node.
		if (nodeListener == null) {
			nodeListener = new MouseAdapter() {

				public void mousePressed(MouseEvent evt) {
					HistoryItem item = findParentDisplay(evt.getSource());
					try {
						if (item.getIndex() == ImViewer.PROJECTION_INDEX) {
							model.setLastProjectionRef(
									item.getProjectionRef());
							model.setLastProjectionTime(item.getDefaultT());
						}
							
						setCursor(Cursor.getPredefinedCursor(
								Cursor.WAIT_CURSOR));
						RndProxyDef def = item.getRndSettings();
						model.resetMappingSettings(def, true);
						setCursor(Cursor.getPredefinedCursor(
								Cursor.DEFAULT_CURSOR));
						resetDefaults();
						showView(item.getIndex());
						controller.renderXYPlane();
						model.setLastRndDef(def);
					} catch (Exception e) {}
				}
			};
		}
		node.addMouseListenerToComponents(nodeListener);
		addHistoryItem(node);
		*/
	}
	
	/**
	 * Replaces the component in the display either icon or busy label
	 * depending on the passed parameter.
	 * 
	 * @param b Pass <code>true</code> to indicate the creation,
	 * 			<code>false</code> to indicate that the creation is done.
	 */
	void setMeasurementLaunchingStatus(boolean b)
	{ 
		Action a = controller.getAction(ImViewerControl.MEASUREMENT_TOOL);
		a.setEnabled(!b);
		toolBar.setMeasurementLaunchingStatus(b);
	}
	
	/**
	 * Updates the scale bar menu.
	 * 
	 * @param index The selected index.
	 */
	void setDefaultScaleBarMenu(int index) { defaultIndex = index; }
	
	/**
	 * Shows or hides a busy label indicating the on-going creation of the
	 * grid image.
	 * 
	 * @param busy  Pass <code>true</code> to indicate the on-going creation,
	 * 				<code>false</code> when it is finished.
	 */
	void createGridImage(boolean busy)
	{
		controlPane.createGridImage(busy);
	}

	/**
     * Returns the parent of the image or <code>null</code> 
     * if no context specified.
     * 
     * @return See above.
     */
    DataObject getParentObject() { return model.getParent(); }
    
	/**
	 * Returns the image to view.
	 * 
	 * @return See above.
	 */
	ImageData getImage() { return model.getImage(); }
	/**
	 * Returns the ID of the viewed image.
	 * 
	 * @return See above.
	 */
	long getImageID() { return model.getImageID(); }
	/**
	 * Returns <code>true</code> if the passed object is one of the
	 * channel buttons, <code>false</code> otherwise.
	 * 
	 * @param source The object to handle.
	 * @return See above.
	 */
	boolean isSourceDisplayed(Object source)
	{
		return controlPane.isSourceDisplayed(source);
	}
		
    /**
     * Returns <code>true</code> if the viewer should be opened in a 
     * separate window, <code>false</code> otherwise.
     * The default value is <code>true</code>.
     * 
     * @return See above.
     */
	boolean isSeparateWindow() { return model.isSeparateWindow(); }
	
	/** Builds the UI to handle overlays. */
	void buildOverlays()
	{
		controlPane.buildOverlays();
	}
	
	/** 
	 * Renders the image with the overlays or not. 
	 * 
	 * @param index 	The index of the selected channel.
	 * @param selected  Pass <code>true</code> to turn the overlay on,
	 * 					<code>false</code> to turn it off.
	 */
	void renderOverlays(int index, boolean selected)
	{
		controlPane.renderOverlays(index, selected);
	}

	/**
	 * Returns <code>true</code> if the overlays are turned on,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isOverlayActive() { return controlPane.isOverlayActive(); }
	
	/**
	 * Returns the selected overlays if displayed otherwise returns 
	 * <code>null</code>.
	 * 
	 * @return See above.
	 */
	Map<Long, Integer> getSelectedOverlays()
	{
		return controlPane.getSelectedOverlays();
	}
	
	/** Invokes when the color model changes. */
	void onColorModelChanged() { controlPane.onColorModelChanged(); }
	
	/**
	 * Returns <code>true</code> if it is a large image, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isBigImage() { return model.isBigImage(); }

	/**
	 * Returns <code>true</code> if it is a large image,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isLargePlane() { return model.isLargePlane(); }

	/**
	 * Returns the view as a component.
	 * 
	 * @return See above.
	 */
	JComponent asComponent()
	{
		if (component != null) return component;
		component = new JPanel();
		component.setLayout(new BorderLayout(0, 0));
		component.add(toolBar, BorderLayout.NORTH);
		component.add(mainComponent, BorderLayout.CENTER);
		return component;
	}
	
	/** 
	 * Re-attaches the viewer. 
	 * This method should only be invoked when the image has been embedded
	 * and detached.
	 */
	void rebuild()
	{
		if (component == null) return;
		//component.add(mainComponent, BorderLayout.CENTER);
		component.add(statusBar, BorderLayout.SOUTH);
		Container c = getContentPane();
		toolBar.setSeparateWindow();
		c.setLayout(new BorderLayout(0, 0));
		c.add(component, BorderLayout.CENTER);
		
	}

	/** Invokes when the rendering control is loaded. */
	void onRndLoaded()
	{
		clearZoomMenu(zoomingGroup, zoomMenu);
		clearZoomMenu(zoomingGridGroup, zoomGridMenu);
		ViewerPreferences pref = ImViewerFactory.getPreferences();
		createZoomMenu(pref, false);
		int index = ZoomAction.getIndex(model.getZoomFactor());
		if (model.isBigImage()) index = model.getSelectedResolutionLevel();
		setMagnificationStatus(model.getZoomFactor(), index);
		controlPane.resetZoomValues();
	}
	
	/** Sets the image data.*/
	void setImageData()
	{
		setTitle(model.getImageTitle());
		statusBar.formatToolTip();
	}
	
	/**
     * Returns the group the image belongs to.
     * 
     * @return See above.
     */
    GroupData getSelectedGroup() { return model.getSelectedGroup(); }

    /**
     * Updates the component displaying the channels' details after update.
     */
    void onChannelUpdated()
    {
    	controlPane.onChannelUpdated();
    }
    
    /**
    * Sets the compression index.
    * 
    * @param index
    */
   void resetCompressionLevel(int index)
    {
    	toolBar.setCompressionLevel(index);
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
				} else packWindow();
			} else packWindow();
			UIUtilities.incrementRelativeToAndShow(
							model.getRequesterBounds(), this);
		} else {
			packWindow();
			UIUtilities.incrementRelativeToAndShow(null, this);
		}
	}
	
	/** 
	 * Overridden to show or hide the glass pane.
	 */
	public void hideAnimation()
	{
		JPanel glass = (JPanel) getGlassPane();
		hideAnimation(glass.getLayout() == null);
	}

}
