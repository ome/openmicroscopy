/*
 * org.openmicroscopy.shoola.agents.roi.editor.ROIEditorMng
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

package org.openmicroscopy.shoola.agents.roi.editor;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.defs.ROIShape;

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
class ROIEditorMng
    implements ActionListener, DocumentListener
{
    
    private static final int SAVE = 0, CANCEL = 1;
    
    private ROIEditor   view;
    
    private ROIShape    roi;
    
    private ROIAgtCtrl  control;
    
    ROIEditorMng(ROIEditor view, ROIAgtCtrl control, ROIShape roi)
    {
        this.view = view;
        this.control = control;
        this.roi = roi;
        attachListeners();
    }
    
    /** Attach the listeners. */
    void attachListeners()
    {
        view.annotationArea.getDocument().addDocumentListener(this);
        //buttons
        view.save.addActionListener(this);
        view.save.setActionCommand(""+SAVE);
        view.cancel.addActionListener(this);
        view.cancel.setActionCommand(""+CANCEL);
    }
    
    /** Handle events fired by buttons. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) { 
                case SAVE:
                    save(); break;
                case CANCEL:
                    cancel(); 
            }
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+index, nfe);
        } 
    }

    /** TextArea event.*/
    public void changedUpdate(DocumentEvent e)
    {
       view.save.setEnabled(true);
    }
    
    /** TextArea event.*/
    public void insertUpdate(DocumentEvent e)
    {
        view.save.setEnabled(true);
        
    }
    
    /** TextArea event.*/
    public void removeUpdate(DocumentEvent e)
    {
        view.save.setEnabled(true); 
    }

    /** Save the annotation. */
    private void save()
    {
        String txt = view.annotationArea.getText();
        roi.setAnnotation(txt);
        if (control.isOnOffAnnotation()) {
            txt = roi.getLabel()+" "+txt;
            control.setAnnotation(txt);
        }
        cancel();
    }
    
    /** Close the window. */
    private void cancel()
    {
        view.setVisible(false);
        view.dispose();
    }
    
}
