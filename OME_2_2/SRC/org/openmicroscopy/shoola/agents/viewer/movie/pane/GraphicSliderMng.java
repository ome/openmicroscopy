/*
 * org.openmicroscopy.shoola.agents.viewer.movie.pane.MovieSliderMng
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

//Third-party libraries

//Application-internal dependencies

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
public class GraphicSliderMng
    implements MouseListener, MouseMotionListener
{
    
    private static final int    MIN = GraphicSlider.BORDER,
                                MAX = GraphicSlider.BL;
    
    private int                 max;
    
    private Rectangle           boxStart, boxEnd;

    private boolean             dragging;
    
    private int                 maxStart, minEnd;
    
    private GraphicSlider       view;
    
    private MoviePaneMng        mpMng;
    
    GraphicSliderMng(GraphicSlider view, MoviePaneMng mpMng, int max)
    {
        this.max = max;
        this.view = view;
        this.mpMng = mpMng;
        boxStart = new Rectangle();
        boxEnd = new Rectangle();
        //setKnobStart(MIN-GraphicSlider.KNOB_WITH/2);
        //setKnobEnd(MAX-GraphicSlider.KNOB_WITH/2);
    }

    /** Attach Mouse listeners. */
    void attachListeners()
    {
        view.addMouseListener(this);
        view.addMouseMotionListener(this);
    }
    
    /** Remove mouse listeners. */
    void removeListeners()
    {
        view.removeMouseListener(this);
        view.removeMouseMotionListener(this);
    }
    
    /** Handle mouse pressed. */
    public void mousePressed(MouseEvent e)
    {
        Point p = e.getPoint();
        if (!dragging) {
            dragging = true;
            if (boxStart.contains(p) && p.x < minEnd && p.x > MIN) 
                handleKnobStart(p.x);
            else if (boxEnd.contains(p) && p.x > maxStart && p.x < MAX)
                handleKnobEnd(p.x);
         }  //else dragging already in progress 
    }

    /** Handle mouse dragged. */
    public void mouseDragged(MouseEvent e)
    {
        Point p = e.getPoint();
        if (dragging) {
            if (boxStart.contains(p) && p.x < minEnd && p.x > MIN) 
                handleKnobStart(p.x);
            else if (boxEnd.contains(p) && p.x > maxStart && p.x < MAX)
                handleKnobEnd(p.x);
         }
    }
   
    /** Reset the dragging control to <code>false</code>. */
    public void mouseReleased(MouseEvent e) { dragging = false; }
      
    /** 
     * Set the location of the start knob.
     * 
     * @param v     value in the range 0, max.
     */
    void setStart(int v)
    {
        int gv = convertRealIntoGraphics(v);
        positionStartKnob(gv);
    }
    
    /** 
     * Set the location of the end knob.
     * 
     * @param v     value in the range 0, max.
     */
    void setEnd(int v)
    {
        int gv = convertRealIntoGraphics(v);
        positionEndKnob(gv);
    }

    /** Handle the dragging of the startKnob. */
    private void handleKnobStart(int x)
    {
        int curRealValue = convertGraphicsIntoReal(x-GraphicSlider.BORDER-1);
        mpMng.setMovieStart(curRealValue);
        positionStartKnob(x);
    }
    
    /** Set the location of the start knob. */
    private void positionStartKnob(int x)
    {
        x -= GraphicSlider.KW2;
        setKnobStart(x);
        view.setKnobStart(x); 
        view.repaint();
    }
    
    /** Handle the dragging of the endKnob. */
    private void handleKnobEnd(int x)
    {  
        int curRealValue = convertGraphicsIntoReal(x-GraphicSlider.BORDER+1);
        mpMng.setMovieEnd(curRealValue);
        positionEndKnob(x);
    }
    
    /** Set the location of the end knob. */
    private void positionEndKnob(int x)
    {
        x -= GraphicSlider.KW2;
        setKnobEnd(x);
        view.setKnobEnd(x); 
        view.repaint();
    }
    
    /** Set the bounds of the rectangle controlling the startKnob. */
    private void setKnobStart(int x)
    {
        maxStart = x+GraphicSlider.KNOB_WITH;
        boxStart = new Rectangle(x, 
                            GraphicSlider.BS2-GraphicSlider.KH2, 
                            GraphicSlider.KNOB_WITH, 
                            GraphicSlider.KNOB_HEIGHT);
    }
    
    /** Set the bounds of the rectangle controlling the endKnob. */
    private void setKnobEnd(int x)
    {
        minEnd = x;
        boxEnd = new Rectangle(x, 
                            GraphicSlider.BS2-GraphicSlider.KH2, 
                            GraphicSlider.KNOB_WITH, 
                            GraphicSlider.KNOB_HEIGHT);
    }
    
    /** Convert a value into graphic value between BORDER and BORDER+LENGTH. */
    private int convertRealIntoGraphics(int x)
    {
        return ((GraphicSlider.LENGTH*x)/max+GraphicSlider.BORDER);
    }
    
    /** Convert a graphic coordinate into a value between 0 and max. */
    private int convertGraphicsIntoReal(int x)
    {
        return ((max*x)/GraphicSlider.LENGTH);
    }
    
    /** 
     * Required by I/F but not actually needed in our case, no op 
     * implementation.
     */   
    public void mouseMoved(MouseEvent e) {}
    
    /** 
     * Required by I/F but not actually needed in our case, no op 
     * implementation.
     */    
    public void mouseClicked(MouseEvent e) {}
    
    /** 
     * Required by I/F but not actually needed in our case, no op 
     * implementation.
     */   
    public void mouseEntered(MouseEvent e) {}
    
    /** 
     * Required by I/F but not actually needed in our case, no op 
     * implementation.
     */    
    public void mouseExited(MouseEvent e) {}
    
}
