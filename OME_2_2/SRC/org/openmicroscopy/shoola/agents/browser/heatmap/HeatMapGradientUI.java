/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapGradientUI
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.heatmap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.browser.util.StringPainter;

/**
 * UI for showing the legend of the heat map.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class HeatMapGradientUI extends JPanel
                                     implements HeatMapGradient
{
    private Font valueFont = new Font(null,Font.PLAIN,10);
    private Font axisFont = new Font(null,Font.BOLD,12);
    
    private boolean inBooleanMode = false;
    
    /**
     * The predefined cold color of the UI.
     */
    private Color coldColor = new Color(0,0,192);
    
    /**
     * The predefined warm color of the UI.
     */
    private Color hotColor = new Color(255,0,0);
    
    private Color falseColor = new Color(255,0,0);
    private Color trueColor = new Color(0,255,0);
    
    /**
     * The predefined inactive cold color of the UI.
     */
    public static final Color inactiveColdColor = new Color(192,192,192);
    
    /**
     * The predefined inactive warm color of the UI.
     */
    public static final Color inactiveHotColor = new Color(102,102,102);
    
    private double min, max;
    private double minRange, maxRange;
    private String axisName = "";
    
    public Set dataTypeListeners;
    
    public HeatMapGradientUI()
    {
        // do nothin, really
        min = 0;
        max = 100;
        min = minRange;
        max = maxRange;
        
        dataTypeListeners = new HashSet();
    }
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        
        if(inBooleanMode)
        {
            paintComponentBoolean(g2);
        }
        else
        {
            paintComponentScalar(g2);
        }
    }
    
    public void paintComponentScalar(Graphics2D g2)
    {
        Dimension dim = getSize();
        Rectangle2D rect = new Rectangle2D.Double(15,getHeight()/2-10,
                                                          dim.getWidth()-30,30);
        
        if(isEnabled())
        {
            g2.setColor(Color.black);
            g2.setFont(valueFont);
        
            GradientPaint gp = new GradientPaint((float)rect.getX(),
                                                 (float)rect.getY(),coldColor,
                                                 (float)(rect.getX()+rect.getWidth()),
                                                 (float)rect.getY(),hotColor);
            g2.setFont(valueFont);
            FontMetrics numberMetrics = g2.getFontMetrics(valueFont);
            Rectangle2D minBounds =
                numberMetrics.getStringBounds(String.valueOf(min),g2);
        
            Rectangle2D maxBounds =
                numberMetrics.getStringBounds(String.valueOf(max),g2);
        
            String theMin = String.valueOf(min);
            String theMax = String.valueOf(max);
            StringPainter.drawString(g2,theMin,(float)rect.getX(),
                          (float)(rect.getY()+rect.getHeight()+minBounds.getHeight()));
                      
            StringPainter.drawString(g2,theMax,
                          (float)(rect.getX()+rect.getWidth()-maxBounds.getWidth()),
                          (float)(rect.getY()+rect.getHeight()+maxBounds.getHeight()));
                          
            g2.setFont(axisFont);
            FontMetrics axisMetrics = g2.getFontMetrics(axisFont);
            Rectangle2D titleBounds = axisMetrics.getStringBounds(axisName,g2);
            
            StringPainter.drawString(g2,axisName,
                          (float)(rect.getX()+
                                  (rect.getWidth()-titleBounds.getWidth())/2),
                          (float)(rect.getY()-titleBounds.getHeight()-10));
            
            g2.setPaint(gp);
            g2.fill(rect);    
            g2.setColor(Color.black);
            g2.draw(rect);                          
        }
        else
        {
            GradientPaint gp = new GradientPaint((float)rect.getX(),
                                                 (float)rect.getY(),
                                                 inactiveColdColor,
                                                 (float)(rect.getX()+rect.getWidth()),
                                                 (float)rect.getY(),
                                                 inactiveHotColor);
                                                 
            g2.setPaint(gp);
            g2.fill(rect);
            g2.setColor(Color.gray);
            g2.draw(rect);
            
            g2.setFont(axisFont);
            FontMetrics axisMetrics = g2.getFontMetrics(axisFont);
            String dummyString = "(no scalar selected)";
            Rectangle2D titleBounds =
                axisMetrics.getStringBounds(dummyString,g2);
            StringPainter.drawString(g2,dummyString,
                          (float)(rect.getX()+
                                  (rect.getWidth()-titleBounds.getWidth())/2),
                          (float)(rect.getY()-titleBounds.getHeight()-5));
        }
    }
    
    public void paintComponentBoolean(Graphics2D g2)
    {
        Dimension dim = getSize();
        
        Rectangle2D rect = new Rectangle2D.Double(15,getHeight()/2-10,
                                                  dim.getWidth()-30,30);
                                                  
        Rectangle2D falseRect = new Rectangle2D.Double(rect.getX(),
                                                       rect.getY(),
                                                       rect.getWidth()/2,
                                                       rect.getHeight());
        Rectangle2D trueRect =
            new Rectangle2D.Double(rect.getX()+(rect.getWidth()/2),
                                   rect.getY(),rect.getWidth()/2,
                                   rect.getHeight());
        
        if(isEnabled())
        {
            g2.setColor(Color.black);
            g2.setFont(valueFont);
            FontMetrics numberMetrics = g2.getFontMetrics(valueFont);
            Rectangle2D minBounds =
                numberMetrics.getStringBounds("False",g2);
        
            Rectangle2D maxBounds =
                numberMetrics.getStringBounds("True",g2);
                
            StringPainter.drawString(g2,"False",(float)rect.getX(),
                          (float)(rect.getY()+rect.getHeight()+minBounds.getHeight()));
                          
            StringPainter.drawString(g2,"True",
                          (float)(rect.getX()+rect.getWidth()-maxBounds.getWidth()),
                          (float)(rect.getY()+rect.getHeight()+maxBounds.getHeight()));
                          
            g2.setFont(axisFont);
            
            FontMetrics axisMetrics = g2.getFontMetrics(axisFont);
            Rectangle2D titleBounds = axisMetrics.getStringBounds(axisName,g2);
            
            StringPainter.drawString(g2,axisName,
                          (float)(rect.getX()+
                                  (rect.getWidth()-titleBounds.getWidth())/2),
                          (float)(rect.getY()-titleBounds.getHeight()-10));
            
            g2.setPaint(falseColor);
            g2.fill(falseRect);
            g2.setPaint(trueColor);
            g2.fill(trueRect);
            g2.setColor(Color.black);
            g2.draw(falseRect);
            g2.draw(trueRect); 
        }
        else
        {
            g2.setPaint(inactiveColdColor);
            g2.fill(falseRect);
            g2.setPaint(inactiveHotColor);
            g2.fill(trueRect);
            g2.setColor(Color.black);
            g2.draw(falseRect);
            g2.draw(trueRect);
        }
    }
    
    public double getMin()
    {
        return min;
    }
    
    public double getMax()
    {
        return max;
    }
    
    public double getRangeMin()
    {
        return minRange;
    }
    
    public double getRangeMax()
    {
        return maxRange;
    }
    
    public String getAxisName()
    {
        return axisName;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapGradient#getMinColor()
     */
    public Color getMinColor()
    {
        return coldColor;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapGradient#getMaxColor()
     */
    public Color getMaxColor()
    {
        return hotColor;
    }
    
    public void setMin(double d)
    {
        this.min = d;
        this.minRange = d;
        repaint();
    }
    
    public void setMax(double d)
    {
        this.max = d;
        this.maxRange = d;
        repaint();
    }
    
    public void setRangeMin(double d)
    {
        this.minRange = d;
        repaint();
    }
    
    public void setRangeMax(double d)
    {
        this.maxRange = d;
        repaint();
    }
    
    public void setAxisName(String name)
    {
        this.axisName = name;
        repaint();
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapGradient#setMinColor(java.awt.Color)
     */
    public void setMinColor(Color c)
    {
        if(c != null)
        {
            this.coldColor = c;
            repaint();
        }
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapGradient#setMaxColor(java.awt.Color)
     */
    public void setMaxColor(Color c)
    {
        if(c != null)
        {
            this.hotColor = c;
            repaint();
        }
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapGradient#isDiscrete()
     */
    public boolean isDiscrete()
    {
        return inBooleanMode;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapGradient#setDiscrete(boolean)
     */
    public void setDiscrete(boolean discrete)
    {
        inBooleanMode = discrete;
        for(Iterator iter = dataTypeListeners.iterator(); iter.hasNext();)
        {
            HeatMapDTListener listener = (HeatMapDTListener)iter.next();
            if(discrete) listener.inBooleanMode();
            else listener.inScalarMode();
        }
        revalidate();
        repaint();
    }
    
    public void addDTListener(HeatMapDTListener listener)
    {
        if(listener != null)
        {
            dataTypeListeners.add(listener);
        }
    }
    
    public void removeDTListener(HeatMapDTListener listener)
    {
        if(listener != null)
        {
            dataTypeListeners.remove(listener);
        }
    }

}
