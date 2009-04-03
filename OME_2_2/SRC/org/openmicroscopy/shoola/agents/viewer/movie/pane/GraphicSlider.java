/*
 * org.openmicroscopy.shoola.agents.viewer.movie.pane.MovieSlider
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

package org.openmicroscopy.shoola.agents.viewer.movie.pane;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies

/** 
 * Slider with two knobs.
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
public class GraphicSlider
    extends JPanel
{
    
    /** Default length of the slider. */
    static final int                    LENGTH= 200;
    
    static final int                    BORDER = 20;
    
    /** Height of the slider, must be a multiple of 2. */
    static final int                    HEIGHT_SLIDER = 20;
    
    /** Size of the panel. */
    static final int                    WIDTH = LENGTH+2*BORDER, 
                                        HEIGHT = 2*BORDER+HEIGHT_SLIDER;
    
    static final int                    BS2 = BORDER+HEIGHT_SLIDER/2,
                                        BL = BORDER+LENGTH;

    
    static final int                    KW2 = 6, KH2 = 8;
    
    static final int                    KNOB_WITH = 2*KW2, KNOB_HEIGHT = 2*KH2;

    private static final int            UP = 2, UP_END = 4, UP_MIDDLE = 3;
    
    private static final int            BIN = 5, NB_BIN = LENGTH/BIN, 
                                        MODULO = 6;
    
    /** Color the knob. Light_gray with alpha component. */
    private static final Color          DEFAULT_KNOB_COLOR = 
                                        new Color(255, 0, 0, 90);
    
    private Color                       knobColor;
    
    private String                      start, end;
    
    /** x-coordinate of the knobs. */
    private Rectangle2D                 startKnob, endKnob;
    
    private GraphicSliderMng            manager;
    
    GraphicSlider(MoviePaneMng mpMng, int max, int s, int e)
    { 
        manager = new GraphicSliderMng(this, mpMng, max);
        knobColor = DEFAULT_KNOB_COLOR;
        start = ""+s;
        end = ""+e;
        startKnob = new Rectangle2D.Double();
        endKnob = new Rectangle2D.Double();
        manager.setStart(s);
        manager.setEnd(e);
        Dimension d = new Dimension(WIDTH, HEIGHT);
        setPreferredSize(d);
        setSize(d);
        repaint();
    }
    
    void setKnobColor(Color c) { if (c != null) knobColor = c; }
    
    GraphicSliderMng getManager() { return manager; }
    
    void attachListeners() { manager.attachListeners(); }

    void removeListeners() { manager.removeListeners(); }
    
    void setKnobStart(int x)
    {
        startKnob.setRect(x, BS2-KH2, KNOB_WITH, KNOB_HEIGHT);
    }
    
    void setKnobEnd(int x)
    {
        endKnob.setRect(x, BS2-KH2, KNOB_WITH, KNOB_HEIGHT);
    }
    
    /** Overrides the paintComponent() method. */ 
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(getBackground());
        g2D.fillRect(0, 0, WIDTH, HEIGHT); 
        //FONT settings.
        FontMetrics fontMetrics = g2D.getFontMetrics();
        int hFont = fontMetrics.getHeight();
        Font font = g2D.getFont();
        Rectangle2D rStart = font.getStringBounds(start,
                                    g2D.getFontRenderContext());
        int wStart = (int) rStart.getWidth();
        Rectangle2D rEnd = font.getStringBounds(end, 
                                    g2D.getFontRenderContext());
        int wEnd = (int) rEnd.getWidth();
        
        g2D.setColor(Color.GRAY);
        //draw slider
        g2D.drawLine(BORDER, BS2, BL, BS2);
        g2D.drawLine(BORDER, BS2-UP_END, BORDER, BS2+UP_END); 
        g2D.drawLine(BL, BS2-UP_END, BL, BS2+UP_END);
        for (int i = 1; i <= NB_BIN-1; i++) {
            if (i%MODULO ==  MODULO-1)
                g2D.drawLine(BORDER+i*BIN, BS2-UP_MIDDLE, BORDER+i*BIN, BS2);
            else
                g2D.drawLine(BORDER+i*BIN, BS2-UP, BORDER+i*BIN, BS2);
        }
            
        //draw min and max values.
        g2D.drawString(start, BORDER-wStart/2, BS2+2*UP_END+hFont);
        g2D.drawString(end, BL-wEnd/2, BS2+2*UP_END+hFont);
        
        //draw knobs
        g2D.setColor(knobColor);
        
        g2D.fill(startKnob);
        g2D.fill(endKnob);
        g2D.draw(startKnob);
        g2D.draw(endKnob);
    }
    
}
