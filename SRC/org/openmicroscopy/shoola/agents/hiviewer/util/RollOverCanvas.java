/*
 * org.openmicroscopy.shoola.agents.hiviewer.util.RollOverCanvas
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

package org.openmicroscopy.shoola.agents.hiviewer.util;



//Java imports
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies

/** 
 * Custom <code>JComponent</code> to paint the thumbnail.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class RollOverCanvas
    extends JComponent
{
    
    /** 
     * The highlight color to use for the inner border surrounding the
     * frame's contents.
     */
    static final Color      INNER_BORDER_HIGHLIGHT = new Color(240, 240, 240);
    
    /** 
     * The shadow color to use for the inner border surrounding the
     * frame's contents.
     */
    static final Color      INNER_BORDER_SHADOW = new Color(200, 200, 200);
    
    /** Reference to the model. */
    private final RollOverWin   model;
    
    /** The pin image painted in the top-right corner. */
    private ImageIcon           pinIcon;
    
    /** The pin image painted in the top-right corner. */
    private ImageIcon           annotatedIcon;
    
    /** The pin image painted in the top-right corner. */
    private ImageIcon           classifiedIcon;
    
    /** The location of the pin icon. */
    private Rectangle           pinRectangle;
    
    /** The location of the pin icon. */
    private Rectangle           annotatedRectangle;
    
    /** The location of the pin icon. */
    private Rectangle           classifiedRectangle;

    /**
     * Creates a new instance. 
     * 
     * @param m         Reference to the model. Mustn't be <code>null</code>.
     * @param pinIcon   The pin icon painted in the top-left corner.
     */
    RollOverCanvas(RollOverWin m, ImageIcon pinIcon)
    {
        if (m == null) throw new IllegalArgumentException("No model.");
        if (pinIcon == null) throw new IllegalArgumentException("No pin.");
        model = m;
        this.pinIcon = pinIcon;
        pinRectangle = new Rectangle();
        annotatedRectangle = new Rectangle();
        classifiedRectangle = new Rectangle();
        setOpaque(false); 
        setBorder(BorderFactory.createBevelBorder(
                BevelBorder.LOWERED, INNER_BORDER_HIGHLIGHT, 
                INNER_BORDER_SHADOW));
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)
            {
                Point p = e.getPoint();
                if (pinRectangle.contains(p)) model.pinThumbnail();
                else if (annotatedRectangle.contains(p)) model.annotate();
                else if (classifiedRectangle.contains(p)) model.classify();
            }
        });
    }
    
    void initialize(ImageIcon annotatedIcon, ImageIcon classifiedIcon)
    {
        this.annotatedIcon = annotatedIcon;
        this.classifiedIcon = classifiedIcon;
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
            int w = getWidth();
            int width = pinIcon.getIconWidth();
            int height = pinIcon.getIconHeight();
            pinRectangle.setBounds(w-width-5, y, width, height);
            g2D.drawImage(pinIcon.getImage(), w-width-5, y, width, height, null);
            if (annotatedIcon != null) {
                width = annotatedIcon.getIconWidth();
                height = annotatedIcon.getIconHeight();
                annotatedRectangle.setBounds(x, y, width, height);
                g2D.drawImage(annotatedIcon.getImage(), x, y, width, height, 
                                null);
                x += annotatedIcon.getIconWidth()+2;
            }
            if (classifiedIcon != null) {
                width = classifiedIcon.getIconWidth();
                height = classifiedIcon.getIconHeight();
                classifiedRectangle.setBounds(x, y, width, height);
                g2D.drawImage(classifiedIcon.getImage(), x, y, width, height, 
                                null);
                //x += annotatedIcon.getIconWidth()+2;
            }
        }  
    }
    
}
