/*
 * org.openmicroscopy.shoola.agents.annotator.pane.AnnotationTableMng
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

package org.openmicroscopy.shoola.agents.annotator.pane;




//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

//Third-party libraries

//Application-internal dependenciess
import org.openmicroscopy.shoola.agents.annotator.AnnotatorCtrl;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;

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
class AnnotationTableMng
    implements ActionListener, DocumentListener
{
    
    private AnnotationTable view;
    private AnnotatorCtrl control;
    
    AnnotationTableMng(AnnotationTable view, AnnotatorCtrl control)
    {
        this.view = view;
        this.control = control;
    }
    
    void attachAreaListener(JTextArea area, int id)
    {
        area.getDocument().addDocumentListener(this);
        area.getDocument().putProperty("name", ""+id);

    }
    
    /** Attach listener to a JButton. */
    void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }

    /** Handle events fired by the buttons. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        if (view.creation) control.viewImage();
        else {
            AnnotationData data = view.getAnnotationData(index);
            control.viewImage(data.getTheZ(), data.getTheT());
        }
    }

    /** Required by  the {@link DocumentListener interface}. */
    public void changedUpdate(DocumentEvent e)
    {
        handleTextChange(e);
    }

    /** Required by  the {@link DocumentListener interface}. */
    public void insertUpdate(DocumentEvent e)
    {
        handleTextChange(e);
    }

    /** Required by  the {@link DocumentListener interface}. */
    public void removeUpdate(DocumentEvent e)
    {
        handleTextChange(e);
    }
    
    private void handleTextChange(DocumentEvent e)
    {
        Document doc = e.getDocument();
        try {
            int index = Integer.parseInt((String) (doc.getProperty("name")));
            AnnotationData data = view.getAnnotationData(index);
            JTextArea area = view.getArea(index);
            data.setAnnotation(area.getText());
        } catch (Exception ex) {
            // TODO: handle exception
        } 
    }
    
}
