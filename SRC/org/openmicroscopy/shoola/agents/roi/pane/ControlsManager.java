/*
 * org.openmicroscopy.shoola.agents.roi.pane.ControlsManager
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgt;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.ROIFactory;

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
class ControlsManager
    implements ActionListener
{
    
    /** Action command ID for the rectangle button. */
    private static final int    RECTANGLE = 0;
    
    /** Action command ID for the ellipse button. */
    private static final int    ELLIPSE = 1;
    
    /** Action command ID for the erase button. */
    private static final int    ERASE = 2;
    
    /** Action command ID for the eraseALL button. */
    private static final int    ERASE_ALL = 3;
    
    /** Action command ID for the moveROI button. */
    private static final int    MOVE_ROI = 4;
    
    /** Action command ID for the sizeROI button. */
    private static final int    SIZE_ROI = 5;
    
    /** Action command ID for the analyse button. */
    private static final int    ANALYSE = 6;
    
    /** Action command ID for the checkBox. */
    private static final int    DRAW_ON_OFF = 7;
    
    /** Action command ID for the checkBox. */
    private static final int    TEXT_ON_OFF = 8;
    
    /** Action command ID for the checkBox. */
    private static final int    ANNOTATION_ON_OFF = 9;
    
    /** Action command ID for the checkBox. */
    private static final int    COLOR = 10;
    
    /** Action command ID for the checkBox. */
    private static final int    CHANNEL = 11;
    
    private static final int    UNDO_ERASE = 12;
    
    private ROIAgtCtrl          control;
    
    private Controls             view;
    
    ControlsManager(Controls view, ROIAgtCtrl control)
    {
        this.view = view;
        this.control = control;
        attachListeners();
    }
    
    private void attachListeners()
    {
        JButton rectangle = view.getRectangle(), ellipse = view.getEllipse(),
                erase = view.getErase(), analyze = view.getAnalyse(),
                moveROI = view.getMoveROI(), sizeROI = view.getSizeROI(),
                eraseAll = view.getEraseAll(), undoErase = view.getUndoErase();
        rectangle.setActionCommand(""+RECTANGLE);
        rectangle.addActionListener(this);
        ellipse.setActionCommand(""+ELLIPSE);
        ellipse.addActionListener(this); 
        erase.setActionCommand(""+ERASE);
        erase.addActionListener(this); 
        eraseAll.setActionCommand(""+ERASE_ALL);
        eraseAll.addActionListener(this); 
        moveROI.setActionCommand(""+MOVE_ROI);
        moveROI.addActionListener(this); 
        sizeROI.setActionCommand(""+SIZE_ROI);
        sizeROI.addActionListener(this);
        analyze.setActionCommand(""+ANALYSE);
        analyze.addActionListener(this);
        undoErase.setActionCommand(""+UNDO_ERASE);
        undoErase.addActionListener(this);
        
        //ComboBox
        JComboBox colorsBox = view.getColors(), 
                channelsBox = view.getChannels();
        colorsBox.addActionListener(this);
        colorsBox.setActionCommand(""+COLOR);
        channelsBox.addActionListener(this);
        channelsBox.setActionCommand(""+CHANNEL);
        //Box
        JCheckBox drawOnOff = view.getDrawOnOff(), 
                    textOnOff = view.getTextOnOff(), 
                    annotationOnOff = view.getAnnotationOnOff();
        drawOnOff.addActionListener(this);
        drawOnOff.setActionCommand(""+DRAW_ON_OFF);
        textOnOff.addActionListener(this);
        textOnOff.setActionCommand(""+TEXT_ON_OFF);
        annotationOnOff.addActionListener(this);
        annotationOnOff.setActionCommand(""+ANNOTATION_ON_OFF);
    }

    /** Handle events fired by buttons. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case RECTANGLE:
                    setType(ROIFactory.RECTANGLE, true); break;
                case ELLIPSE:
                    setType(ROIFactory.ELLIPSE, false); break;
                case ERASE:
                    control.erase(); break;
                case ERASE_ALL:
                    control.eraseAll(); break;
                case MOVE_ROI:
                    handleState(ROIAgt.MOVING, true); break;
                case SIZE_ROI:
                    handleState(ROIAgt.RESIZING, false); break;
                case DRAW_ON_OFF:
                    handleOnOffDrawing(e); break;
                case TEXT_ON_OFF:
                    handleOnOffText(e); break;
                case ANNOTATION_ON_OFF:
                    handleOnOffAnnotation(e); break;    
                case COLOR:
                    handleColor(e); break;
                case CHANNEL:
                    handleChannel(e); break;
                case ANALYSE:
                    control.analyse(); break;
                case UNDO_ERASE:
                    control.undoErase();
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
    }
    
    private void handleState(int state, boolean b)
    {
        paintedStateButtons(b);
        control.setState(state);
    }
    
    private void handleChannel(ActionEvent e)
    {
        JComboBox box = (JComboBox) e.getSource();
        control.setChannelIndex(box.getSelectedIndex());
    }
    
    private void handleColor(ActionEvent e)
    {
        JComboBox box = (JComboBox) e.getSource();
        control.setLineColor(view.getColorSelected(box.getSelectedIndex()));
    }
    
    private void handleOnOffDrawing(ActionEvent e)
    {
        JCheckBox box = (JCheckBox) e.getSource();
        control.onOffDrawing(box.isSelected());
    }
    
    private void handleOnOffText(ActionEvent e)
    {
        JCheckBox box = (JCheckBox) e.getSource();
        control.onOffText(box.isSelected());
    }
    
    private void handleOnOffAnnotation(ActionEvent e)
    {
        JCheckBox box = (JCheckBox) e.getSource();
        control.onOffAnnotation(box.isSelected());
    }
    
    private void setType(int type, boolean b)
    {
        paintedDrawingButtons(b);
        control.setType(type);
        control.setState(ROIAgt.CONSTRUCTING);
    }


    private void paintedStateButtons(boolean b)
    {
       view.getRectangle().setBorderPainted(false);
       view.getEllipse().setBorderPainted(false);
       view.getMoveROI().setBorderPainted(b);
       view.getSizeROI().setBorderPainted(!b);
    }
    
    private void paintedDrawingButtons(boolean b)
    {
       view.getRectangle().setBorderPainted(b);
       view.getEllipse().setBorderPainted(!b);
       view.getMoveROI().setBorderPainted(false);
       view.getSizeROI().setBorderPainted(false);
    }
    
}
