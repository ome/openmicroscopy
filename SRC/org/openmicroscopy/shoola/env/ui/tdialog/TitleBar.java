/*
 * org.openmicroscopy.shoola.env.ui.tdialog.TitleBar
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.ui.tdialog;





//Java imports
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
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
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TitleBar
    extends JComponent
{

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
    static final int    SIZE_BUTTON_AREA_WIDTH = 2*SIZE_BUTTON_DIM+3*H_SPACING;
    //13 = 2 for space from left edge + 9 for button + 2 for space b/f title.
    
    /** The height, in pixels, of the whole title bar. */
    static final int    HEIGHT = 12; 
    
    /** The minimum width, in pixels, the title bar can be shrunk to. */
    static final int    MIN_WIDTH = SIZE_BUTTON_AREA_WIDTH+26;  //50
    
    /** Paints the title string on the title area. */
    private TitlePainter    titlePainter;
    
    /** The button the user presses to collapse/expand the window. */
    final SizeButton        sizeButton;
    
    /** The button the user presses to close the window. */
    final CloseButton       closeButton;
    //TODO: make private.
    
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
        
        return new Rectangle(SIZE_BUTTON_AREA_WIDTH, 0, 
                                w-SIZE_BUTTON_AREA_WIDTH, getHeight());
    }
    
    /**
     * Creates a new title bar for the specified <code>frame</code>.
     * 
     * @param title The frame's title.
     */
    TitleBar(String title) 
    {
        setBorder(BorderFactory.createEmptyBorder());
        titlePainter = new TitlePainter(new Font("SansSerif", Font.PLAIN, 16));
        update(title);
        //create buttons
        sizeButton = new SizeButton();
        sizeButton.setActionType(SizeButton.COLLAPSE);
        closeButton = new CloseButton();
        closeButton.setActionType(CloseButton.CLOSE_ACTION);
        
        //add sub components
        add(sizeButton);
        add(closeButton);
        
        setLayout(new TitleBarLayout());
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

    /** Overridden to do custom painting required for this component. */
    public void paintComponent(Graphics g)  
    {   
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(Color.WHITE);
        g2D.fillRect(0, 0, getWidth(), getHeight());
        titlePainter.paint(g2D, getTitleAreaBounds(), 
                new Rectangle(0, 0, getWidth(), getHeight()));
    }

}
