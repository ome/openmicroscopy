/*
 * org.openmicroscopy.shoola.util.ui.ClosableTabbedPaneUI 
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;



//Third-party libraries

//Application-internal dependencies

/** 
 * Adds a close button to the basic implementation of the 
 * tabbed pane UI.
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
class ClosableTabbedPaneUI 
	extends BasicTabbedPaneUI
	implements MouseMotionListener
{

	/** The start color the gradient used to select a tab. */
	private static final Color		COLOR_START = 
									UIUtilities.SELECTED_BACKGROUND_COLOUR;
	
	/** The end color the gradient used to select a tab. */
	private static final Color		COLOR_END = new Color(200, 200, 255);
	
	/** The image representing a close button. */
	private Image					closeImage;
	
	/** The image representing a close button. */
	private Image					closeOverImage;

	/** Stored the close icon associated to a tab. */
	private Map<Integer, Image>		images;
	
	/** Stored the bound of the close icon for a given tab. */
	private Map<Integer, Rectangle>	rectangles;
	
	/** The mouse listener. */
	private MouseAdapter			handler;
	
	/**
	 * Shows the menu at the specified location.
	 * 
	 * @param invoker 	The component in whose space the popup menu is to
	 * 					appear.
     * @param x 		The x coordinate in invoker's coordinate space at which 
     * 					the popup menu is to be displayed
     * @param y 		The y coordinate in invoker's coordinate space at which 
     * 					the popup menu is to be displayed
	 */
	private void showMenu(Component invoker, int x, int y)
	{
		ClosablePopupMenu menu = new ClosablePopupMenu(
										(ClosableTabbedPane) tabPane);
		menu.show(invoker, x, y);
	}
	
	/** Initializes the various component. */
	private void initialize()
	{
		IconManager icons = IconManager.getInstance();
		ImageIcon icon = icons.getImageIcon(IconManager.CLOSE);
		closeImage = icon.getImage();
		icon = icons.getImageIcon(IconManager.CLOSE_OVER);
		closeOverImage = icon.getImage();
		images = new HashMap<Integer, Image>();
		rectangles = new HashMap<Integer, Rectangle>();
		handler = new MouseHandler() {
		
			/**
			 * Reacts to mouse click and release on the close rectangle.
			 * @see MouseHandler
			 */
			public void mouseReleased(MouseEvent e) {
	            int x = e.getX();
	            int y = e.getY();
	            int tabIndex = -1;
	            int tabCount = tabPane.getTabCount();
	            for (int i = 0; i < tabCount; i++) {
	                if (rects[i].contains(x, y)) {
	                    tabIndex = i;
	                    break;
	                }
	            }
			    if (tabIndex >= 0) {
			    	if (e.isPopupTrigger()) {
			    		Object source = e.getSource();
			    		if (source instanceof Component)
			    			showMenu((Component) source, x, y);
			    	} else {
			    		Rectangle r = rectangles.get(tabIndex);
			    		if (r != null && r.contains(x, y)) {
			    			Component c = tabPane.getComponentAt(tabIndex); 
			    			boolean closable = true;
			    			if (c instanceof ClosableTabbedPaneComponent) {
			    				closable = 
			    				((ClosableTabbedPaneComponent) c).isClosable();
			    			}
			    			if (closable)
			    				((ClosableTabbedPane) tabPane).remove(tabIndex);
			    		}
			    			
			    	}
			    }
			}
			
			public void mousePressed(MouseEvent e)
			{
				super.mousePressed(e);
				if (e.isPopupTrigger()) {
					Object source = e.getSource();
		    		if (source instanceof Component)
		    			showMenu((Component) source, e.getX(), e.getY());
				}
			}
		};
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param pane The pane this UI is for.
	 */
	ClosableTabbedPaneUI(JTabbedPane tabPane)
	{
		this.tabPane = tabPane;
		initialize();
	}
	
	/** Clears the maps. */
	void resetDefault()
	{
		rectangles.clear();
		images.clear();
	}
	
	/**
	 * Returns the customized handler.
	 * @see BasicTabbedPaneUI#createMouseListener()
	 */
	protected MouseListener createMouseListener() { return handler; }
	
	/**
	 * Overridden to paint the close image.
	 * @see BasicTabbedPaneUI#paintTab(Graphics, int, Rectangle[], int, 
	 * 								Rectangle, Rectangle)
	 */
	protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects,
	        				int tabIndex, Rectangle iconRect, 
	        				Rectangle textRect)
	{
		 Graphics2D g2D = (Graphics2D) g;
		 Rectangle rect = rects[tabIndex];
		 boolean selected = tabPane.getSelectedIndex() == tabIndex;
		 if (selected) {
			 GradientPaint gradient = new GradientPaint(rect.x, rect.y, 
					 				COLOR_START,
					 rect.x+rect.width, rect.y+rect.height, COLOR_END, false);
			 g2D.setPaint(gradient);
			 g2D.fill(rect);
		 } else g2D.setColor(tabPane.getBackgroundAt(tabIndex));
		 
		paintTabBorder(g, tabPlacement, tabIndex, rect.x, rect.y, 
							rect.width, rect.height, selected);

		String title = tabPane.getTitleAt(tabIndex);
		Font font = tabPane.getFont();
		FontMetrics metrics = tabPane.getFontMetrics(font);
		Icon icon = getIconForTab(tabIndex);

		layoutLabel(tabPlacement, metrics, tabIndex, title, icon, 
				rect, iconRect, textRect, selected);

		paintText(g, tabPlacement, font, metrics, 
				tabIndex, title, textRect, selected);

		paintIcon(g, tabPlacement, tabIndex, icon, iconRect, selected);

		paintFocusIndicator(g, tabPlacement, rects, tabIndex, 
				iconRect, textRect, selected);	
		
		Component c = tabPane.getComponentAt(tabIndex); 
		boolean closable = true;
		boolean closeVisible = true;
		if (c instanceof ClosableTabbedPaneComponent) {
			closable = ((ClosableTabbedPaneComponent) c).isClosable();
			closeVisible = ((ClosableTabbedPaneComponent) c).isCloseVisible();
		}
		if (!(images.containsKey(tabIndex)))
			images.put(tabIndex, closeImage);
		int x = 2+rect.x+rect.width-19;
		int y = rect.y+2;
		int w = 0, h = 0;
		Image img = images.get(tabIndex);
		if (!closable) img = closeImage;//closeOverImage;
		if (img != null && closeVisible) {
			w = img.getWidth(null);
			h = img.getHeight(null);
			g2D.drawImage(img, x, y, w, h, null); 
			Rectangle r = rectangles.get(tabIndex);
			if (r == null) r =  new Rectangle(x, y, w, h);
			else r.setBounds(x, y, w, h);
			rectangles.put(tabIndex, r);
		}
	}
	
	/**
	 * Overridden to increase the width of the tab.
	 * @see BasicTabbedPaneUI#calculateTabWidth(int, int, FontMetrics)
	 */
	protected int calculateTabWidth(int tabPlacement,int tabIndex,
	        			FontMetrics metrics)
	{
		return super.calculateTabWidth(tabPlacement, tabIndex, metrics)+24;
	}
	
	/**
	 * Modifies the close icon when the mouse is over the it.
	 * @see MouseMotionListener#mouseMoved(MouseEvent)
	 */
	public void mouseMoved(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();
		Rectangle r;
		if (rectangles == null || images == null) return;
		if (tabPane == null)
			tabPane = (JTabbedPane) e.getSource();
		for (int i = 0; i < tabPane.getTabCount(); i++) {
			r = rectangles.get(i);
			if (r != null && r.contains(x, y)) images.put(i, closeOverImage);
			else images.put(i, closeImage);
		}
		tabPane.repaint();
	}

	/**
	 * Required by the {@link MouseMotionListener} I/F but no-op implementation
	 * in our case.
	 * @see MouseMotionListener#mouseDragged(MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {}

}
