/*
 * org.openmicroscopy.shoola.agents.viewer.transform.zooming.ZoomBarManager
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

package org.openmicroscopy.shoola.agents.viewer.transform.zooming;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.JButton;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspector;
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspectorManager;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * 
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
public class ZoomBarManager
    implements ActionListener, FocusListener
{

    /** Action command ID. */
    private static final int        ZOOM_FIELD_CMD = 0;
    private static final int        ZOOM_IN_CMD = 1;
    private static final int        ZOOM_OUT_CMD = 2;
    private static final int        ZOOM_FIT_CMD = 3;
    
    /** current zooming level. */
    private double                  zoomLevel;
    
    private NumberFormat            percentFormat;
    private JTextField              zoomField;
    private ZoomBar                 view;

    private ImageInspectorManager   control;
    
    public ZoomBarManager(ZoomBar view, ImageInspectorManager control, 
                            double magFactor)
    {
        zoomLevel = magFactor;
        percentFormat = NumberFormat.getPercentInstance();
        this.view = view;
        this.control = control;
    }
    
    /** Attach listeners. */
    void attachListeners()
    {
        //textfield
        zoomField = view.getZoomField();
        zoomField.setActionCommand(""+ZOOM_FIELD_CMD);  
        zoomField.addActionListener(this);
        zoomField.addFocusListener(this);
        
        //button
        JButton zoomIn = view.getZoomIn(), zoomOut = view.getZoomOut(), 
                zoomFit = view.getZoomFit();
        zoomIn.addActionListener(this);
        zoomIn.setActionCommand(""+ZOOM_IN_CMD);
        zoomOut.addActionListener(this);
        zoomOut.setActionCommand(""+ZOOM_OUT_CMD); 
        zoomFit.addActionListener(this);
        zoomFit.setActionCommand(""+ZOOM_FIT_CMD);
    }
    
    /** Handle event fired by button. */
    public void actionPerformed(ActionEvent e) 
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case ZOOM_FIELD_CMD:
                    zoomFieldActionHandler(); break;
                case ZOOM_IN_CMD:
                    zoomIn(); break;
                case ZOOM_OUT_CMD:
                    zoomOut(); break;
                case ZOOM_FIT_CMD:
                    zoomFit(); break;
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe);
        }
    }

    /** Set the value. */
    public void setText(double level)
    {
        zoomField.setText(percentFormat.format(level));
    }
    
    /** Make image bigger. */
    private void zoomIn()
    {
        if (zoomLevel < ImageInspector.MAX_ZOOM_LEVEL) 
            zoomLevel += ImageInspector.ZOOM_INCREMENT;
        control.setZoomLevel(zoomLevel);
    }

    /** Make image smaller.*/
    private void zoomOut()
    {
        if (zoomLevel > ImageInspector.MIN_ZOOM_LEVEL) 
            zoomLevel -= ImageInspector.ZOOM_INCREMENT;
        control.setZoomLevel(zoomLevel);
    }
    
    /** Reset the image. */
    private void zoomFit()
    {
        zoomLevel = ImageInspector.ZOOM_DEFAULT;
        control.setZoomLevel(zoomLevel);
    }
    
    /** 
     * Handles the action event fired by the zooming text field when the 
     * user enters some text. If the entered text can be converted to a valid 
     * zoomlevel, the image is updated. 
     */  
    private void zoomFieldActionHandler()
    {
        boolean valid = false;
        double val = 0;
        try {
            val = percentFormat.parse(zoomField.getText()).doubleValue();
            if (ImageInspector.MIN_ZOOM_LEVEL <= val && 
                val <= ImageInspector.MAX_ZOOM_LEVEL) {
                valid = true;
            } else if (val < ImageInspector.MIN_ZOOM_LEVEL) {
                val = ImageInspector.MIN_ZOOM_LEVEL;
                valid = true;
            } else if (val > ImageInspector.MAX_ZOOM_LEVEL) {
                val = ImageInspector.MAX_ZOOM_LEVEL;
                valid = true;
            }
        } catch(NumberFormatException nfe) {}
        catch(ParseException e) {}
        
        if (valid) synchZoom(val);  
        else {
            zoomField.selectAll();
            UserNotifier un = view.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid input value", 
                "Please enter a value between 25% and 300%.");
        }
    }

    /** Synchronizes the zooming and the textField. */
    private void synchZoom(double value)
    {
        zoomLevel = value;
        control.setZoomLevel(zoomLevel);
    }

    /** 
     * Handles the lost of focus on the timepoint text field.
     * If focus is lost while editing, then we don't consider the text 
     * currently displayed in the text field and we reset it to the current
     * timepoint.
     */
    public void focusLost(FocusEvent e)
    {
        String val = zoomField.getText();
        String cVal = percentFormat.format(zoomLevel);
        if (val == null || !val.equals(cVal))  zoomField.setText(cVal);
    }
    
    /** 
     * Required by I/F but not actually needed in our case, no op 
     * implementation.
     */
    public void focusGained(FocusEvent e) {}

}
