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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.ImageIcon;
import javax.swing.plaf.basic.BasicTabbedPaneUI;


//Third-party libraries

//Application-internal dependencies

/** 
 * 
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

	/** The image representing a close button. */
	private Image			closeImage;
	
	/** The image representing a close button. */
	private Image			closeOverImage;
	
	/** The image displayed on the selected component. */
	private Image			selectedIndexImage;
	
	/** The image displayed on the selected component. */
	private Image			tabMouseImage;
	
	/** The index of the component, the mouse is over. */
	private int				tabMouseIndex;
	
	/** The bounds to the {@link #selectedIndexImage}. */
	private Rectangle		selectedRectangle;
	
	/** The bounds to the {@link #tabMouseImage}. */
	private Rectangle		mouseOverRectangle;
	
	/** The mouse listener. */
	private MouseAdapter	handler;
	
	/** Initializes the various component. */
	private void initialize()
	{
		tabMouseIndex = -1;
		IconManager icons = IconManager.getInstance();
		ImageIcon icon = icons.getImageIcon(IconManager.CLOSE);
		closeImage = icon.getImage();
		icon = icons.getImageIcon(IconManager.CLOSE_OVER);
		closeOverImage = icon.getImage();
		mouseOverRectangle = new Rectangle();
		selectedRectangle = new Rectangle();
		selectedIndexImage = closeImage;
		tabMouseImage = closeImage;
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
			    if (tabIndex >= 0 && !e.isPopupTrigger()) {
			    	if (selectedRectangle.contains(x, y) ||
			    			mouseOverRectangle.contains(x, y))
			    		tabPane.remove(tabIndex);
		        }
			}
		};
	}
	
	/** Creates a new instance. */
	ClosableTabbedPaneUI()
	{
		initialize();
	}
	
	/**
	 * @see BasicTabbedPaneUI#createMouseListener()
	 */
	protected MouseListener createMouseListener()
	{ 
		return handler;
	}
	
	/**
	 * Overridden to paint the close image.
	 * @see BasicTabbedPaneUI#paintTab(Graphics, int, Rectangle[], int, 
	 * 								Rectangle, Rectangle)
	 */
	protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects,
	        				int tabIndex, Rectangle iconRect, 
	        				Rectangle textRect)
	{
		 super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
		 Graphics2D g2D = (Graphics2D) g;
		 Rectangle rect = rects[tabIndex];
		 int x = rect.x+rect.width-19;
		 int y = rect.y+6;
		 int w = 0, h = 0;
		 if (tabIndex == tabPane.getSelectedIndex()) {
			 w = selectedIndexImage.getWidth(null);
			 h = selectedIndexImage.getHeight(null);
			 selectedRectangle.setBounds(x, y, w, h);
			 g2D.drawImage(selectedIndexImage, x, y, w, h, null); 
		 } else if (tabIndex == tabMouseIndex) {
			 w = tabMouseImage.getWidth(null);
			 h = tabMouseImage.getHeight(null);
			 mouseOverRectangle.setBounds(x, y, w, h);
			 g2D.drawImage(tabMouseImage, x, y, w, h, null); 
		 }
	}
	
	/**
	 * Overridden to increase the width of the tab.
	 * @see BasicTabbedPaneUI#calculateTabWidth(int, int, FontMetrics)
	 */
	protected int calculateTabWidth(int tabPlacement,int tabIndex,
	        			FontMetrics metrics) {
		return super.calculateTabWidth(tabPlacement, tabIndex, metrics)+24;
	}
	
	/**
	 * Displays or hides the close icons.
	 * @see MouseMotionListener#mouseMoved(MouseEvent)
	 */
	public void mouseMoved(MouseEvent e)
	{
		int x = e.getX();
        int y = e.getY();
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            if (rects[i].contains(x, y)) {
           	 tabMouseIndex = i;
               break;
            }
            tabMouseIndex = -1;
        }
        if (tabMouseIndex == tabPane.getSelectedIndex()) {
        	if (selectedRectangle.contains(x, y)) {
        		selectedIndexImage = closeOverImage;
        	} else selectedIndexImage = closeImage;
         } else if (tabMouseIndex != -1) {
        	 if (mouseOverRectangle.contains(x, y)) {
        		 tabMouseImage = closeOverImage;
         	} else tabMouseImage = closeImage;
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
