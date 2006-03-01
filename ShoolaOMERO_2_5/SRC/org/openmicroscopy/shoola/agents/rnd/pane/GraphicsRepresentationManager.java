/*
 * org.openmicroscopy.shoola.agents.rnd.pane.GraphicsRepresentationManager
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

package org.openmicroscopy.shoola.agents.rnd.pane;

//Java imports
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;

/** 
 * Handles events fired the graphical cursors drawn in
 * {@link GraphicsRepresentation}.
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
class GraphicsRepresentationManager
    implements MouseListener, MouseMotionListener
{

    /** Graphic constants. */
    private static final int        topBorder = 
                                        GraphicsRepresentation.topBorder, 
                                    leftBorder = 
                                        GraphicsRepresentation.leftBorder,
                                    square = GraphicsRepresentation.square, 
                                    bottomBorder = 
                                        GraphicsRepresentation.bottomBorder,
                                    bottomBorderSupp = 
                                    GraphicsRepresentation.bottomBorderSupp,
                                    triangleW = 
                                        GraphicsRepresentation.triangleW, 
                                    lS = leftBorder+square, 
                                    tS = topBorder+square,
                                    length = 2*triangleW, 
                                    absMin = leftBorder+20;
    
    private static final int        extraControl = 2;
    
    /** Used to control mouse pressed and dragged events. */                    
    private boolean                 dragging;

    private boolean                 equalStartAndEnd;
    
    /** Rectangles used to listen the knobs. */
    private Rectangle               boxStart, boxEnd, boxOutputStart,
                                    boxOutputEnd;
    
    /** controls' bounds. */
    private int                     maxStartX, minEndX, maxEndX, 
                                    maxStartOutputY, minEndOutputY;
    
    /** Reference to the main control {@link QuantumPaneManager}. */
    private QuantumPaneManager      control;

    /** Reference to the view. */
    private GraphicsRepresentation  view;
    
    /** 
     * Family selected, one of the constants defined by {@link QuantumFactory}. 
     */
    private int                     type;
    
    private int                     curRealValue;
    
    /** Controls to determine which knob has been selected. */
    private boolean                 inputStartKnob, inputEndKnob, 
                                    outputStartKnob, outputEndKnob;

    GraphicsRepresentationManager(GraphicsRepresentation view, 
                                    QuantumPaneManager control, int type)
    {
        this.view = view;
        this.control = control;
        this.type = type;
        boxStart = new Rectangle();
        boxEnd = new Rectangle();
        boxOutputStart = new Rectangle();
        boxOutputEnd = new Rectangle();
        maxEndX = leftBorder+square/2; //only used if type = exponential
        attachListeners();
        setBooleanControls(false, false, false, false);
    }

    /** Attach listeners. */
    private void attachListeners()
    {
        view.addMouseListener(this);
        view.addMouseMotionListener(this);
    }

    void setEqualStartAndEnd(boolean b) { equalStartAndEnd  = b; }
    /**
     * Resize the input window.
     * The method is invoked by the control {@link QuantumPaneManager}.
     * 
     * @param value real input value.
     */
    void setInputWindowStart(int v, int min, int max)
    {
        int vg = convertRealIntoGraphics(v, max-min, 
                                        view.getInputGraphicsRange(), min);
        vg = vg + leftBorder;
        setInputStartBox(vg);
        view.updateInputStart(vg, v);   
    }

    /**
     * Resize the input window.
     * The method is invoked by the control {@link QuantumPaneManager}.
     * 
     * @param v real input value.
     */
    void setInputWindowEnd(int v, int min, int max)
    {
        int vg = convertRealIntoGraphics(v, max-min, 
                                        view.getInputGraphicsRange(), min);
        vg = vg + leftBorder;
        setInputEndBox(vg);
        view.updateInputEnd(vg, v);
    }
    
    /** Set the sizes and location of the input rectangles. */
    void setInputRectangles(int start, int end)
    {
        setInputStartBox(start);
        setInputEndBox(end);
    }
    
    /** Set the sizes and location of the output rectangles. */
    void setOutputRectangles(int start, int end)
    {
        setOutputStartBox(start);
        setOutputEndBox(end);
    }
    
    /** 
     * Converts a graphic value into a real value 
     * (equation of the form y = ax+b).
     * The rangeReal & rangeGraphics values are used to 
     * compute a; b is a parameter.
     *
     * @param x             x-coordinate (graphic).
     * @param rangeReal     real range. 
     * @param rangeGraphics graphic range.
     * @param b             equation's parameter.
     * @return a real value 
     */    
    int convertGraphicsIntoReal(int x, int rangeReal, int rangeGraphics, int b)
    {
        double a = (double) rangeReal/rangeGraphics;
        int r = (int) (a*x+b);
        int c = control.getGlobalMinimum();
        int d = control.getGlobalMaximum();
        if (r < c) r = c;
        if (r > d) r = d;
        return r;
    }

    int convertGraphicsIntoReal(int x)
    {
            double a = (double) 255/square;
            return (int) (255-a*x);
    }
    /** 
     * Converts a real value into a graphic value 
     * (equation of the form y = ax+b).
     * The rangeReal & rangeGraphics values are used to compute a;
     * b is a parameter.
     *
     * @param x             x-coordinate (real)
     * @param rangeReal     real range. 
     * @param rangeGraphics graphic range.
     * @param b             equation's parameter
     * @return a graphic value
     */    
    int convertRealIntoGraphics(int x, int rangeReal, int rangeGraphics, int b)
    {
        double a = (double) rangeGraphics/rangeReal;
        return (int) (a*(x-b));
    }

    /** Handles events fired the knobs. */
    public void mousePressed(MouseEvent e)
    {
        Point p = e.getPoint();
        if (!dragging && !equalStartAndEnd) {
            dragging = true;
            if (boxEnd.contains(p) && p.x >= leftBorder && p.x <= lS && 
                p.x >= maxStartX && p.x <= maxEndX && p.x >= absMin 
                && type == QuantumFactory.EXPONENTIAL) 
                handleBoxEndExponential(p.x);      
            if (boxEnd.contains(p) && p.x >= leftBorder && p.x <= lS &&
                p.x >= maxStartX && type != QuantumFactory.EXPONENTIAL)
                handleBoxEnd(p.x);
            if (boxStart.contains(p) && p.x >= leftBorder && p.x <= lS &&
                p.x <= minEndX)
                handleBoxStart(p.x);
            if (boxOutputStart.contains(p) && p.y >= minEndOutputY &&
                p.y <= tS)
                handleBoxOutputStart(p.y); 
            if (boxOutputEnd.contains(p) && p.y <= maxStartOutputY &&
                p.y >= topBorder)
                handleBoxOutputEnd(p.y);   
         }  //else dragging already in progress 
    }

    /** Handles events fired the knobs. */    
    public void mouseDragged(MouseEvent e)
    {
        Point p = e.getPoint();
        if (dragging && !equalStartAndEnd) {
            if (boxEnd.contains(p) && p.x >= leftBorder && p.x <= lS && 
                p.x >= maxStartX && p.x <= maxEndX && p.x >= absMin 
                && type == QuantumFactory.EXPONENTIAL)
                handleBoxEndExponential(p.x);  
            if (boxEnd.contains(p) && p.x >= leftBorder && p.x <= lS &&
                p.x >= maxStartX && type != QuantumFactory.EXPONENTIAL)
                handleBoxEnd(p.x);
            if (boxStart.contains(p) && p.x >= leftBorder && p.x <= lS &&
                p.x <= minEndX)
                handleBoxStart(p.x);  
            if (boxOutputStart.contains(p) && p.y >= minEndOutputY 
                && p.y <= tS)
                handleBoxOutputStart(p.y);
            if (boxOutputEnd.contains(p) && p.y <= maxStartOutputY && 
                p.y >= topBorder)
                handleBoxOutputEnd(p.y);  
        }
    }
    
    /** 
     * Resets the dragging control to false and 
     * fire an event to render the image.
     */    
    public void mouseReleased(MouseEvent e)
    { 
        if (!equalStartAndEnd) {
            if (inputStartKnob) 
                control.setChannelWindowStart(curRealValue);
            else if (inputEndKnob) 
                control.setChannelWindowEnd(curRealValue);
            else if (outputStartKnob) 
                control.setCodomainLowerBound(curRealValue);
            else if (outputEndKnob) 
                control.setCodomainUpperBound(curRealValue);
            dragging = false; 
            setBooleanControls(false, false, false, false);
        }
    }
    
    /** Handle the BoxEnd events if family == EXPONENTIAL. */
    private void handleBoxEndExponential(int x)
    {
        int min = control.getGlobalMinimum();
        curRealValue = convertGraphicsIntoReal(x-leftBorder, 
                        control.getGlobalMaximum()-min, 
                        view.getInputGraphicsRange(), min); 
        setBooleanControls(false, true, false, false);
        control.setInputWindowEnd(curRealValue);
    }
    
    /** Handle the BoxEnd events if family != EXPONENTIAL. */
    private void handleBoxEnd(int x)
    {
        int min = control.getGlobalMinimum();
        curRealValue = convertGraphicsIntoReal(x-leftBorder, 
                        control.getGlobalMaximum()-min, 
                        view.getInputGraphicsRange(), min);
        setBooleanControls(false, true, false, false);
        control.setInputWindowEnd(curRealValue); 
    }
    
    /** Handle the BoxStart events. */
    private void handleBoxStart(int x)
    {
        int min = control.getGlobalMinimum();
        curRealValue = convertGraphicsIntoReal(x-leftBorder, 
                        control.getGlobalMaximum()-min, 
                        view.getInputGraphicsRange(), min);
        setBooleanControls(true, false, false, false);
        control.setInputWindowStart(curRealValue); 
    }
    
    /** Handle the BoxOutputStart events. */
    private void handleBoxOutputStart(int y)
    {
        curRealValue = convertGraphicsIntoReal(y-topBorder);          
        setBooleanControls(false, false, true, false);
        setOutputWindowStart(y);  
    }
    
    /** Handle the BoxOutputEnd events. */
    private void handleBoxOutputEnd(int y)
    {
        curRealValue =  convertGraphicsIntoReal(y-topBorder);         
        setBooleanControls(false, false, false, true);
        setOutputWindowEnd(y);
    }
    
    private void setBooleanControls(boolean a, boolean b, boolean c, boolean d)
    {
        inputStartKnob = a;
        inputEndKnob = b;
        outputStartKnob = c;
        outputEndKnob = d;
    }
    
    /**
     * Resize the output window.
     * 
     * @param y     graphics value.
     */
    void setOutputWindowStart(int y)
    {
        setOutputStartBox(y);
        view.updateOutputStart(y);
    }

    /**
     * Resize the output window.
     * 
     * @param value     graphics value.
     */
    void setOutputWindowEnd(int y)
    {
        setOutputEndBox(y);
        view.updateOutputEnd(y);
    }

    /** 
     * Set the type. 
     *
     * @param t     family index.
     * @param x     MaxEndX value.
     */
    void setType(int type, int x)
    {
        this.type = type;
        maxEndX = x;
    }

    /** 
     * Set the MaxEndX value that is used to control the knobs.
     *
     * @param x value.
     */    
    void setMaxEndX(int x) { maxEndX = x; }
    
    /** 
     * Sizes the rectangle used to listen to the outpuStart knob.
     *
     * @param y     y-coordinate.
     */ 
    private void setOutputStartBox(int y)
    {
        maxStartOutputY = y-extraControl;
        boxOutputStart.setBounds(0, y-2*triangleW, leftBorder, 4*length);
    }

    /** 
     * Sizes the rectangle used to listen to the outputEnd cursor.
     *
     * @param y     y-coordinate.
     */  
    private void setOutputEndBox(int y)
    {
        minEndOutputY = y+extraControl;
        boxOutputEnd.setBounds(lS, y-triangleW, leftBorder, 4*length);
    }

    /** 
     * Sizes the rectangle used to listen to the inputStart cursor.
     *
     * @param x     x-coordinate.
     */
    void setInputStartBox(int x)
    {
        maxStartX = x+extraControl;
        boxStart.setBounds(x-triangleW, tS+triangleW+1, 4*length, 
                            bottomBorder+bottomBorderSupp);
    }
    
    /** 
     * Sizes the rectangle used to listen to the inputEnd cursor.
     *
     * @param x     x-coordinate.
     */  
    void setInputEndBox(int x)
    {
        minEndX = x-extraControl;
        boxEnd.setBounds(x-2*triangleW, 0, 4*length, topBorder);
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
