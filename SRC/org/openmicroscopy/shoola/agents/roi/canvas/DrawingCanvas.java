/*
 * org.openmicroscopy.shoola.agents.roi.canvas.DrawingCanvas
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

package org.openmicroscopy.shoola.agents.roi.canvas;


//Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.defs.ScreenPlaneArea;
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DrawingCanvas
    extends JComponent
{

    private static final int            ALPHA = 150;
    
    private static final int            LENGTH = 6;
    
    /** Basic stroke. */
    private static final BasicStroke    stroke = new BasicStroke(1.0f);
    private static final float          dash1[] = {3.0f};
    private static final BasicStroke    dashed = new BasicStroke(1.0f, 
                                                      BasicStroke.CAP_SQUARE, 
                                                      BasicStroke.JOIN_MITER, 
                                                      3.0f, dash1, 0.0f);

    private DrawingCanvasMng            manager;
    
    private boolean                     onOff, textOnOff;
    
    List                                listSPA;
    
    /** Index of the ROI selected. */
    int                                 indexSelected;
    
    public DrawingCanvas()
    {
        indexSelected = -1;
        textOnOff = false;
        listSPA = new ArrayList();
        manager = new DrawingCanvasMng(this);
    }
    
    public DrawingCanvasMng getManager() { return manager; }
    
    public void setOnOff(boolean b) { onOff = b; }
    
    public void setTextOnOff(boolean b)
    {
        textOnOff = b;
        repaint();
    }

    public void setSelectedIndex(int index)
    {
        indexSelected = index;
        repaint();
    }
    
    public void addSPAToCanvas(ScreenPlaneArea spa)
    {
        listSPA.add(spa);
    }
    
    public void removeSPAFromCanvas(int index)
    {
        ((ScreenPlaneArea) listSPA.get(index)).setPlaneArea(null);
        repaint();
    }
    
    public void clearPreviousView()
    {
        //listSPA.removeAll(listSPA);
        Iterator i = listSPA.iterator();
        ScreenPlaneArea spa;
        while (i.hasNext()) {
            spa = (ScreenPlaneArea) i.next();
            spa.setPlaneArea(null);
        }
    }
    
    public ScreenPlaneArea getScreenPlaneArea(int index)
    {
        if (index >= listSPA.size() || index <0) return null;
        return ((ScreenPlaneArea) listSPA.get(index));
    }
    
    public void magnifyPlaneAreas(double f)
    {
        Iterator i = listSPA.iterator();
        ScreenPlaneArea spa;
        PlaneArea pa;
        while (i.hasNext()) {
            spa = (ScreenPlaneArea) i.next();
            pa = spa.getPlaneArea();
            if (pa != null) {
                pa.scale(f);
                spa.setPlaneArea(pa);
            }
        }
        repaint();
    }
    
    public void setPlaneArea(PlaneArea pa, int index)
    {
        ((ScreenPlaneArea) listSPA.get(index)).setPlaneArea(pa);
        repaint();
    }
    
    void setPlaneArea(PlaneArea pa)
    {
         ((ScreenPlaneArea) listSPA.get(indexSelected)).setPlaneArea(pa);
    }
    
    /** Erase selection, the current one or all. */
    void erase()
    {
        repaint();
    }
    
    /** Overrides the paintComponent method. */
    public void paintComponent(Graphics g)
    {
        if (onOff) {
            Graphics2D g2D = (Graphics2D) g;
            if (manager.planeArea != null) {
                g2D.setColor(manager.lineColor);
                g2D.draw(manager.planeArea);
            }
            paintCollection(g2D);
        }
    }
    
    /** Paint the existing {@link PlaneArea}s if any. */
    private void paintCollection(Graphics2D g2D)
    {
        Iterator i = listSPA.iterator();
        ScreenPlaneArea spa;
        PlaneArea pa;
        Rectangle r;
        while (i.hasNext()) {
            spa = (ScreenPlaneArea) i.next();
            pa = spa.getPlaneArea();
            if (pa != null) {
                r = pa.getBounds();
                g2D.setStroke(dashed);
                g2D.setColor(alphaColor(spa.getAreaColor()));
                if (spa.getIndex() == indexSelected) {
                    g2D.setStroke(stroke);
                    g2D.setColor(spa.getAreaColor());
                }
                g2D.draw(pa);
                //draw label
                if (textOnOff)
                    g2D.drawString("#"+spa.getIndex(),  r.x-LENGTH/2, 
                            r.y-LENGTH/2);
            }
        }
    }
    
    /** Add an alpha component to the selected color. */
    private Color alphaColor(Color c) 
    {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), ALPHA);
    }
    
}

