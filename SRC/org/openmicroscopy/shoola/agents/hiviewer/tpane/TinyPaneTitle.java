/*
 * org.openmicroscopy.shoola.agents.hiviewer.tpane.TinyPaneTitle
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

package org.openmicroscopy.shoola.agents.hiviewer.tpane;





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
 * (<b>Internal version:</b> $Revision$ $Date$)
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
        titlePainter.setTitle(model.getTitle());
        repaint();
    }
    
    /** Overridden to do custom painting required for this component. */
    public void paintComponent(Graphics g)  
    {   
        Graphics2D g2D = (Graphics2D) g;
        titlePainter.paint(g2D, new Rectangle(0, 0, getWidth(), getHeight()));
    }
    
}
