/*
 * org.openmicroscopy.shoola.agents.viewer.controls.ToolBarNavigatorMng
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

package org.openmicroscopy.shoola.agents.viewer.controls;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
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
public class ToolBarManager
    implements ActionListener, FocusListener
{
    
    /** Action command ID to be used with the timepoint text field. */
    private static final int                    T_FIELD_CMD = 0;
    
    /** Action command ID to be used with the rewind button. */
    private static final int                    RENDER_CMD = 1;
    
    /** Action command ID to be used with the inspector button. */
    private static final int                    INSPECTOR_CMD = 2;
    
    /** Action command ID to be used with the z-section text field. */
    private static final int                    Z_FIELD_CMD = 3;
    
    /** Action command ID to be used with the saveAs button. */
    private static final int                    SAVEAS_CMD = 4;
    
    /** Action command ID to be used with the viewer3D button. */
    private static final int                    VIEWER3D_CMD = 5;
    
    /** Action command ID to be used with the movie button. */
    private static final int                    MOVIE_CMD = 6;
    
     /** Action command ID to be used with the roi button. */
    private static final int                    ROI_CMD = 7;
    
    private int                                 curT, maxT, curZ, maxZ;
    
    private ViewerCtrl                          control;

    private ToolBar                             view;
    
    public ToolBarManager(ViewerCtrl control, ToolBar view, int sizeT, int t,
                          int sizeZ, int z)
    {
        this.control = control;
        this.view = view;
        maxT = sizeT;
        curT = t;
        maxZ = sizeZ;
        curZ = z;
    }
        
    /** Attach the listeners. */
    void attachListeners()
    {
        //textfield
        JTextField tField = view.getTField(), zField = view.getZField();
        tField.setActionCommand(""+T_FIELD_CMD);  
        tField.addActionListener(this);
        tField.addFocusListener(this);
        zField.setActionCommand(""+Z_FIELD_CMD);  
        zField.addActionListener(this);
        zField.addFocusListener(this);
    
        //button
        JButton render = view.getRender(), inspector = view.getInspector(),
                saveAs = view.getSaveAs(), viewer3D = view.getViewer3D(),
                movie = view.getMovie(), roi = view.getROI();
        
        movie.setActionCommand(""+MOVIE_CMD);
        movie.addActionListener(this);  
        render.setActionCommand(""+RENDER_CMD);
        render.addActionListener(this);
        inspector.setActionCommand(""+INSPECTOR_CMD);
        inspector.addActionListener(this);
        saveAs.setActionCommand(""+SAVEAS_CMD);
        saveAs.addActionListener(this);
        viewer3D.setActionCommand(""+VIEWER3D_CMD);
        viewer3D.addActionListener(this);
        roi.setActionCommand(""+ROI_CMD);
        roi.addActionListener(this);
    }

    public void setMaxZ(int z) { maxZ = z; }
    
    public void setMaxT(int t) { maxT = t; }
    
    /** Update the TField when a new timepoint is selected using the slider. */
    public void onTChange(int t)
    {
        curT = t;
        view.getTField().setText(""+t);
    }
        
    /** Update the ZField when a z-section is selected using the slider. */
    public void onZChange(int z)
    {
        curZ = z;
        view.getZField().setText(""+z);
    }
    
    /** 
     * Handles the action event fired by the timepoint text field when the user 
     * enters some text. 
     * If that text doesn't evaluate to a valid timepoint, then we simply 
     * suggest the user to enter a valid one.
     */
    private void tFieldActionHandler() 
    {
        boolean valid = false;
        int val = 0;
        try {
            val = Integer.parseInt(view.getTField().getText());
            if (0 <= val && val <= maxT) valid = true;
        } catch(NumberFormatException nfe) {}
        if (valid) {
            curT = val;
            control.onTChange(curZ, curT); 
        } else {
            view.getTField().selectAll();
            UserNotifier un = control.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid timepoint", 
                "Please enter a timepoint between 0 and "+maxT);
        }
    }

    /** 
     * Handles the action event fired by the timepoint text field when the user 
     * enters some text. 
     * If that text doesn't evaluate to a valid timepoint, then we simply 
     * suggest the user to enter a valid one.
     */
    private void zFieldActionHandler() 
    {
        boolean valid = false;
        int val = 0;
        try {
            val = Integer.parseInt(view.getZField().getText());
            if (0 <= val && val <= maxZ) valid = true;
        } catch(NumberFormatException nfe) {}
        if (valid) {
            curZ = val;
            control.onZChange(curZ, curT);
        } else {
            view.getZField().selectAll();
            UserNotifier un = control.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid z-section", 
                "Please enter a z-section between 0 and "+maxZ);
        }
    }
    
    /** Handle events fired byt text field and buttons. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case T_FIELD_CMD:
                    tFieldActionHandler(); break;
                case Z_FIELD_CMD:
                    zFieldActionHandler(); break;
                case RENDER_CMD:
                    control.showRendering(); break;
                case INSPECTOR_CMD:
                    control.showInspector(); break;
                case SAVEAS_CMD:
                    control.showImageSaver(); break;
                case VIEWER3D_CMD:
                    control.showImage3DViewer(); break; 
                case MOVIE_CMD:
                    control.showMovie(); break; 
                case ROI_CMD:
                    control.showROI(); break;
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
    }
    
    /** 
     * Handles the lost of focus on the timepoint text field.
     * If focus is lost while editing, then we don't consider the text 
     * currently displayed in the text field and we reset it to the current
     * timepoint.
     */
    public void focusLost(FocusEvent e)
    {
        String tVal = view.getTField().getText(), t = ""+curT;
        String zVal = view.getZField().getText(), z = ""+curZ;
        if (tVal == null || !tVal.equals(t)) view.getTField().setText(t);
        if (zVal == null || !zVal.equals(z)) view.getZField().setText(z);
    }
    
    /** 
     * Required by I/F but not actually needed in our case, no op 
     * implementation.
     */ 
    public void focusGained(FocusEvent e) {}

}
