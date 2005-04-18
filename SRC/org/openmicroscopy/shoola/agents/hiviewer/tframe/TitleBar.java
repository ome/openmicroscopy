/*
 * org.openmicroscopy.shoola.agents.hiviewer.tframe.TitleBar
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

package org.openmicroscopy.shoola.agents.hiviewer.tframe;


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
 * A small title bar UI for the {@link TinyFrame}.
 * The title bar is divided in two areas.  The left area contains a button to
 * collapse/expand the frame.  Every thing on the right of the button area is
 * used to draw the frame's title.  This component has no borders so to keep
 * its area as small as possible.
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
     * Paints the background when the frame is not highlighted.
     * It's stateless, so we share it.
     */
    private static final Painter NORMAL_PAINTER = new BgPainter();
    
    /** The width and hight, in pixels, of the {@link #sizeButton}. */
    static final int    SIZE_BUTTON_DIM = 9;
    
    /** Horizontal space, in pixels, around a component. */
    static final int    H_SPACING = 2;
    
    /** 
     * The width, in pixels, of the area reserved to the {@link #sizeButton}. 
     */
    static final int    SIZE_BUTTON_AREA_WIDTH = SIZE_BUTTON_DIM+2*H_SPACING;
    //13 = 2 for space from left edge + 9 for button + 2 for space b/f title.
    
    /** The height, in pixels, of the whole title bar. */
    static final int    HEIGHT = 12; 
    
    /** The minimum width, in pixels, the title bar can be shrunk to. */
    static final int    MIN_WIDTH = SIZE_BUTTON_AREA_WIDTH+27;  //40
    
    
    /** 
     * Is in charge of painting the background.
     * Will be a different object depending on whether the frame is
     * highlighted.
     */
    private Painter         bgPainter;
    
    /** Paints the title string on the title area. */
    private TitlePainter    titlePainter;
    
    /** The button the user presses to collapse/expand the frame. */
    final SizeButton        sizeButton;
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
        update(title, null);
        sizeButton = new SizeButton();
        sizeButton.setActionType(SizeButton.COLLAPSE);
        add(sizeButton);
        setLayout(new TitleBarLayout());
    }
    
    /**
     * Updates the title bar to the new values in the model.
     * 
     * @param title The new title.
     * @param highlightColor The highlight color if a request to highlight the
     *                       frame was made, or <code>null</code> if the frame
     *                       is in normal mode.
     */
    void update(String title, Color highlightColor) 
    {
        titlePainter.setTitle(title);
        if (highlightColor == null)  //No highlighting required, normal mode.
            bgPainter = NORMAL_PAINTER;
        else bgPainter = new HiBgPainter(highlightColor);  //Highlight bg.
        repaint();
    }

    /** Overridden to do custom painting required for this component. */
    public void paintComponent(Graphics g)  
    {   
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(Color.WHITE);
        g2D.fillRect(0, 0, getWidth(), getHeight());
        bgPainter.paint(g2D, new Rectangle(0, 0, getWidth(), getHeight()));
        titlePainter.paint(g2D, getTitleAreaBounds());
    }

}
