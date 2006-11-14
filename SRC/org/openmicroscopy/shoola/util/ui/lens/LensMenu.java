/*
 * org.openmicroscopy.shoola.util.ui.lens.LensMenu 
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

//Third-party libraries

//Application-internal dependencies

/** 
 * LensMenu is a singleton class which creates both the popupmenus and menubars
 * used in the lensUI and zoomWindowUI.
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

	/** Reference to the LensMenu object -- this is a singleton. */
	private static LensMenu ref;
	
	/** Text for the popup Menu -- not shown. */
	final static String POPUP_MENU_DESCRIPTION = "Magnifying Lens Options"; 
	
	/** Text for the popup menu -- shown as a top option to the user. */
	final static String POPUP_MENU_TOPOPTION = "Magnifying Lens Options"; 
	
	/** 
	 * Text for the lens options -- parent of the resizing methods for the 
	 * lens. 
	 * */
	final static String LENS_OPTIONS = "Lens";
	
	/** 
	 * Text for the zoom options -- parent of the changing of magnification 
	 * methods for the lens. 
	 */
	final static String ZOOM_OPTIONS = "Zoom";
	
	/** 
	 * Text for the option to display units -- parent of the micron/pixel 
	 * options. 
	 */
	final static String DISPLAY_UNITS = "Units";
	
	/** Parent component of the lens and zoomWindowUI. */
	private LensComponent		lensComponent;

	/** The menubar which holds the menu items. */
	private JPopupMenu				popupMenu;
	
	/** The menubar which holds the menu items. */
	private JMenuBar				menubar;

	/**
	 * Create the menu and attach the lens component. 
	 * 
	 * @param lensComponent
	 */
	private LensMenu(LensComponent lensComponent)
	{
		this.lensComponent = lensComponent;
		createPopupMenu();
		createMenubarMenu();
	}

	/**
	 * Return the menu instance to the user, create the class if needed. 
	 * 
	 * @param lensComponent
	 * 
	 * @return reference to the singleton object. 
	 */
	static LensMenu getMenu(LensComponent lensComponent)
	{
	    if (ref == null)
	        ref = new LensMenu(lensComponent);		
	    return ref;
	}

	/**
	 * Overwrite the clone method so we cannot break the singleton class. 
	 * @return nothing we hope. 
	 * @throws CloneNotSupportedException 
	 */
	public Object clone()
        throws CloneNotSupportedException
	{
	    throw new CloneNotSupportedException(); 
	}
	
	/**
	 * Get the popup version of the menu. 
	 * 
	 * @return see above.
	 */
	JPopupMenu getPopupMenu()
	{
		return ref.popupMenu;
	}
	
	/**
	 * Get the menubar version of the menu. 
	 * 
	 * @return see above.
	 */
	JMenuBar getMenubar()
	{
		return ref.menubar;
	}
	
	/** 
	 * Create the popmenu for the lens, allow the user to change settings:
	 * zoom factor, lens size and display units.
	 *
	 */
	private void createPopupMenu()
	{
		JMenu					lensOptions;
		JMenu					zoomOptions;
		JMenuItem 				topOption;
		JMenu					displayOptions;
		JMenuItem				setLensDefaultSize;
		JMenuItem				setLens40x40;
		JMenuItem				setLens60x60;
		JMenuItem				setLens80x80;
		JMenuItem				setLens100x100;
		JMenuItem				setLens150x150;
		JMenuItem				setZoomx3;
		JMenuItem				setZoomx4;
		JMenuItem				setZoomx5;
		JMenuItem				setZoomx10;
		JRadioButtonMenuItem	setDisplayMicrons;
		JRadioButtonMenuItem	setDisplayPixels;
			
		popupMenu = new JPopupMenu(POPUP_MENU_DESCRIPTION);
		topOption = new JMenuItem(POPUP_MENU_TOPOPTION);
		popupMenu.add(topOption);
		popupMenu.addSeparator();
		lensOptions = new JMenu(LENS_OPTIONS);
		zoomOptions = new JMenu(ZOOM_OPTIONS);
		displayOptions = new JMenu(DISPLAY_UNITS);
		popupMenu.add(lensOptions);
		popupMenu.add(zoomOptions);
		popupMenu.add(displayOptions);
		
		setLensDefaultSize = new JMenuItem(new LensAction(lensComponent, 
												LensAction.LENSDEFAULTSIZE));
		setLens40x40  = new JMenuItem(new LensAction(lensComponent, 
												LensAction.LENS40x40));
		setLens60x60 = new JMenuItem(new LensAction(lensComponent, 
												LensAction.LENS60x60));
		setLens80x80 = new JMenuItem(new LensAction(lensComponent, 
												LensAction.LENS80x80));
		setLens100x100 = new JMenuItem(new LensAction(lensComponent, 
												LensAction.LENS100x100));
		setLens150x150 = new JMenuItem(new LensAction(lensComponent, 
												LensAction.LENS150x150));
		setZoomx3 = new JMenuItem(new ZoomAction(lensComponent, 
												ZoomAction.ZOOMx3));
		setZoomx4 = new JMenuItem(new ZoomAction(lensComponent, 
												ZoomAction.ZOOMx4));
		setZoomx5 = new JMenuItem(new ZoomAction(lensComponent, 
												ZoomAction.ZOOMx5));
		setZoomx10 = new JMenuItem(new ZoomAction(lensComponent, 
												ZoomAction.ZOOMx10));
		
		lensOptions.add(setLensDefaultSize);
		lensOptions.add(setLens40x40);
		lensOptions.add(setLens60x60);
		lensOptions.add(setLens80x80);
		lensOptions.add(setLens100x100);
		lensOptions.add(setLens150x150);

		zoomOptions.add(setZoomx3);
		zoomOptions.add(setZoomx4);
		zoomOptions.add(setZoomx5);
		zoomOptions.add(setZoomx10);
		
		setDisplayMicrons = new JRadioButtonMenuItem(new DisplayAction
				(lensComponent, DisplayAction.MICRON_OPTION));
		setDisplayPixels = new JRadioButtonMenuItem(new DisplayAction
				(lensComponent, DisplayAction.PIXEL_OPTION));
		displayOptions.add(setDisplayMicrons);
		displayOptions.add(setDisplayPixels);
		ButtonGroup displayUnits = new ButtonGroup();
		displayUnits.add(setDisplayMicrons);
		displayUnits.add(setDisplayPixels);
		setDisplayPixels.setSelected(true);
	}
	
	/** 
	 * Create the popmenu for the lens, allow the user to change settings:
	 * zoom factor, lens size and display units.
	 *
	 */
	private void createMenubarMenu()
	{
		JMenu					lensOptions;
		JMenu					zoomOptions;
		JMenu					displayOptions;
		JMenuItem				setLensDefaultSize;
		JMenuItem				setLens40x40;
		JMenuItem				setLens60x60;
		JMenuItem				setLens80x80;
		JMenuItem				setLens100x100;
		JMenuItem				setLens150x150;
		JMenuItem				setZoomx3;
		JMenuItem				setZoomx4;
		JMenuItem				setZoomx5;
		JMenuItem				setZoomx10;
		JRadioButtonMenuItem	setDisplayMicrons;
		JRadioButtonMenuItem	setDisplayPixels;
		
		menubar = new JMenuBar();
		lensOptions = new JMenu(LENS_OPTIONS);
		zoomOptions = new JMenu(ZOOM_OPTIONS);
		displayOptions = new JMenu(DISPLAY_UNITS);
		menubar.add(lensOptions);
		menubar.add(zoomOptions);
		menubar.add(displayOptions);
		
		setLensDefaultSize = new JMenuItem(new LensAction(lensComponent, 
												LensAction.LENSDEFAULTSIZE));
		setLens40x40  = new JMenuItem(new LensAction(lensComponent, 
												LensAction.LENS40x40));
		setLens60x60 = new JMenuItem(new LensAction(lensComponent, 
												LensAction.LENS60x60));
		setLens80x80 = new JMenuItem(new LensAction(lensComponent, 
												LensAction.LENS80x80));
		setLens100x100 = new JMenuItem(new LensAction(lensComponent, 
												LensAction.LENS100x100));
		setLens150x150 = new JMenuItem(new LensAction(lensComponent, 
												LensAction.LENS150x150));
		setZoomx3 = new JMenuItem(new ZoomAction(lensComponent, 
												ZoomAction.ZOOMx3));
		setZoomx4 = new JMenuItem(new ZoomAction(lensComponent, 
												ZoomAction.ZOOMx4));
		setZoomx5 = new JMenuItem(new ZoomAction(lensComponent, 
												ZoomAction.ZOOMx5));
		setZoomx10 = new JMenuItem(new ZoomAction(lensComponent, 
												ZoomAction.ZOOMx10));
		
		lensOptions.add(setLensDefaultSize);
		lensOptions.add(setLens40x40);
		lensOptions.add(setLens60x60);
		lensOptions.add(setLens80x80);
		lensOptions.add(setLens100x100);
		lensOptions.add(setLens150x150);
		
		zoomOptions.add(setZoomx3);
		zoomOptions.add(setZoomx4);
		zoomOptions.add(setZoomx5);
		zoomOptions.add(setZoomx10);

		setDisplayMicrons = new JRadioButtonMenuItem(new DisplayAction
				(lensComponent, DisplayAction.MICRON_OPTION));
		setDisplayPixels = new JRadioButtonMenuItem(new DisplayAction
				(lensComponent, DisplayAction.PIXEL_OPTION));
		displayOptions.add(setDisplayMicrons);
		displayOptions.add(setDisplayPixels);
		ButtonGroup displayUnits = new ButtonGroup();
		displayUnits.add(setDisplayMicrons);
		displayUnits.add(setDisplayPixels);
		setDisplayPixels.setSelected(true);
	}

}


