/*
 * org.openmicroscopy.shoola.agents.imviewer.util.ContrastStretchingDialog
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import javax.swing.JFrame;


//Third-party libraries


//Application-internal dependencies
import ome.model.display.CodomainMapContext;
import ome.model.display.ContrastStretchingContext;
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;


/** 
 * Modal dialog to set the setting of the <code>Contrast Stretching</code>
 * transformation.
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
public class ContrastStretchingDialog
    extends CodomainMapContextDialog
    implements MouseListener, MouseMotionListener
{

    /** The window's title. */
    private static final String     TITLE = "Contrast Stretching settings";
    
    /** Description of the dialog's action. */
    private static final String     TEXT = "Increase the dynamic " +
                                            "range of the gray levels.";
    
    /** Indicates that the input start knob has been selected. */
    private static final int        INPUT_START = 0;
    
    /** Indicates that the input end knob has been selected. */
    private static final int        INPUT_END = 1;
    
    /** Indicates that the output start knob has been selected. */
    private static final int        OUTPUT_START = 2;
    
    /** Indicates that the output end knob has been selected. */
    private static final int        OUTPUT_END = 3;
    
    /** Flag to handle mouse pressed and dragged events. */                
    private boolean                 dragging;
    
    /** Rectangle that controls the input Start knob mouse events.*/
    private Rectangle               boxStart;
    
    /** Rectangle that controls the input End knob mouse events.*/
    private Rectangle               boxEnd;
    
    /** Rectangle that controls the output Start knob mouse events.*/
    private Rectangle               boxOutputStart;
    
    /** Rectangle that controls the output End knob mouse events.*/
    private Rectangle               boxOutputEnd;
                                        
    /** Maximum value for the input start knob. */                                
    private int                     maxStartX;
    
    /** Minimum value for the input end knob. */
    private int                     minEndX;
    
    /** Maximum value for the output start knob. */
    private int                     maxStartOutputY;
    
    /** Minimum value for the output end knob. */
    private int                     minEndOutputY;
    
    /**
     * Indicates the selected knob. One of the following constant:
     * {@link #INPUT_START}, {@link #INPUT_END}, {@link #OUTPUT_START} or
     * {@link #OUTPUT_END}.
     */
    private int                     knobIndex;
    
    /** The current value. */
    private int                     curValue;
    
    /** The component hosting the settings UI controls. */
    private ContrastStretchingUI    uiDelegate;
    
    /** 
     * Adds a {@link MouseMotionListener} and {@link MouseListener} 
     * to the {@link #uiDelegate}.
     */
    private void attachListeners()
    {
        uiDelegate.addMouseListener(this);
        uiDelegate.addMouseMotionListener(this);
    }
    
    /** 
     * Converts a real value into its corresponding graphical value. 
     * 
     * @param x
     * @param r
     * @param b
     * @return See above.
     */
    private int convertGraphicsIntoReal(int x, int r, int b)
    {
        double a = (double) r/ContrastStretchingUI.square;
        return (int) (a*x+b);
    }
    /** 
     * Converts a graphical value into its corresponding real value. 
     * 
     * @param x
     * @param r
     * @param b
     * @return See above.
     */
    private int convertRealIntoGraphics(int x, int r, int b)
    {
        double a = (double) ContrastStretchingUI.square/r;
        return (int) (a*(x-b));
    }
    
    /** 
     * Resizes the rectangle which controls the input End knob.
     *
     * @param x The x-coordinate.
     */
    private void setInputStartBox(int x)
    {
        maxStartX = x+ContrastStretchingUI.triangleW;
        boxStart.setBounds(x-ContrastStretchingUI.triangleW,
                ContrastStretchingUI.tS, 2*ContrastStretchingUI.triangleW,
                ContrastStretchingUI.bottomBorder);
    }
    
    /** 
     * Resizes the rectangle which controls the input End knob.
     *
     * @param x The x-coordinate.
     */
    private void setInputEndBox(int x)
    {
        minEndX = x-ContrastStretchingUI.triangleW;
        boxEnd.setBounds(x-ContrastStretchingUI.triangleW, 
                ContrastStretchingUI.tS, 2*ContrastStretchingUI.triangleW, 
                ContrastStretchingUI.bottomBorder);
    }
    
    /** 
     * Resizes the rectangle which controls the output Start knob.
     *
     * @param y The y-coordinate.
     */
    private void setOutputStartBox(int y)
    {
        maxStartOutputY = y-ContrastStretchingUI.triangleW;
        boxOutputStart.setBounds(0, y-ContrastStretchingUI.triangleW, 
               ContrastStretchingUI.leftBorder-ContrastStretchingUI.triangleW-1, 
               2*ContrastStretchingUI.triangleW);
    }
    
    /** 
     * Resizes the rectangle which controls the output End knob.
     *
     * @param y The y-coordinate.
     */
    private void setOutputEndBox(int y)
    {
        minEndOutputY = y+ContrastStretchingUI.triangleW;
        boxOutputEnd.setBounds(0, y-ContrastStretchingUI.triangleW, 
               ContrastStretchingUI.leftBorder-ContrastStretchingUI.triangleW-1, 
               2*ContrastStretchingUI.triangleW);
    }
    
    /**
     * Modifies the x-coordinate of the control start point.
     * 
     * @param x The graphic coordinate.
     */
    private void setInputStart(int x)
    {
        uiDelegate.updateStartKnob(x);
        curValue = convertGraphicsIntoReal(x-ContrastStretchingUI.leftBorder, 
                                        cdEnd-cdStart, cdStart); 
    }
    
    /**
     * Modifies the x-coordinate of the control endPoint.
     * 
     * @param x The graphic coordinate.
     */
    private void setInputEnd(int x)
    {
        uiDelegate.updateEndKnob(x);
        curValue = convertGraphicsIntoReal(x-ContrastStretchingUI.leftBorder,
                                        cdEnd-cdStart, cdStart);
    }
    
    /**
     * Modifies the y-coordinate of the control startPoint.
     * 
     * @param y The graphic coordinate.
     */
    private void setOutputStart(int y)
    {
        uiDelegate.updateStartOutputKnob(y);
        curValue = convertGraphicsIntoReal(y-ContrastStretchingUI.topBorder,
                                            cdStart-cdEnd, cdEnd);
    }
    
    /** 
     * Modifies the y-coordinate of the control end point.
     * 
     * @param y The graphic coordinate.
     */
    private void setOutputEnd(int y)
    {
        uiDelegate.updateEndOutputKnob(y);
        curValue = convertGraphicsIntoReal(y-ContrastStretchingUI.topBorder, 
                                            cdStart-cdEnd, cdEnd);
    }
    
    /** Initializes the components. */
    private void initialize()
    {
        int xStart, xEnd, yStart, yEnd;
        int diff = cdEnd-cdStart;
        ContrastStretchingContext csCtx = (ContrastStretchingContext) ctx;
        xStart = ContrastStretchingUI.leftBorder+convertRealIntoGraphics(
                csCtx.getXstart().intValue(), diff, cdStart);
        xEnd = ContrastStretchingUI.leftBorder+convertRealIntoGraphics(
                csCtx.getXend().intValue(), diff, cdStart);
        yStart = ContrastStretchingUI.topBorder+convertRealIntoGraphics(
                csCtx.getYstart().intValue(), -diff, cdEnd);
        yEnd = ContrastStretchingUI.topBorder+convertRealIntoGraphics(
                csCtx.getYend().intValue(), -diff, cdEnd);
        uiDelegate = new ContrastStretchingUI(xStart, xEnd, yStart, yEnd); 
        setOutputEndBox(yEnd);
        setOutputStartBox(yStart);
        setInputStartBox(xStart);
        setInputEndBox(xEnd);
        attachListeners();
    }
    
    /** 
     * Builds the component hosting the window's information.
     * @see CodomainMapContextDialog#buildTitlePane()
     */
    protected JComponent buildTitlePane()
    {
        return new TitlePanel(TITLE, TEXT, NOTE, 
                icons.getIcon(IconManager.CONTRAST_STRETCHING_BIG));
    }
    
    /** 
     * Builds the component hosting the settings controls.
     * @see CodomainMapContextDialog#buildBody()
     */
    protected JComponent buildBody() { return uiDelegate; }

    /**
     * Sets the settings. 
     * @see CodomainMapContextDialog#updateContext()
     */
    protected void updateContext()
    {
        ContrastStretchingContext csCtx = (ContrastStretchingContext) ctx;
        //set values depending on the knob index.
        switch (knobIndex) {
            case INPUT_START:
                csCtx.setXstart(new Integer(curValue));
                break;
            case INPUT_END:   
                csCtx.setXend(new Integer(curValue));
                break;
            case OUTPUT_START:
                csCtx.setYstart(new Integer(curValue));
                break;
            case OUTPUT_END:
                csCtx.setYend(new Integer(curValue));
        }
    }

    /** Sets the window's title. */
    protected void setWindowTitle() { setTitle(TITLE); }

    /**
     * Creates a new instance.
     * 
     * @param owner     The owner of the dialog.
     * @param ctx       The codomain map context this dialog is for.
     * @param cdEnd     The upper bound of the codomain interval.
     * @param cdStart   The lower bound of the codomain interval.
     */
    public ContrastStretchingDialog(JFrame owner, CodomainMapContext ctx,
            int cdEnd, int cdStart)
    {
        super(owner, ctx, cdEnd, cdStart);
        initialize();
        buildGUI();
    }

    /**
     * Handles events fired the cursors. 
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent e)
    {
        Point p = e.getPoint();
        if (!dragging) { 
            dragging = true; 
            if (boxStart.contains(p) && p.x >= ContrastStretchingUI.leftBorder
               && p.x <= ContrastStretchingUI.lS && p.x <= minEndX) {
                knobIndex = INPUT_START;
                setInputStartBox(p.x);
                setInputStart(p.x); 
            }
            if (boxEnd.contains(p) && p.x >= ContrastStretchingUI.leftBorder && 
                p.x <= ContrastStretchingUI.lS && p.x >= maxStartX) {
                knobIndex = INPUT_END;
                setInputEndBox(p.x);   
                setInputEnd(p.x); 
            }   
            if (boxOutputStart.contains(p) && p.y >= minEndOutputY
                && p.y <= ContrastStretchingUI.tS) {
                knobIndex = OUTPUT_START;
                setOutputStartBox(p.y);
                setOutputStart(p.y);    
            }
            if (boxOutputEnd.contains(p) && p.y <= maxStartOutputY
                && p.y >= ContrastStretchingUI.topBorder) {
                knobIndex = OUTPUT_END;
                setOutputEndBox(p.y);
                setOutputEnd(p.y);  
            }   
        }
    }
    
    /** 
     * Handles events fired the cursors. 
     * @see MouseMotionListener#mouseDragged(MouseEvent)
     */
    public void mouseDragged(MouseEvent e)
    {
        Point p = e.getPoint();
        if (dragging) {  
            if (boxStart.contains(p) && p.x >= ContrastStretchingUI.leftBorder
                 &&  p.x <= ContrastStretchingUI.lS && p.x <= minEndX) {
                setInputStartBox(p.x);
                setInputStart(p.x); 
            }
            if (boxEnd.contains(p) && p.x >= ContrastStretchingUI.leftBorder &&
                p.x <= ContrastStretchingUI.lS && p.x >= maxStartX) {
                setInputEndBox(p.x);
                setInputEnd(p.x); 
            }
            if (boxOutputStart.contains(p) && p.y >= minEndOutputY
                && p.y <= ContrastStretchingUI.tS) {
                setOutputStartBox(p.y);
                setOutputStart(p.y);
            }
            if (boxOutputEnd.contains(p) && p.y <= maxStartOutputY
                && p.y >= ContrastStretchingUI.topBorder) {
                setOutputEndBox(p.y);
                setOutputEnd(p.y);
            }     
        }
    }
    
    /** 
     * Resets the dragging control and sets the settings.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent e)
    { 
        dragging = false;
        apply();
    }

    /** 
     * Required by {@link MouseMotionListener} I/F but not actually needed in
     * our case, no op implementation. 
     * @see MouseMotionListener#mouseMoved(MouseEvent)
     */   
    public void mouseMoved(MouseEvent e) {}
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in
     * our case, no op implementation. 
     * @see MouseListener#mouseClicked(MouseEvent)
     */  
    public void mouseClicked(MouseEvent e) {}
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in
     * our case, no op implementation. 
     * @see MouseListener#mouseEntered(MouseEvent)
     */    
    public void mouseEntered(MouseEvent e) {}
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in
     * our case, no op implementation. 
     * @see MouseListener#mouseExited(MouseEvent)
     */ 
    public void mouseExited(MouseEvent e) {}
    
}
