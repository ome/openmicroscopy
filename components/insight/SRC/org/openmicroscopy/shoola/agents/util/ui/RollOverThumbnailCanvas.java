/*
 * org.openmicroscopy.shoola.agents.util.ui.RollOverThumbnailCanvas 
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
package org.openmicroscopy.shoola.agents.util.ui;




//Java imports
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Custom <code>JComponent</code> to paint the thumbnail.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class RollOverThumbnailCanvas 
	extends JComponent
{
    
    /** Reference to the model. */
    private final RollOverThumbnail	model;

    /**
     * Creates a new instance. 
     * 
     * @param md	Reference to the model. Mustn't be <code>null</code>.
     */
    RollOverThumbnailCanvas(RollOverThumbnail md)
    {
        if (md == null) throw new IllegalArgumentException("No model.");
        this.model = md;
        setOpaque(false); 
        setBorder(BorderFactory.createBevelBorder(
                BevelBorder.LOWERED, UIUtilities.INNER_BORDER_HIGHLIGHT, 
                UIUtilities.INNER_BORDER_SHADOW));
        addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				model.magnifyImage(e.getWheelRotation());
			}
		});
    }
    
    /** 
     * Overridden to paint the thumbnail.
     * @see JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        Graphics2D g2D = (Graphics2D) g;
        if (model.getImage() != null) {
            Insets i = getInsets();
            int x = i.left+1;
            int y = i.top+1;
            g2D.drawImage(model.getImage(), null, x, y);
        }
    }
    
}
