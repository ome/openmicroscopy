/*
 * org.openmicroscopy.shoola.util.ui.tpane.TinyPaneIcon
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;


//Third-party libraries

//Application-internal dependencies

/** 
 * The frame icon in the {@link TitleBar}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 4694 $ $Date: 2006-12-15 17:02:59 +0000 (Fri, 15 Dec 2006) $)
 * </small>
 * @since OME2.2
 */
public class TinyPaneIcon
    extends JLabel
    implements TinyObserver, PropertyChangeListener
{

    /** The Model this icon is working with. */
    private TinyPane   model;


    /**
     * Creates a new instance.
     * 
     * @param model The Model this icon will be working with.
     *              Mustn't be <code>null</code>.
     */
    TinyPaneIcon(TinyPane model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
    }

    /**
     * Registers this icon with the Model.
     * @see TinyObserver#attach()
     */
    public void attach()
    {
        model.addPropertyChangeListener(TinyPane.FRAME_ICON_PROPERTY, this);
        propertyChange(null);  //Synch icon w/ current state.
    }

    /**
     * Detaches this icon from the Model's change notification registry.
     * @see TinyObserver#detach()
     */
    public void detach()
    {
        model.removePropertyChangeListener(TinyPane.FRAME_ICON_PROPERTY, this);
    }

    /**
     * Updates the display every time the the Model's icon changes.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        //NOTE: We can only receive FRAME_ICON_PROPERTY changes, see attach().
        setIcon(model.getFrameIcon());
    }
    
}
