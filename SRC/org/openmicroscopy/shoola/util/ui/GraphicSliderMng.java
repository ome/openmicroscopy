/*
 * org.openmicroscopy.shoola.util.ui.GraphicSliderMng
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.openmicroscopy.shoola.util.ui.events.ChangeEventSlider;

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
    
    private static final int    MIN = GraphicSliderUIF.BORDER,
                                MAX = GraphicSliderUIF.BL;
    
    private int                 max;
    
    private Rectangle           boxStart, boxEnd;

    private boolean             dragging;
    
    private int                 maxStart, minEnd;
    
    private int                 curStartValue, curEndValue;
    
    private GraphicSliderUIF    presentation;
    
    private GraphicSlider       abstraction;
    
    /** A list of event listeners for this component. */
    private EventListenerList listenerList = new EventListenerList();
    
    private ChangeEventSlider changeEvent = null;
    
    GraphicSliderMng(GraphicSlider abstraction, GraphicSliderUIF presentation,
                        int max, int s, int e)
    {
        this.max = max;
        this.presentation = presentation;
        this.abstraction = abstraction;
        init(s, e);
    }
    
    /** 
     * Add a {@link ChangeListener} to the list of listeners.
     * 
     * @param l {@link ChangeListener} to be added.
     */
    void addChangeListener(ChangeListener l)
    {
        listenerList.add(ChangeListener.class, l);
    }

    /** 
     * Remove a {@link ChangeListener} from the list of listeners.
     * 
     * @param l {@link ChangeListener} to be removed.
     */
    void removeChangeListener(ChangeListener l)
    {
        listenerList.remove(ChangeListener.class, l);
    } 
    
    /** Attach Mouse listeners. */
    void attachMouseListeners()
    {
        presentation.addMouseListener(this);
        presentation.addMouseMotionListener(this);
    }
    
    /** Remove mouse listeners. */
    void removeMouseListeners()
    {
        presentation.removeMouseListener(this);
        presentation.removeMouseMotionListener(this);
    }
    
    int getStartValue() { return curStartValue; }
    
    int getEndValue() { return curEndValue; }
    
    /** 
     * Set the location of the start knob.
     * 
     * @param v     value in the range 0, max.
     */
    void setStartValue(int v)
    {
        int gv = MIN;
        if (max != 0) gv = convertRealIntoGraphics(v);
        positionStartKnob(gv);
    }
    
    /** 
     * Set the location of the end knob.
     * 
     * @param v     value in the range 0, max.
     */
    void setEndValue(int v)
    {
        int gv = MAX;
        if (max != 0) gv = convertRealIntoGraphics(v);
        positionEndKnob(gv);
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
      
    /** Init the different controls. */
    private void init(int s, int e)
    {
        curStartValue = 0;
        curEndValue = max;
        boxStart = new Rectangle();
        boxEnd = new Rectangle();
        setStartValue(s);
        setEndValue(e);
    }
    
    /** Fire a ChangeEventSlider. */
    private void fireStateChanged(boolean s, boolean e)
    {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null)
                    changeEvent = new ChangeEventSlider(abstraction);
                changeEvent.setStart(s);
                changeEvent.setEnd(e);
                ((ChangeListener) listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }  
    
    /** Convert a value into graphic value between BORDER and BORDER+LENGTH. */
    private int convertRealIntoGraphics(int x)
    {
        return ((GraphicSliderUIF.LENGTH*x)/max+GraphicSliderUIF.BORDER);
    }
    
    /** Convert a graphic coordinate into a value between 0 and max. */
    private int convertGraphicsIntoReal(int x)
    {
        return ((max*x)/GraphicSliderUIF.LENGTH);
    }
    
    /** Handle the dragging of the startKnob. */
    private void handleKnobStart(int x)
    {
        curStartValue = convertGraphicsIntoReal(x-GraphicSliderUIF.BORDER-1);
        fireStateChanged(true, false);
        //mpMng.setMovieStart(curRealValue);
        positionStartKnob(x);
    }
    
    /** Set the location of the start knob. */
    private void positionStartKnob(int x)
    {
        x -= GraphicSliderUIF.KW2;
        setKnobStart(x);
        presentation.setKnobStart(x); 
        presentation.repaint();
    }
    
    /** Handle the dragging of the endKnob. */
    private void handleKnobEnd(int x)
    {  
        curEndValue = convertGraphicsIntoReal(x-GraphicSliderUIF.BORDER+1);
        fireStateChanged(false, true);
        //mpMng.setMovieEnd(curRealValue);
        positionEndKnob(x);
    }
    
    /** Set the location of the end knob. */
    private void positionEndKnob(int x)
    {
        x -= GraphicSliderUIF.KW2;
        setKnobEnd(x);
        presentation.setKnobEnd(x); 
        presentation.repaint();
    }
    
    /** Set the bounds of the rectangle controlling the startKnob. */
    private void setKnobStart(int x)
    {
        maxStart = x+GraphicSliderUIF.KNOB_WITH;
        boxStart = new Rectangle(x, GraphicSliderUIF.BS2-GraphicSliderUIF.KH2, 
                      GraphicSliderUIF.KNOB_WITH, GraphicSliderUIF.KNOB_HEIGHT);
    }
    
    /** Set the bounds of the rectangle controlling the endKnob. */
    private void setKnobEnd(int x)
    {
        minEnd = x;
        boxEnd = new Rectangle(x, GraphicSliderUIF.BS2-GraphicSliderUIF.KH2, 
                    GraphicSliderUIF.KNOB_WITH, GraphicSliderUIF.KNOB_HEIGHT);
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
