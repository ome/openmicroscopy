/*
 * org.openmicroscopy.shoola.agents.imviewer.util.PlaneSlicingPane
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
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
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
class PlaneSlicingPane
    extends JPanel
{
    
    /** Graphics constants. */
    static final int                WIDTH = 220, HEIGHT = 200;
    static final int                topBorder = 20, leftBorder = 40,
                                    square = 140, bottomBorder = 30, 
                                    rightBorder = 10,
                                    lS = leftBorder+square, 
                                    tS = topBorder+square, 
                                    tS2 = topBorder+square/2,
                                    rStart = leftBorder+square/2-20+1, 
                                    rEnd = leftBorder+square/2+20;
                                    
    /** Width of the knob. */
    static final int                triangleW = 7;
    
    /** Height of the knob. */
    static final int                triangleH = 8;
    
    /** Background color of the panel. */
    static final Color              bgColor = Color.WHITE;
    
    /** Color of the border painted when the panel is selected. */
    static final Color              borderColor = Color.BLUE;
    
    /** Color of the fixed line. */
    static final Color              fixedLineColor = Color.BLACK;
    
    /** Color of the grid. */
    static final Color              gridColor = Color.LIGHT_GRAY;
    
    /** Color of the axis. */
    static final Color              axisColor = Color.GRAY;
    
    /** Color of the lines. */
    static final Color              lineColor = Color.RED;
    
    /** 
     * Color of the layer painted on top of the panel wher the panel 
     * is not selected.
     * Light_gray with alpha component.
     */
    static final Color              layerColor = new Color(192, 192, 192, 80);
    
    /** Color of the start knob (input and output). */
    private static final Color      ostartColor = Color.BLACK;
    
    /** Color of the end knob (input and output). */
    private static final Color      oendColor = Color.GRAY;
    
    /** Control points. */
    private Point2D                 startPt, endPt;
    
    private int                     xStartOutput1, xStartOutput2, xStartOutput3, 
                                    yStartOutput1, yStartOutput2, yStartOutput3;
    private int                     xEndOutput1, xEndOutput2, xEndOutput3,
                                    yEndOutput1, yEndOutput2, yEndOutput3;

    private PlaneSlicingDialog      model;
    
    PlaneSlicingPane(PlaneSlicingDialog model)
    {
        this.model = model;

    }

    void initialize(int yStart, int yEnd)
    {
        setKnobOutputStart(leftBorder-10, yStart);
        setKnobOutputEnd(lS+10, yEnd);
        startPt = new Point2D.Double();
        endPt = new Point2D.Double();
        startPt.setLocation(leftBorder, yStart);
        endPt.setLocation(lS, yEnd);
        repaint();
    }
    
    /** 
     * Sets the position of the start Knob.
     * 
     * @param x x-coordinate.
     * @param y y-coordinate.
     */
    void setKnobOutputStart(int x, int y)
    {  
        xStartOutput1 = x;
        xStartOutput2 = x-triangleH;
        xStartOutput3 = x-triangleH;
        yStartOutput1 = y;
        yStartOutput2 = y-triangleW;
        yStartOutput3 = y+triangleW;
    }
    
    /** 
     * Sets the position of the end Knob.
     * 
     * @param x x-coordinate.
     * @param y y-coordinate.
     */
    void setKnobOutputEnd(int x, int y)
    {
        xEndOutput1 = x;
        xEndOutput2 = x+triangleH;
        xEndOutput3 = x+triangleH;
        yEndOutput1 = y;
        yEndOutput2 = y-triangleW;
        yEndOutput3 = y+triangleW;
    }
    
    /** 
     * Sets the location of the start Knob and the control point.
     * 
     * @param y The y-coordinate.
     */
    void updateOutputStart(int y)
    {
        yStartOutput1 = y;
        yStartOutput2 = y-triangleW;
        yStartOutput3 = y+triangleW;
        startPt.setLocation(startPt.getX(), y);
        repaint();
    } 
    
    /** 
     * Sets the location of the end Knob and the control point.
     * 
     * @param y The y-coordinate.
     */
    void updateOutputEnd(int y)
    {
        yEndOutput1 = y;
        yEndOutput2 = y-triangleW;
        yEndOutput3 = y+triangleW;
        endPt.setLocation(endPt.getX(), y);
        repaint();
    }
    
    /** Overrides the {@link #paintComponent(Graphics)} method. */ 
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(bgColor);
        g2D.fillRect(0, 0, WIDTH, HEIGHT); 
        Font font = g2D.getFont();
        FontMetrics fontMetrics = g2D.getFontMetrics();
        int hFont = fontMetrics.getHeight();
        Rectangle2D rInput = font.getStringBounds("(1) Input", 
                                    g2D.getFontRenderContext());
        Rectangle2D rUpper= font.getStringBounds("upper", 
                                    g2D.getFontRenderContext());                            
        int wInput = (int) rInput.getWidth();
        int wUpper = (int) rUpper.getWidth();
         
        // grid
        AffineTransform transform = new AffineTransform();
        // 140/10 = 14 then middle = 14/2
        transform.translate(leftBorder+70, topBorder+70); 
        transform.scale(1, -1);
        transform.scale(10, 10);       
        g2D.setPaint(gridColor);
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
        g2D.setColor(axisColor);
         
        //y-axis
        g2D.drawLine(leftBorder, topBorder-8, leftBorder, tS+5);
        g2D.drawLine(leftBorder, topBorder-8, leftBorder-3, topBorder-5);
        g2D.drawLine(leftBorder, topBorder-8, leftBorder+3, topBorder-5);
        g2D.drawLine(leftBorder-5, topBorder, leftBorder, topBorder);
         
        //x-axis
        g2D.drawLine(leftBorder-5, tS, lS+8, tS);
        g2D.drawLine(lS+5, tS-3, lS+8, tS);
        g2D.drawLine(lS+5, tS+3, lS+8, tS);
        g2D.drawLine(lS, tS, lS, tS+5);
         
        //output cursor start 
        int xStartOutputPoints[] = {xStartOutput1, xStartOutput2, 
                                xStartOutput3};
        int yStartOutputPoints[] = {yStartOutput1, yStartOutput2, 
                                yStartOutput3};
        GeneralPath filledPolygonStartOutput = new GeneralPath();
        filledPolygonStartOutput.moveTo(xStartOutputPoints[0], 
                                    yStartOutputPoints[0]);
        for (int index = 1; index < xStartOutputPoints.length; index++)
            filledPolygonStartOutput.lineTo(xStartOutputPoints[index], 
                                            yStartOutputPoints[index]);
        filledPolygonStartOutput.closePath();
        g2D.setColor(ostartColor);
        g2D.fill(filledPolygonStartOutput);
        //output cursor end output
        int xEndOutputPoints[] = {xEndOutput1, xEndOutput2, xEndOutput3};
        int yEndOutputPoints[] = {yEndOutput1, yEndOutput2, yEndOutput3};
        GeneralPath filledPolygonEndOutput = new GeneralPath();
        filledPolygonEndOutput.moveTo(xEndOutputPoints[0], 
                                    yEndOutputPoints[0]);
        for (int index = 1; index < xEndOutputPoints.length; index++)
            filledPolygonEndOutput.lineTo(xEndOutputPoints[index], 
                                        yEndOutputPoints[index]);
        filledPolygonEndOutput.closePath();
        g2D.setColor(oendColor);
        g2D.fill(filledPolygonEndOutput);
        g2D.drawString("lower", leftBorder+10, tS+hFont);
        g2D.drawString("upper", lS-wUpper-10, tS+hFont);
        g2D.drawString("(1) Input", leftBorder+square/2-wInput/2, 
                    tS+bottomBorder/2+hFont);
        g2D.drawLine(rStart-1, (int) startPt.getY(), rStart-1, tS2);
        g2D.drawLine(rEnd, tS2, rEnd, (int) endPt.getY());
        //set line color
        g2D.setColor(lineColor);
        g2D.setStroke(new BasicStroke(1.5f));
        //drawline
        g2D.drawLine(leftBorder+1, (int) startPt.getY(), rStart-1,
                    (int) startPt.getY());
        g2D.drawLine(rEnd+1, (int) endPt.getY(), lS, (int) endPt.getY());
         
        //Fixed line.
        g2D.setColor(fixedLineColor);
        g2D.drawLine(rStart, tS2, rEnd, tS2);
        if (model.getPaneIndex() == PlaneSlicingDialog.NON_STATIC) {
            g2D.setColor(borderColor);
            g2D.draw(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT));
        } else {
            g2D.setColor(layerColor);
            g2D.fillRect(0, 0, WIDTH, HEIGHT);
        }
    }
     
}
