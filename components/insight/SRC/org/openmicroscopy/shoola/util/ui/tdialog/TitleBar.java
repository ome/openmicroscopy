/*
 * org.openmicroscopy.shoola.util.ui.tdialog.TitleBar
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

package org.openmicroscopy.shoola.util.ui.tdialog;


//Java imports
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies

/** 
 * A small title bar UI for the {@link TinyDialog}.
 * The title bar is divided in two areas. The left area contains two buttons:
 * a button to collapse/expand the window and a button to close the window.
 * Every thing on the right of the button area is used to draw the window's 
 * title. This component has no borders so to keep its area as small 
 * as possible.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 4724 $ $Date: 2007-01-17 08:46:48 +0000 (Wed, 17 Jan 2007) $)
 * </small>
 * @since OME2.2
 */
class TitleBar
    extends JComponent
{

	/** Identifies the <code>SizeButton</code>. */
	static final int	SIZE_BUTTON = 0;
	
	/** Identifies the <code>CloseButton</code>. */
	static final int	CLOSE_BUTTON = 1;
	
    /** 
     * The width and hight, in pixels, of the {@link #sizeButton} and
     * {@link #closeButton}. 
     */
    static final int    SIZE_BUTTON_DIM = 9;
    
    /** Horizontal space, in pixels, around a component. */
    static final int    H_SPACING = 2;
    
    /** 
     * The width, in pixels, of the area reserved to the {@link #sizeButton}
     * and  {@link #closeButton}. 
     */
    private static final int   SIZE_BUTTON_AREA_WIDTH = 2*SIZE_BUTTON_DIM+
    													3*H_SPACING;
    //13 = 2 for space from left edge + 9 for button + 2 for space b/f title.
    
    /** The height, in pixels, of the whole title bar. */
    static final int    HEIGHT = 12; 
    
    /** The minimum width, in pixels, the title bar can be shrunk to. */
    static final int    MIN_WIDTH = SIZE_BUTTON_AREA_WIDTH+26;  //50
    
    /** Paints the title string on the title area. */
    private TitlePainter    	titlePainter;
    
    /** The button the user presses to collapse/expand the window. */
    private SizeButton        	sizeButton;
    
    /** The button the user presses to close the window. */
    private CloseButton       	closeButton;
    
    /** Default width of the button area. */
    private int					buttonAreaWidth;
    
    /**
     * Returns the current bounds of the area reserved to the title.
     * 
     * @return See above.
     */
    private Rectangle getTitleAreaBounds()
    {
        int w = getWidth();  //Current width.
        if (w <= SIZE_BUTTON_AREA_WIDTH)
            //This should never happen b/c min size of title bar is determined
            //by minimumLayoutSize() and this method is only invoked while
            //painting, so the title bar should have been sized.
            return new Rectangle(0, 0, 0, 0);
        
        return new Rectangle(buttonAreaWidth, 0, 
                                w-SIZE_BUTTON_AREA_WIDTH, getHeight());
    }
    
    /**
     * Creates a new title bar for the specified <code>frame</code>.
     * 
     * @param title	 The frame's title.
     * @param index  The type of buttons to display in the title bar.
     */
    TitleBar(String title, int index) 
    {
        setBorder(BorderFactory.createEmptyBorder());
        titlePainter = new TitlePainter(new Font("SansSerif", Font.PLAIN, 16));
        update(title);
        //create buttons
        sizeButton = new SizeButton();
        sizeButton.setActionType(SizeButton.COLLAPSE);
        //add sub components
        buttonAreaWidth = SIZE_BUTTON_DIM+2*H_SPACING;
        closeButton = new CloseButton();
        closeButton.setActionType(CloseButton.CLOSE_ACTION);
    	add(closeButton);
    	buttonAreaWidth += SIZE_BUTTON_DIM+H_SPACING;
        setLayout(new TitleBarLayout());
        setDecoration(null, index);
    }
    
    /**
     * Sets the node's decoration.
     * 
     * @param l 	The list of components to add to the title bar.
     * @param index	The type of buttons to display in the title bar.
     */
    void setDecoration(List l, int index)
    {
    	removeAll();
    	//add(sizeButton);
    	/*
    	buttonAreaWidth = SIZE_BUTTON_DIM+2*H_SPACING;
    	if (closeButton != null) {
    		add(closeButton);
    		buttonAreaWidth += SIZE_BUTTON_DIM+H_SPACING;
    	}
    	*/
    	switch (index) {
    		case TinyDialog.CLOSE_ONLY:
    			buttonAreaWidth = SIZE_BUTTON_DIM+2*H_SPACING;
    			add(closeButton);
    		break;
    		case TinyDialog.SIZE_ONLY:
    			buttonAreaWidth = SIZE_BUTTON_DIM+2*H_SPACING;
    			add(sizeButton);
    		break;
			case TinyDialog.BOTH:
			//default:
				buttonAreaWidth = 2*SIZE_BUTTON_DIM+3*H_SPACING;
			add(sizeButton);
			add(closeButton);
		}
    	
    	if (l == null) return;
    	Iterator i = l.iterator();
    	Object object;
    	JComponent c;
    	while (i.hasNext()) {
    		object = i.next();
			if (object instanceof JComponent) {
				c = (JComponent) object;
				add(c);
				buttonAreaWidth += c.getPreferredSize().width+H_SPACING;
			}	
		}
    }

    /**
     * Returns the button identified by the passed id.
     * 
     * @param id The id of the button.
     * @return See above.
     */
    JButton getButton(int id)
    {
    	switch (id) {
			case SIZE_BUTTON: return sizeButton;
			case CLOSE_BUTTON: return closeButton;
		}
    	return null;
    }
    
    /**
     * Updates the title bar to the new values in the model.
     * 
     * @param t The new title.
     */
    void update(String t) 
    {
        setToolTipText(t);
        titlePainter.setTitle(t);
        repaint();
    }
    /**
     * Derives the default font of the title.
     * 
     * @param style The new style to set.
     */
    void setFontStyle(int style)
    {
    	titlePainter.setFontStyle(style);
    	repaint();
    }
    
    /** 
     * Overridden to do custom painting required for this component. 
     * @see JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)  
    {   
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(Color.WHITE);
        g2D.fillRect(0, 0, getWidth(), getHeight());
        titlePainter.paint(g2D, getTitleAreaBounds(), 
                new Rectangle(0, 0, getWidth(), getHeight()));
    }

}
