/*
 * org.openmicroscopy.shoola.agents.roi.pane.ToolBarMng
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

package org.openmicroscopy.shoola.agents.roi.pane;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.defs.ScreenROI;
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;
import org.openmicroscopy.shoola.util.ui.ColoredLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
public class ToolBarMng
    implements ActionListener
{
    
    /** Action command ID. */
    private static final int        CREATE_ROI = 0, ERASE_ROI = 1, 
                                    ASSISTANT = 2, ROI_SELECTION = 3,
                                    ROI_VIEWER = 4;
    
    private ToolBar                 view;
    
    private ROIAgtCtrl              control;
    
    private AssistantDialog         assistant;
    
    private ROIViewer               viewer;
    
    private int                     maxT, maxZ;
    
    private int                     indexViewer;
    
    public ToolBarMng(ToolBar view, ROIAgtCtrl control, int maxT, int maxZ)
    {
        this.view = view;
        this.control = control;
        this.maxT = maxT;
        this.maxZ = maxZ;
        attachListeners();
    }

    public boolean isViewerOn() 
    {
        boolean b = false;
        if (viewer != null) b = true;
        return b;
    }
    
    public boolean isMoveResize()
    {
        boolean b = false;
        if (assistant != null) b = assistant.moveResizeBox.isSelected();
        return b;
    }
    
    public void setROIImage(BufferedImage img, PlaneArea pa)
    {
        if (viewer != null) viewer.setImage(img, pa);
    }
    
    public void removeCurrentPlane(int z, int t)
    {
        if (assistant != null)
            assistant.manager.setSelectedPlane(z, t, ColoredLabel.NO_SHAPE);
    }
    
    public void setCurrentPlane(int z, int t, int shapeType)
    {
        if (assistant != null)
            assistant.manager.setSelectedPlane(z, t, shapeType);
    }
    
    void refreshDialogs(int index)
    {
        if (assistant != null)
            assistant.buildComponent(control.getScreenROI());
        if (viewer != null) {
            if (index != indexViewer) {
                indexViewer = index;
                viewer.setWidgetName(index);
                viewer.resetMagnificationFactor();
                viewer.setImage(control.getROIImage(), control.getClip());
            }
        }
    }
    
    /** Handle events fired by buttons. */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            switch (index) {
                case CREATE_ROI:
                    control.createROI5D(); break;
                case ERASE_ROI:
                    handleErase(); break;
                case ASSISTANT:
                    showAssistant(); break; 
                case ROI_SELECTION:
                    handleROISelection(); break;
                case ROI_VIEWER:
                    showViewer();
                
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
    }
    
    private void handleROISelection()
    {
        int index = view.listROI.getSelectedIndex();
        control.setSelectedIndex(index);
    }
    
    /** Bring up the {@link AssistantDialog assistant} widget. */
    private void showAssistant()
    {
        if (assistant == null) 
            assistant = new AssistantDialog(control, maxZ, maxT, 
                                            control.getScreenROI());
        UIUtilities.centerAndShow(assistant);
    }
    
    private void showViewer()
    {
        if (viewer == null) {
            ScreenROI roi = control.getScreenROI();
            indexViewer = roi.getIndex();
            viewer = new ROIViewer(control, indexViewer);
            viewer.setImage(control.getROIImage(), control.getClip());
        } 
        UIUtilities.centerAndShow(viewer);
    }
    
    private void handleErase()
    {
        //Bring up a notification dialog.
        //TO BE MODIFIED
        IconManager im = IconManager.getInstance(control.getRegistry());
        EraseDialog dialog = new EraseDialog(this, control.getReferenceFrame(), 
                            im.getIcon(IconManager.QUESTION));
        dialog.pack();  
        UIUtilities.centerAndShow(dialog);
    }
    
    void eraseROI()
    { 
        control.removeROI5D(); 
        closeDialogs();
    }
    
    private void closeDialogs()
    {
        if (assistant != null) {
            assistant.dispose();
            assistant = null;
        }
        if (viewer != null) {
            viewer.dispose();
            viewer = null;
        }
    }
    
    /** Attach listeners to the GUI components. */
    private void attachListeners()
    {
        view.listROI.addActionListener(this);
        view.listROI.setActionCommand(""+ROI_SELECTION);
        attachButtonListener(view.createROI, CREATE_ROI);
        attachButtonListener(view.eraseCurrentROI, ERASE_ROI);
        attachButtonListener(view.assistant, ASSISTANT);
        attachButtonListener(view.viewer, ROI_VIEWER);
    }

    /** Attach listener to the JButton. */
    private void attachButtonListener(JButton button, int id)
    {
        button.setActionCommand(""+id);
        button.addActionListener(this);
    }
    
}
