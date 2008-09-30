/*
 * org.openmicroscopy.shoola.agents.dataBrowser.util.RollOverThumbnailCanvas 
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
package org.openmicroscopy.shoola.agents.dataBrowser.util;




//Java imports
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
 * @since OME3.0
 */
class RollOverThumbnailCanvas 
	extends JComponent
{
    
    /** Reference to the model. */
    private final RollOverThumbnail	model;
    
    /** The pin image painted in the top-right corner. */
    private ImageIcon           	pinIcon;
    
    /** The location of the pin icon. */
    private Rectangle           	pinRectangle;

    /**
     * Creates a new instance. 
     * 
     * @param md		Reference to the model. Mustn't be <code>null</code>.
     * @param pinIcon   The pin icon painted in the top-left corner.
     */
    RollOverThumbnailCanvas(RollOverThumbnail md, ImageIcon pinIcon)
    {
        if (md == null) throw new IllegalArgumentException("No model.");
        if (pinIcon == null) throw new IllegalArgumentException("No pin.");
        this.model = md;
        this.pinIcon = pinIcon;
        pinRectangle = new Rectangle();
        setOpaque(false); 
        setBorder(BorderFactory.createBevelBorder(
                BevelBorder.LOWERED, UIUtilities.INNER_BORDER_HIGHLIGHT, 
                UIUtilities.INNER_BORDER_SHADOW));
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)
            {
                Point p = e.getPoint();
                if (pinRectangle.contains(p)) model.pinThumbnail();
                if (e.getClickCount() == 2) model.viewImage();
            }
        });
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
            /*
            int w = getWidth();
            int width = pinIcon.getIconWidth();
            int height = pinIcon.getIconHeight();
            pinRectangle.setBounds(w-width-5, y, width, height);
            g2D.drawImage(pinIcon.getImage(), w-width-5, y, width, height, 
            				null);
            				*/
        }  
    }
}
