/*
 * org.openmicroscopy.shoola.agents.roi.results.stats.graphic.GraphicCanvas
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

package org.openmicroscopy.shoola.agents.roi.results.stats.graphic;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPaneMng;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint;

/** 
 * Plot the ROIStats results.
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
public class GraphicCanvas
    extends JPanel
{
    
    /** Background color. */
    public static final Color       BACKGROUND = Color.WHITE;
    
    /** Axis color. */
    private static final Color      AXIS_COLOR = Color.GRAY;;

    private static final int        TOP = 20, LEFT = 50, RIGHT = 30, 
                                    BORDER = 20, DASH = 3, LENGTH = 10,
                                    WIDTH = 300, HEIGHT = 300;
    
    private int                     MIN_IVER = 5, MIN_IHOR = 5;

    /** Minimum and maximum values y-axis. */
    private double                  minY, maxY;
    
    /** Minimum and maximum values x-axis. */
    private int                     minX, maxX;
    
    /** Width and Height of the graphic. */
    private int                     width, height;
    
    private int                     leftBorder, widthRec, heightRec;
    
    private double                  vBin, hBin;
    
    private int                     numXBin, numYBin, vBinAxis, hBinAxis;
    
    private int                     xCorner, yCorner;
    
    private Map                     values, valuesRef;
    
    private int                     txtWidth;
    
    private String                  legend, displayFor, xAxis;
    
    private NumberFormat            nf; 
    
    private BufferedImage           graphicImage;
    
    private StatsResultsPaneMng     mng;
    
    public GraphicCanvas(StatsResultsPaneMng mng, String s)
    {
        this.mng = mng;
        displayFor = "Plot "+s+" values";
        nf = NumberFormat.getInstance();
        initTxtWidth();
        values = new TreeMap();
    }
    
    public void acrossT(Map ref, int minX, int maxX, double minY, double maxY, 
                        Map values)
    {
        legend = "z = ";
        xAxis = "timepoints";
        valuesRef = ref;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.values = values;
        initDistances(values.size());
    }

    public void acrossZ(Map ref, int minX, int maxX, double minY, double maxY, 
                        Map values)
    {
        legend = "t = ";
        xAxis = "z-sections";
        valuesRef = ref;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.values = values;
        initDistances(values.size());
    }

    /** Return the graphic representation. */ 
    public BufferedImage getGraphicImage() { return graphicImage; }
    
    /** Initializes the width of the text. */
    private void initTxtWidth()
    {
        FontMetrics metrics = getFontMetrics(getFont());
        txtWidth = metrics.charWidth('m');
    }
    
    /** Initializes the different lengths. */
    private void initDistances(int n)
    {
        numXBin = maxX-minX;
        numYBin = (int) (maxY-minY);
        if (numXBin > 0) hBin = WIDTH/numXBin; 
        hBinAxis = (int) hBin;
        if (hBin < MIN_IHOR) {
            hBinAxis = MIN_IHOR;
            numXBin = WIDTH/hBinAxis;
        }
        if (numYBin > 0) vBin = HEIGHT/(maxY-minY);
        vBinAxis = (int) vBin;
        if (vBin < MIN_IVER) {
            vBinAxis = MIN_IVER;
            numYBin = HEIGHT/vBinAxis;
        }
        height = numYBin*vBinAxis; 
        width = numXBin*hBinAxis;
        
        leftBorder = BORDER+txtWidth*(nf.format(maxY)).length();
        if (leftBorder < LEFT) leftBorder = LEFT;
        widthRec = leftBorder+width+RIGHT;
        heightRec = TOP+height+(n+2)*BORDER;
        Dimension d = new Dimension(widthRec, heightRec);
        setPreferredSize(d);
        setSize(d);
    }
    
    /** Create the {@link BufferedImage} representing the graphic.*/
    private BufferedImage createGraphic()
    {
        graphicImage = (BufferedImage) createImage(widthRec, heightRec);
        Graphics2D g2D = (Graphics2D) graphicImage.getGraphics();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2D.setColor(BACKGROUND);
        g2D.fillRect(0, 0, widthRec, heightRec);
        
        //FONT settings.
        FontMetrics fontMetrics = g2D.getFontMetrics();
        int hFont = fontMetrics.getHeight();
        g2D.setColor(AXIS_COLOR);
        drawAxis(g2D, hFont);
        //draw x-axis info
        int l = (width-(txtWidth*(xAxis.length()))/4)/2;
        g2D.drawString(xAxis, leftBorder+l, TOP+height+3*hFont/2);
        g2D.drawString(displayFor, leftBorder+BORDER, TOP+height+2*BORDER);
        //draw the curve
        drawCurves(g2D, hFont);
        return graphicImage;
    }
    
    /** Draw the axis. */
    private void drawAxis(Graphics2D g2D, int hFont)
    {
        //x-axis
        int l;
        String s;
        for (int i = 0; i <= numXBin; i++) {
            if (i == 0) {
                s = ""+minX;
                l = (txtWidth*(s.length()))/4;
                g2D.drawString(s, leftBorder-l, TOP+height+hFont);
            }  else if (i == numXBin) {
                s = ""+maxX;
                l = (txtWidth*(s.length()))/4;
                g2D.drawString(s, leftBorder+width-l, TOP+height+hFont);
            } 
            g2D.drawLine(leftBorder+i*hBinAxis, TOP+height, 
                        leftBorder+i*hBinAxis, TOP+height+DASH);
        }
        
        g2D.drawLine(leftBorder, TOP+height, leftBorder+width, TOP+height); 
        //y-axis
        for (int i = 0; i <= numYBin; i++) {
            if (i == 0) {
                s = nf.format(minY);
                l = (txtWidth*(s.length()));
                g2D.drawString(s, leftBorder-l-DASH, TOP+height+hFont/4);
            } else if (i == numYBin) {
                s = nf.format(maxY);
                l = (txtWidth*(s.length()));
                g2D.drawString(s, leftBorder-l-DASH, TOP+hFont/4);
            }    
            g2D.drawLine(leftBorder, TOP+height-i*vBinAxis, leftBorder-DASH, 
                        TOP+height-i*vBinAxis);
        }
        g2D.drawLine(leftBorder, TOP+height, leftBorder, TOP);
    }
    
    /** Draw the graph. */
    private void drawCurves(Graphics2D g2D, int hFont)
    {
        Iterator j = ((values).keySet()).iterator();
        Iterator k;
        Integer key;
        PlanePoint point;
        ArrayList list;
        int x0 = -1, x1 = -1, y0 = -1, y1 = -1;
        int c = 0;
        while (j.hasNext()) {
            key = (Integer) j.next();
            g2D.setColor((Color) valuesRef.get(key));
            drawLegend(g2D, key.intValue(), c, hFont/4);
            list = (ArrayList) values.get(key);
            k = list.iterator();
            while (k.hasNext()) {
                point = (PlanePoint) k.next();
                x1 = leftBorder+(int) Math.abs(hBin*(point.x1-minX));
                y1 = TOP+height-(int) Math.abs(vBin*(point.x2-minY));
                if (y1 < TOP) y1 = TOP;
                if (x0 != -1 && y0 != -1)
                    g2D.drawLine(x0, y0, x1, y1);
                x0 = x1;
                y0 = y1;
            }
            x0 = -1;
            y0 = -1;
            c++;
        }  
    }
    
    /** Draw the information. */
    private void drawLegend(Graphics2D g2D, int v, int c, int f)
    {
        //info on curves drawn
        g2D.drawLine(BORDER, TOP+(2+c)*BORDER+height, BORDER+LENGTH, 
                TOP+(2+c)*BORDER+height);
        g2D.drawString(legend+""+v, BORDER+3*LENGTH/2, 
                        TOP+(2+c)*BORDER+height+f);
    }
    
    /** Set the location of the canvas. */
    private void setLocation()
    {
        Rectangle r = mng.getViewportBounds();
        xCorner = ((r.width-widthRec)/2);
        yCorner = ((r.height-heightRec)/2);
        if (xCorner < 0) xCorner = 0;
        if (yCorner < 0) yCorner = 0;
    }
    
    /** Overrides the method. */ 
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        setLocation();
        g2D.drawImage(createGraphic(), null, xCorner, yCorner); 
    }

}
