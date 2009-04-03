/*
 * org.openmicroscopy.shoola.agents.imviewer.util.PlaneSlicingPaneStatic
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

package org.openmicroscopy.shoola.agents.imviewer.util;




//Java imports
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class PlaneSlicingPaneStatic
    extends JPanel
{
    
    private static final int        topBorder = PlaneSlicingPane.topBorder, 
                                    leftBorder = PlaneSlicingPane.leftBorder,
                                    square = PlaneSlicingPane.square, 
                                    bottomBorder = 
                                            PlaneSlicingPane.bottomBorder;
    private static final int        lS = leftBorder+square, 
                                    tS = topBorder+square, 
                                    tS4 = topBorder+30,
                                    rStart = leftBorder+square/2-10, 
                                    rEnd = leftBorder+square/2+10,
                                    yStart = tS-square/2+10,
                                    yEnd = tS-square/2-10;

    private PlaneSlicingDialog      model;

    PlaneSlicingPaneStatic(PlaneSlicingDialog model)
    {
        this.model = model;
    }
    
    /** Overrides the {@link #paintComponent(Graphics)} method. */ 
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(PlaneSlicingPane.bgColor);
        g2D.fillRect(0, 0, PlaneSlicingPane.WIDTH, PlaneSlicingPane.HEIGHT);
        Font font = g2D.getFont();
        FontMetrics fontMetrics = g2D.getFontMetrics();
        int hFont = fontMetrics.getHeight();
        Rectangle2D rInput = font.getStringBounds("(2) Input", 
                                g2D.getFontRenderContext());
        int wInput = (int) rInput.getWidth();
        // grid
        AffineTransform transform = new AffineTransform();
        // 140/10 = 14 then middle = 14/2
        transform.translate(leftBorder+70, topBorder+70); 
        transform.scale(1, -1);
        transform.scale(10, 10);       
        g2D.setPaint(PlaneSlicingPane.gridColor);
        GeneralPath path = new GeneralPath();
        for (int i = -7; i <= 7; i++) {
            path.moveTo(i, -7);
            path.lineTo(i, 7);
        }
        for (int i = -7; i <= 7; i++) {
            path.moveTo(-7, i);
            path.lineTo(7, i);
        }
        g2D.draw(transform.createTransformedShape(path));
        g2D.setColor(PlaneSlicingPane.axisColor);
         
        //y-axis
        g2D.drawLine(leftBorder, topBorder-8, leftBorder, tS+5);
        g2D.drawLine(leftBorder, topBorder-8, leftBorder-3, topBorder-5);
        g2D.drawLine(leftBorder, topBorder-8, leftBorder+3, topBorder-5);
        g2D.drawLine(leftBorder-5, topBorder, leftBorder, topBorder);
         
        // x-axis
        g2D.drawLine(leftBorder-5, tS, lS+8, tS);
        g2D.drawLine(lS+5, tS-3, lS+8, tS);
        g2D.drawLine(lS+5, tS+3, lS+8, tS);
        g2D.drawLine(lS, tS, lS, tS+5);
        g2D.drawString("(2) Input", leftBorder+square/2-wInput/2, 
                        tS+bottomBorder/2+hFont);
        g2D.drawLine(rStart, yStart, rStart, tS4);
        g2D.drawLine(rEnd, yEnd, rEnd, tS4);
         
        //set line color
        g2D.setColor(PlaneSlicingPane.lineColor);
        g2D.setStroke(new BasicStroke(1.5f));
         
        // drawline
        g2D.drawLine(leftBorder, tS, rStart, yStart);
        g2D.drawLine(rEnd, yEnd, lS, topBorder);
        g2D.setColor(PlaneSlicingPane.fixedLineColor);
        g2D.drawLine(rStart+1, tS4, rEnd, tS4);
        if (model.getPaneIndex() == PlaneSlicingDialog.STATIC) {
            g2D.setColor(PlaneSlicingPane.borderColor);
            g2D.draw(new Rectangle2D.Double(0, 0, PlaneSlicingPane.WIDTH,
                        PlaneSlicingPane.HEIGHT));
        } else {
            g2D.setColor(PlaneSlicingPane.layerColor);
            g2D.fillRect(0, 0, PlaneSlicingPane.WIDTH, PlaneSlicingPane.HEIGHT);
        }
    }
     
}
