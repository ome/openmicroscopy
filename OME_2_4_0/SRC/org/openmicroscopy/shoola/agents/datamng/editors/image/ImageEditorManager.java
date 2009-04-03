/*
 * org.openmicroscopy.shoola.agents.datamng.editors.image.ImageEditorManager
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

package org.openmicroscopy.shoola.agents.datamng.editors.image;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.env.data.model.ImageData;

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
class ImageEditorManager
	implements ActionListener, DocumentListener,  MouseListener
{
	
	/** ID used to handle events. */
	private static final int	SAVE = 0;	
	private static final int	CANCEL = 1;
	
	private ImageData			model;
	private ImageEditor			view;
	private DataManagerCtrl 	control;
	
	private boolean				nameChange, isName;
	
	ImageEditorManager(ImageEditor view, DataManagerCtrl control,
						ImageData model)
	{
		this.view = view;
		this.control = control;
		this.model = model;
		nameChange = false;
		isName = false;
	}
	
	ImageData getImageData() { return model; }

	/** Initializes the listeners. */
	void initListeners()
	{
        attachButtonListener(view.getSaveButton(), SAVE);
		attachButtonListener(view.getCancelButton(), CANCEL);
		JTextArea nameField = view.getNameField();
		nameField.getDocument().addDocumentListener(this);
		nameField.addMouseListener(this);
		JTextArea descriptionArea = view.getDescriptionArea();
		descriptionArea.getDocument().addDocumentListener(this);
	}
	
    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = -1;
		try {
            index = Integer.parseInt(e.getActionCommand());
			switch (index) { 
				case SAVE:
					save(); break;
				case CANCEL:
					cancel();
			}// end switch  
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		} 
	}
	
	/** Save changes in DB. */
	private void save()
	{
		model.setDescription(view.getDescriptionArea().getText());
		model.setName(view.getNameField().getText());
		control.updateImage(model, nameChange);
		view.dispose();
	}
	
	/** Close the widget, doesn't save changes. */
	private void cancel()
	{
		view.setVisible(false);
		view.dispose();
	}
	
	/** Require by I/F. */
	public void changedUpdate(DocumentEvent e)
    { 
        view.getSaveButton().setEnabled(true); 
    }

	/** Require by I/F. */
	public void insertUpdate(DocumentEvent e)
	{
		view.getSaveButton().setEnabled(isName);
	}
	
	/** Require by I/F. */
	public void removeUpdate(DocumentEvent e)
	{
        view.getSaveButton().setEnabled(isName);
	}
	
	/** Indicates that the name has been modified. */
	public void mousePressed(MouseEvent e) { isName = true; }

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

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */ 
	public void mouseReleased(MouseEvent e){}
	
}	
	
