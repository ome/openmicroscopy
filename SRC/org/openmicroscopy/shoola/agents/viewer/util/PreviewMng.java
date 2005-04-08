/*
 * org.openmicroscopy.shoola.agents.viewer.util.PreviewMng
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

package org.openmicroscopy.shoola.agents.viewer.util;



//Java imports
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies

/** 
 * Manager of the {@link Preview} widget.
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
class PreviewMng
    implements ActionListener
{
    
    /** Action command ID. */
    private static final int    SAVE = 0, CANCEL = 1, SELECTION = 2, 
                                TEXT = 3, COLOR = 4;
    
    private ImageSaverMng       mng;
    
    private Preview             view;
    
    private boolean             modified;
    
    PreviewMng(Preview view, ImageSaverMng mng)
    {
        this.mng = mng;
        this.view = view;
        modified = false;
    }
    
    /** Attach listener to components. */
    void attachListeners()
    {
        attachButtonListener(view.save, SAVE);
        attachButtonListener(view.cancel, CANCEL);
        attachBoxListener(view.colors, COLOR);
        attachBoxListener(view.selections, SELECTION);
        attachTextListener(view.nameField, TEXT);
    }
    
    /** Attach listener to a {@link JComboBox}. */
    private void attachBoxListener(JComboBox box, int id)
    {
        box.addActionListener(this);
        box.setActionCommand(""+id);
    }
    
    /** Attach listener to a {@link JTextField}. */
    private void attachTextListener(JTextField field, int id)
    {
        field.addActionListener(this);
        field.setActionCommand(""+id);
    }
    
    /** Attach listener to a {@link JButton}. */
    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }

    /** Handle event fired by buttons. */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            switch (index) {
                case SAVE:
                    savePreviewImage(); break;
                case CANCEL:
                    mng.cancelPreviewSaveImage(); break;
                case TEXT:
                case SELECTION:
                case COLOR:
                    refreshPreview();
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
    }
    
    private void savePreviewImage()
    {
        if (modified) {
            BufferedImage img = view.canvas.getDisplayImage();
            mng.savePreviewImage(img);  
        } else mng.savePreviewImage();  
    }
    
    /** Refresh the preview image. */
    private void refreshPreview()
    {
        modified = true;
        String txt = view.nameField.getText();
        int index = view.selections.getSelectedIndex();
        Color color = view.getColor(view.colors.getSelectedIndex());
        view.canvas.paintTextOnImage(txt, index, color);
    }
    
}
