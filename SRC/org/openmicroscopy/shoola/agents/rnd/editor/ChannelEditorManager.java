/*
 * org.openmicroscopy.shoola.agents.rnd.model.WavelengthEditorManager
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

package org.openmicroscopy.shoola.agents.rnd.editor;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.RenderingAgtCtrl;
import org.openmicroscopy.shoola.env.data.model.ChannelData;

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
class ChannelEditorManager
	implements ActionListener, DocumentListener
{
	
	/** Action command ID, handle events. */
	private static final int	SAVE = 100;
	private static final int	CANCEL = 101;
	
	/** Save Button displayed in {@link ChannelEditorBar}. */
	private JButton				saveButton;
	
	/** Cancel Button displayed in {@link ChannelEditorBar}. */
	private JButton				cancelButton;
	
	/** textArea displayed in the {@link ChannelPane}. */
	private JTextArea			interpretationArea;
	
	/** textField displayed in the {@link ChannelPane}. */	
	private JTextField			excitation;
	
	/** textField displayed in the {@link ChannelPane}. */	
	private JTextField			fluor;
	
	private RenderingAgtCtrl 	eventManager;
	
	private ChannelData 		model;
	private ChannelEditor 		view;
	
	ChannelEditorManager(RenderingAgtCtrl eventManager, ChannelEditor view, 
						ChannelData model)
	{
		this.eventManager = eventManager;
		this.view = view;
		this.model =model;
	}
	
	ChannelData getChannelData() { return model; }
	
	/** Attach the listeners. */
	void attachListeners()
	{
		saveButton = view.getSaveButton();
		saveButton.addActionListener(this);
		saveButton.setActionCommand(""+SAVE);
		cancelButton = view.getCancelButton();
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		//text area.
		interpretationArea = view.getInterpretationArea();
		interpretationArea.getDocument().addDocumentListener(this);
		excitation = view.getExcitation();
		excitation.getDocument().addDocumentListener(this);
		fluor = view.getFluor();
		fluor.getDocument().addDocumentListener(this);
	}

	public void actionPerformed(ActionEvent e)
	{
		int index = -1;
		try {
            index = Integer.parseInt(e.getActionCommand());
			switch (index) { 
				case SAVE:
					save(); break;
				case CANCEL:
					cancel(); break;
			}  
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		} 
	}
	
	/** Close the widget, doesn't save changes. */
	private void cancel()
	{
		view.setVisible(false);
		view.dispose();
	}

	/** Save in DB. */
	private void save()
	{
		model.setInterpretation(interpretationArea.getText());
		model.setFluor(fluor.getText());
		int value = Integer.parseInt(excitation.getText());
		model.setExcitation(value);
		eventManager.updateChannelData(model);
		view.dispose();
	}

	/** Require by {@link DocumentListener} I/F. */
	public void changedUpdate(DocumentEvent e) { saveButton.setEnabled(true); }

	/** Require by {@link DocumentListener} I/F. */
	public void insertUpdate(DocumentEvent e) { saveButton.setEnabled(true); }

	/** Require by {@link DocumentListener} I/F. */
	public void removeUpdate(DocumentEvent e) { saveButton.setEnabled(true); }
	
}
