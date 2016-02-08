/*
 * org.openmicroscopy.shoola.util.ui.lens.LensMenu 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.ColorMenuItem;

/** 
* LensMenu is a singleton class which creates both the pop-up menus and
* menu bars used in the lensUI and zoomWindowUI.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/
class LensMenu 
{
	
	/** Text for the popup Menu -- not shown. */
	private final static String POPUP_MENU_DESCRIPTION =
		"Magnifying Lens Options"; 
	
	/** Text for the popup menu -- shown as a top option to the user. */
	private final static String POPUP_MENU_TOPOPTION = 
		"Magnifying Lens Options"; 
	
	/** 
	 * Text for the lens options -- parent of the resizing methods for the 
	 * lens. 
	 * */
	private final static String LENS_OPTIONS = "Lens";
	
	/** 
	 * Text for the zoom options -- parent of the changing of magnification 
	 * methods for the lens. 
	 */
	private final static String ZOOM_OPTIONS = "Zoom";
	
	/** 
	 * Text for the option to display units -- parent of the micron/pixel 
	 * options. 
	 */
	private final static String DISPLAY_UNITS = "Units";
	
	/** 
	 * Text for the option to change the colour of the lens.
	 */
	private final static String LENS_COLOR_OPTIONS = "Lens Color";
	
	/** Parent component of the lens and zoomWindowUI. */
	private LensComponent		lensComponent;

	/** The menubar which holds the menu items. */
	private JPopupMenu			popupMenu;
	
	/** The menubar which holds the menu items. */
	private JMenuBar			menubar;

	/** Group hosting the lens width check box elements. */
	private ButtonGroup			lensGroup;
	
	/** Group hosting the zooming check box elements. */
	private ButtonGroup			zoomGroup;
	
	/** The display actions to toggle between pixel and microns display */
	private Collection<DisplayAction> displayActions = new ArrayList<DisplayAction>();
	
	/**
	 * Creates the menu which will allow the user to adjust the size of the lens.
	 * 
	 * @return The lens sizing menu.
	 */
	private JMenu createLensOptions()
	{
		JMenu lensOptions = new JMenu(LENS_OPTIONS);
		JCheckBoxMenuItem setLensSize;
		LensAction a;
		lensGroup = new ButtonGroup();
		
		for (int i = 0 ; i < LensAction.MAX ; i++)
		{
			a = new LensAction(lensComponent, i);
			setLensSize = new JCheckBoxMenuItem();
			setLensSize.setSelected(i == LensAction.LENSDEFAULTSIZE);
			setLensSize.setAction(a);
			lensGroup.add(setLensSize);
			lensOptions.add(setLensSize);
		}
		return lensOptions;
	}
	
	/**
	 * Create the menu which will allow the user to adjust the zooming of 
	 * the lens.
	 * 
	 * @return The lens zooming menu.
	 */
	private JMenu createZoomOptions()
	{
		JMenu zoomOptions = new JMenu(ZOOM_OPTIONS);
		JCheckBoxMenuItem setLensZoom;
		ZoomAction a;
		zoomGroup = new ButtonGroup();
		for (int i = 0 ; i < ZoomAction.MAX ; i++) {
			a = new ZoomAction(lensComponent, i);
			setLensZoom = new JCheckBoxMenuItem();
			setLensZoom.setSelected(i == ZoomAction.ZOOMx1);
			setLensZoom.setAction(a);
			zoomGroup.add(setLensZoom);
			zoomOptions.add(setLensZoom);
		}
		return zoomOptions;
	}
	
	/**
	 * Create the menu which allows the user to adjust the colour of the lens.
	 * 
	 * @return The menu which allows the chaning of the lens colour.
	 */
	private JMenu createLensColorOptions()
	{
		JMenu lensColorOptions;
		ColorMenuItem lensColor;
		LensColorAction lensColorAction;
		lensColorOptions = new JMenu(LENS_COLOR_OPTIONS);
		for (int indexCnt = 0; indexCnt < LensColorAction.MAX ; indexCnt++)
		{
			lensColorAction = new LensColorAction(lensComponent, indexCnt);
			lensColor = new ColorMenuItem(lensColorAction.getColor());
			lensColor.addActionListener(lensColorAction);
			lensColor.setText(lensColorAction.getName());
			lensColorOptions.add(lensColor);
		}
		return lensColorOptions;
	}
	
	/**
	 * Create the menu which will allow the user to change the units the lens 
	 * will be measured and positioned in. (Micron or pixels); 
	 * 
	 * @return The lens select units menu.
	 */
	private JMenu createDisplayOptions()
	{
		JMenu displayOptions;
		JCheckBoxMenuItem setDisplayScale;
		displayOptions = new JMenu(DISPLAY_UNITS);
		ButtonGroup displayUnits = new ButtonGroup();
		int i;
		for (i = 0 ; i < DisplayAction.MAX ; i++)
		{
		    DisplayAction action = new DisplayAction
                    (lensComponent, i);
		    displayActions.add(action);
			setDisplayScale = new JCheckBoxMenuItem(action);
			displayUnits.add(setDisplayScale);
			displayOptions.add(setDisplayScale);
			setDisplayScale.setSelected(i == 1);
		}
		return displayOptions;
	}
	
	/**
	 * Create the menu displaying the action related to file handling e.g. 
	 * Save as. 
	 * 
	 * @return The lens select units menu.
	 */
	private JMenu createFileMenu()
	{
		JMenu menu = new JMenu("File");
		menu.add(new JMenuItem(new SaveAction(lensComponent)));
		return menu;
	}
	/** 
	 * Creates the popmenu for the lens, allow the user to change settings:
	 * zoom factor, lens size and display units.
	 */
	private void createPopupMenu()
	{
		popupMenu = new JPopupMenu(POPUP_MENU_DESCRIPTION);
		popupMenu.add(new JMenuItem(POPUP_MENU_TOPOPTION));
		popupMenu.addSeparator();
		popupMenu.add(createFileMenu());
		popupMenu.add(createLensOptions());
		popupMenu.add(createZoomOptions());
		popupMenu.add(createDisplayOptions());
		popupMenu.add(createLensColorOptions());
	}
	
	/** 
	 * Creates the popmenu for the lens, allow the user to change settings:
	 * zoom factor, lens size and display units.
	 */
	private void createMenubarMenu()
	{
		menubar = new JMenuBar();
		menubar.add(createFileMenu());
		menubar.add(createLensOptions());
		menubar.add(createZoomOptions());
		menubar.add(createDisplayOptions());
		menubar.add(createLensColorOptions());
	}
	
	/**
	 * Creates the menu and attaches the lens component. 
	 * 
	 * @param lensComponent
	 */
	LensMenu(LensComponent lensComponent)
	{
		this.lensComponent = lensComponent;
		createPopupMenu();
		createMenubarMenu();
	}
	
	/**
	 * Gets the popup version of the menu. 
	 * 
	 * @return See above.
	 */
	JPopupMenu getPopupMenu() { return popupMenu; }
	
	/**
	 * Gets the menubar version of the menu. 
	 * 
	 * @return See above.
	 */
	JMenuBar getMenubar() { return menubar; }
	
	/**
	 * Selects the menu item corresponding to the passed index.
	 * 
	 * @param index The index. 
	 */
	void setSelectedSize(int index)
	{
		JCheckBoxMenuItem item;
		LensAction a;
		Enumeration e;
		for (e = lensGroup.getElements(); e.hasMoreElements();) {
			item = (JCheckBoxMenuItem) e.nextElement();
			a = (LensAction) item.getAction();
			item.removeActionListener(a);
			item.setSelected(a.getIndex() == index);
			item.setAction(a);
		}
	}
	
	/**
	 * Selects the menu item corresponding to the passed index.
	 * 
	 * @param index The index. 
	 */
	void setZoomIndex(int index)
	{
		JCheckBoxMenuItem item;
		ZoomAction a;
		Enumeration e;
		for (e = zoomGroup.getElements(); e.hasMoreElements();) {
			item = (JCheckBoxMenuItem) e.nextElement();
			a = (ZoomAction) item.getAction();
			item.removeActionListener(a);
			item.setSelected(a.getIndex() == index);
			item.setAction(a);
		}
	}
	
    /**
     * Enables or disables the action, to set the display units to microns
     * 
     * @param b
     *            Pass <code>true</code> to enable the action,
     *            <code>false</code> to disable it
     */
    void setMicronsMenuEnabled(boolean b) {
        for (DisplayAction action : displayActions) {
            if (action.getIndex() == DisplayAction.MICRON_OPTION) {
                action.setEnabled(b);
            }
        }
    }
}


