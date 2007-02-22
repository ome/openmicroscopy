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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.actions.UnitBarSizeAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ViewerAction;
import org.openmicroscopy.shoola.agents.imviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.imviewer.util.ChannelColorMenuItem;
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.ColorCheckBoxMenuItem;
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

    /** The available colors for the unit bar. */
    private static Map<Color, String>	colors;
    
    static {
    	colors = new LinkedHashMap<Color, String>();
    	colors.put(ImagePaintingFactory.UNIT_BAR_COLOR, 
    				ImagePaintingFactory.UNIT_BAR_COLOR_NAME);
    	colors.put(Color.ORANGE, "Orange");
    	colors.put(Color.YELLOW, "Yellow");
    	colors.put(Color.BLACK, "Black");
    	colors.put(new Color(75, 0, 130), "Indigo");
    	colors.put(new Color(238, 130, 238), "Violet");
    	colors.put(Color.RED, "Red");
    	colors.put(Color.GREEN, "Green");
    	colors.put(Color.BLUE, "Blue");
    }
    
    /** Reference to the Control. */
    private ImViewerControl controller;
    
    /** Reference to the Model. */
    private ImViewerModel   model;
 
    /** The status bar. */
    private StatusBar       statusBar;
    
	/** Lens component which will control all behaviour of the lens. */
	private LensComponent	lens;
	
    /** The tool bar. */
    private ToolBar         toolBar;
    
    /** The control pane. */
    private ControlPane     controlPane;
    
    /** Group hosting the items of the <code>Rate</code> menu. */
    private ButtonGroup     ratingGroup;
    
    /** Group hosting the items of the <code>Zoom</code> menu. */
    private ButtonGroup     zoomingGroup;
    
    /** Group hosting the items of the <code>Color Model</code> menu. */
    private ButtonGroup     colorModelGroup;
    
    /** The loading window. */
    private LoadingWindow   loadingWindow;
    
    /** First time the lens has been shown then variable <code>true</code>. */
    private boolean 		firstTimeLensShown = true;
    
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
     * @return The controls submenu;
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
        return menu;
    }
    
    /**
     * Helper method to create the help menu.
     * 
     * @return The controls submenu;
     */
    private JMenu createHelpMenu()
    {
        JMenu menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        return menu;
    }
    
    /**
     * Helper method to create the controls menu.
     * 
     * @return The controls submenu;
     */
    private JMenu createControlsMenu()
    {
        JMenu menu = new JMenu("Controls");
        menu.setMnemonic(KeyEvent.VK_C);
        ViewerAction action = controller.getAction(ImViewerControl.RENDERER);
        JMenuItem item = new JMenuItem(action);
        item.setText(action.getName());
        menu.add(item);
        action = controller.getAction(ImViewerControl.MOVIE);
        item = new JMenuItem(action);
        item.setText(action.getName());
        menu.add(item);
        action = controller.getAction(ImViewerControl.LENS);
        item = new JMenuItem(action);
        item.setText(action.getName());
        menu.add(item);
        menu.add(createColorModelMenu());
        menu.add(new JSeparator());
        action = controller.getAction(ImViewerControl.SAVE);
        item = new JMenuItem(action);
        item.setText(action.getName());
        menu.add(item);
        return menu;
    }
    
    /**
     * Helper method to create the color model submenu.
     * 
     * @return The color model submenu.
     */
    private JMenu createColorModelMenu()
    {
        JMenu menu = new JMenu("Models");
        colorModelGroup = new ButtonGroup();
        ViewerAction action = controller.getAction(
                        ImViewerControl.GREY_SCALE_MODEL);
        JRadioButtonMenuItem item = new JRadioButtonMenuItem();
        String cm = model.getColorModel();
        item.setSelected(cm.equals(ImViewer.GREY_SCALE_MODEL));
        item.setAction(action);
        colorModelGroup.add(item);
        menu.add(item);
        action = controller.getAction(ImViewerControl.RGB_MODEL);
        item = new JRadioButtonMenuItem();
        item.setAction(action);
        item.setSelected(cm.equals(ImViewer.RGB_MODEL) || 
                        cm.equals(ImViewer.HSB_MODEL));
        colorModelGroup.add(item);
        menu.add(item);
        //action = controller.getAction(ImViewerControl.HSB_MODEL);
        //item = new JRadioButtonMenuItem();
        //item.setAction(action);
        //item.setSelected(cm.equals(ImViewer.HSB_MODEL));
        //colorModelGroup.add(item);
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
        //tabbedPane =  new JTabbedPane();
        Browser browser = model.getBrowser();
        browser.setComponentsSize(model.getMaxX(), model.getMaxY());
        //tabbedPane.addTab(browser.getTitle(), browser.getUI());
        Container container = getContentPane();
        container.setLayout(new BorderLayout(0, 0));
        container.add(toolBar, BorderLayout.NORTH);
        container.add(controlPane, BorderLayout.WEST);
        container.add(browser.getUI(), BorderLayout.CENTER);
        container.add(statusBar, BorderLayout.SOUTH);
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
        toolBar = new ToolBar(controller, model);
        controlPane = new ControlPane(controller, model, this); 
        statusBar = new StatusBar();
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
        //pack();
    }
    
    /**
     * Updates UI components when a zooming factor is selected.
     * 
     * @param action    The selected action embedding the zooming factor
     *                  information.
     */
    void setZoomFactor(ViewerAction action)
    {
        controlPane.setZoomFactor(action);
        JCheckBoxMenuItem b;
        Enumeration e;
        for (e = zoomingGroup.getElements(); e.hasMoreElements();) {
            b = (JCheckBoxMenuItem) e.nextElement();
            if ((b.getAction()).equals(action)) {
                b.removeActionListener(action);
                b.setSelected(true);
                b.setAction(action);
            }
        }
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
     * @param action    The selected action embedding the color model
     *                  information.
     */
    void setColorModel(ViewerAction action)
    {
        controlPane.setColorModel();
        AbstractButton b;
        Enumeration e;
        for (e = colorModelGroup.getElements(); e.hasMoreElements();) {
            b = (AbstractButton) e.nextElement();
            if ((b.getAction()).equals(action)) {
                b.removeActionListener(action);
                b.setSelected(true);
                b.setAction(action);
            }
        }
    }

    /**
     * Updates UI components when a new z-section is selected.
     * 
     * @param z The selected z-section.
     */
    void setZSection(int z)
    {
        toolBar.setZSection(z);
        controlPane.setZSection(z);
    }

    /**
     * Updates UI components when a new timepoint is selected.
     * 
     * @param t The selected timepoint.
     */
    void setTimepoint(int t)
    {
        toolBar.setTimepoint(t);
        controlPane.setTimepoint(t);
    }

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
        toolBar.onStateChange(b);
        controlPane.onStateChange(b);
    }

    /**
     * Updates status bar.
     * 
     * @param description   The text to display.
     */
    void setStatus(String description)
    {
        statusBar.setStatus(description);
    }

    /**
     * Updates the buttons' selection when a new button is selected or 
     * deselected.
     */
    void setChannelsSelection() { controlPane.setChannelsSelection(); }

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
     * Sets the image in the lens to the plane image shown on the screen.
     * 
     * @param img The value to set.
     */
    void setLensPlaneImage(BufferedImage img)
    {
    	if (lens == null) return;
        lens.setPlaneImage(img);
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
    
    /** Displays the lens on screen when the image is zoomed. */
    void scrollLens()
    {
    	if (lens == null) return;
    	model.getBrowser().scrollTo(lens.getLensScaledBounds(), false);
    }
    
    /**
     * Sets the lens's visibility. If the lens hasn't previously created, 
     * we first create the lens.
     * 
     * @param b Pass <code>true</code> to display the lens, <code>false</code>
     * 			otherwise.
     */
    void setLensVisible(boolean b)
    {
        if (lens == null) {
        	lens = new LensComponent(this);
        	lens.addPropertyChangeListener(
        			LensComponent.LENS_LOCATION_PROPERTY, controller);
        }
        if (b) {
            if (model.getMaxX() < lens.getLensUI().getWidth() || 
                model.getMaxY() < lens.getLensUI().getHeight())
                    return;
            if (firstTimeLensShown) {
                firstTimeLensShown = false;
                int diffX = model.getMaxX()-lens.getLensUI().getWidth();
                int diffY = model.getMaxY()-lens.getLensUI().getHeight();
                int lensX = diffX/2;
                int lensY = diffY/2;
                if (lensX+lens.getLensUI().getWidth() > model.getMaxX())
                    lensX = diffX;
                if (lensY+lens.getLensUI().getHeight() > model.getMaxY())
                    lensY = diffY;
                lens.setImageZoomFactor((float) model.getZoomFactor());
                lens.setLensLocation(lensX, lensY);
                lens.setXYPixelMicron(model.getPixelsSizeX(), 
                                    model.getPixelsSizeY());
                model.getBrowser().addComponent(lens.getLensUI());
                lens.setZoomWindowLocation(getX()+getWidth(), getY());
            }

            lens.setImageZoomFactor((float) model.getZoomFactor());
            lens.setPlaneImage(model.getOriginalImage());
            lens.setLensPreferredColour();
        }
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
     *                  {@link ImViewer#COLOR_PICKER_MENU}.
     * @param source	The component that requested the popup menu.
     * @param location	The point at which to display the menu, relative to the
     *                  <code>component</code>'s coordinates.
     */
	void showMenu(int menuID, Component source, Point location)
	{
		if (menuID != ImViewer.COLOR_PICKER_MENU) return;
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
                int width = 9*(screen.width/10);
                int height = 9*(screen.height/10);
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
