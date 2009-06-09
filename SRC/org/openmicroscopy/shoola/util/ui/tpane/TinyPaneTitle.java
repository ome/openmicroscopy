/*
 * org.openmicroscopy.shoola.util.ui.tpane.TinyPaneTitle
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

package org.openmicroscopy.shoola.util.ui.tpane;


//Java imports
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies

/** 
 * The frame title in the {@link TitleBar}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 4694 $ $Date: 2006-12-15 17:02:59 +0000 (Fri, 15 Dec 2006) $)
 * </small>
 * @since OME2.2
 */
class TinyPaneTitle
    extends JComponent
    implements TinyObserver, PropertyChangeListener
{
    
    /** The Model this icon is working with. */
    private TinyPane       model;
    
    /** Paints the title string on the title area. */
    private TitlePainter    titlePainter;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    TinyPaneTitle(TinyPane model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        titlePainter = new TitlePainter(new Font("SansSerif", Font.PLAIN, 16));
        //NOTE: The TitlePainter will then adjust the font size according to
        //the title bar's height.  However, in any case the font size won't 
        //be bigger than 16pt.
    }

    /**
     * Derives the default font of the painter.
     * 
     * @param style The new style to set.
     */
    void setFontStyle(int style) { titlePainter.setFontStyle(style); }
    
    /**
     * Registers this component with the Model.
     * @see TinyObserver#attach()
     */
    public void attach()
    {
        model.addPropertyChangeListener(TinyPane.TITLE_PROPERTY, this);
        propertyChange(null);  //Synch w/ current state.
    }
    
    /**
     * Detaches this component from the Model's change notification registry.
     * @see TinyObserver#detach()
     */
    public void detach()
    {
        model.removePropertyChangeListener(TinyPane.TITLE_PROPERTY, this);
    }
    
    /**
     * Updates the display every time the the Model's icon changes.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        //NOTE: We can only receive TITLE_PROPERTY changes, see attach().
        titlePainter.setTitle(model.getTitle()+model.getNote());
        repaint();
    }
    
    /** 
     * Overridden to do custom painting required for this component. 
     * @see JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)  
    {   
        Graphics2D g2D = (Graphics2D) g;
        titlePainter.paint(g2D, new Rectangle(0, 0, getWidth(), getHeight()));
    }
    
}
