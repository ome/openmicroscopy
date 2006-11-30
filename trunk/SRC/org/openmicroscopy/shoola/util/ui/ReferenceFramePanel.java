/*
 * org.openmicroscopy.shoola.util.ui.table.ReferenceFramePanel
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

package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Utility class to draw a frame.
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
public class ReferenceFramePanel
    extends JPanel
{
    
    /** Type of frame. */
    public static final int             UP_RIGHT = 0, DOWN_RIGHT = 1;
    
    /** Constants used to draw the XY-axis. */
    private static final int            LENGTH = 25,  ARROW = 3, START = 10;
    
    private static final Dimension      DIM = new Dimension(2*LENGTH, 2*LENGTH);
    
    private String      hAxis;
    
    private String      vAxis;
    
    private String      origin;
    
    private boolean     drawOrigin;
    
    private Color       axisColor;
    
    private int         type;
    
    public ReferenceFramePanel(String hAxis, String vAxis, String origin)
    {
        this.hAxis = hAxis;
        this.vAxis = vAxis;
        this.origin = origin;
        type = UP_RIGHT;  //default
        setPreferredSize(DIM);
    }

    /** Set the color of the axis. */
    public void setAxisColor(Color c) { axisColor = c; }
    
    /** Set the type of frame. */
    public void setTypeFrame(int type) { this.type = type;}
    
    public void setDrawOrigin(boolean b) { drawOrigin = b;}
   
    /** Overrides the paintComponent. */
    public void paint(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        FontMetrics fontMetrics = g2D.getFontMetrics();
        if (axisColor == null) axisColor = Color.BLACK;
        g2D.setColor(getBackground());
        Dimension d = getSize();
        g2D.fillRect(0, 0, d.width, d.height); 
        g2D.setColor(axisColor);
        g2D.setStroke(new BasicStroke(1.5f));
        switch (type) {
            case UP_RIGHT:
                upFrame(g2D, fontMetrics.getHeight()); break;
            case DOWN_RIGHT:
                downFrame(g2D, fontMetrics.getHeight()); break;
        }
    }
    
    /** Paint a downFrame i.e. origin in the top-left corner. */
    private void downFrame(Graphics2D g2D, int hFont)
    {
        int x1 = 2*START;
        int y1 = 3*START/2;
        //x-axis
        g2D.drawLine(x1, y1, x1+LENGTH, y1);
        g2D.drawLine(x1-ARROW+LENGTH, y1-ARROW, x1+LENGTH, y1);
        g2D.drawLine(x1-ARROW+LENGTH, y1+ARROW, x1+LENGTH, y1);
        //y-axis
        g2D.drawLine(x1, y1, x1, y1+LENGTH);
        g2D.drawLine(x1-ARROW, y1+LENGTH-ARROW, x1, y1+LENGTH);
        g2D.drawLine(x1+ARROW, y1+LENGTH-ARROW, x1, y1+LENGTH);   
        //name
        if(drawOrigin) g2D.drawString(origin, x1-hFont/4, y1-hFont/4);
        g2D.drawString(hAxis, x1+LENGTH/2, y1-hFont/4);
        g2D.drawString(vAxis, x1-hFont, y1+LENGTH/2);
    }
    
    /** Paint a upFrame i.e. origin in the bottom-left corner. */
    private void upFrame(Graphics2D g2D, int hFont)
    {
        int x1 = 2*START;
        int y1 = START/2;
        //x-axis
        g2D.drawLine(x1, y1+LENGTH, x1+LENGTH, y1+LENGTH);
        g2D.drawLine(x1-ARROW+LENGTH, y1+LENGTH-ARROW, x1+LENGTH, y1+LENGTH);
        g2D.drawLine(x1-ARROW+LENGTH, y1+LENGTH+ARROW, x1+LENGTH, y1+LENGTH);
        //y-axis
        g2D.drawLine(x1, y1, x1, y1+LENGTH);
        g2D.drawLine(x1-ARROW, y1+ARROW, x1, y1);
        g2D.drawLine(x1+ARROW, y1+ARROW, x1, y1);   
        //name
        if(drawOrigin) g2D.drawString(origin, x1-hFont/4, y1+LENGTH+hFont);
        g2D.drawString(hAxis, x1+LENGTH/2, y1+LENGTH+hFont);
        g2D.drawString(vAxis, x1-hFont, y1+LENGTH/2);
    }
    
}
