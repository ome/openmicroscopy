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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

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
	private static final int	SAVE = 0;
	private static final int	CANCEL = 1;
	
	/** Save Button displayed in {@link ChannelEditorBar}. */
	private JButton				saveButton;
    
	private RenderingAgtCtrl 	eventManager;
	
	private ChannelData 		model;
	private ChannelEditor 		view;
	
	ChannelEditorManager(RenderingAgtCtrl eventManager, ChannelEditor view, 
						ChannelData model)
	{
		this.eventManager = eventManager;
		this.view = view;
		this.model = model;
	}
	
	ChannelData getChannelData() { return model; }
	
	/** Attach the listeners. */
	void attachListeners()
	{
		saveButton = view.getSaveButton();
        attachButtonListener(saveButton, SAVE);
        attachButtonListener(view.getCancelButton(), CANCEL);
		//text area.
        attachDocumentListener(view.getInterpretationArea());
        attachDocumentListener(view.getExcitation());
        attachDocumentListener(view.getFluor());
        attachDocumentListener(view.getNDFilter());
        attachDocumentListener(view.getAuxLightAttenuation());
        attachDocumentListener(view.getDetectorGain());
        attachDocumentListener(view.getDetectorOffset());
        attachDocumentListener(view.getLightAttenuation());
        attachDocumentListener(view.getAuxLightWavelength());
        attachDocumentListener(view.getPinholeSize());
        attachDocumentListener(view.getLightWavelength());
        attachDocumentListener(view.getSamplesPerPixel());
        attachDocumentListener(view.getAuxTechnique());
        attachDocumentListener(view.getContrastMethod());
        attachDocumentListener(view.getMode());
        attachDocumentListener(view.getIlluminationType());
	}
    
	public void actionPerformed(ActionEvent e)
	{
		try {
            int index = Integer.parseInt(e.getActionCommand());
			switch (index) { 
				case SAVE:
					save(); break;
				case CANCEL:
					cancel(); break;
			}  
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+e.getActionCommand(), nfe);
		} 
	}

	/** Require by {@link DocumentListener} I/F. */
	public void changedUpdate(DocumentEvent e) { saveButton.setEnabled(true); }

	/** Require by {@link DocumentListener} I/F. */
	public void insertUpdate(DocumentEvent e) { saveButton.setEnabled(true); }

	/** Require by {@link DocumentListener} I/F. */
	public void removeUpdate(DocumentEvent e) { saveButton.setEnabled(true); }
	
	/** Attach a DocumentListener to the specified component. */
    private void attachDocumentListener(JTextComponent component)
    {
        component.getDocument().addDocumentListener(this);
    }
    
    /** Attach an actionListener to the specified JButton. */
    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
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
        //string
        model.setInterpretation(view.getInterpretationArea().getText());
        model.setFluor(view.getFluor().getText());
        model.setMode(view.getMode().getText());
        model.setAuxTechnique(view.getAuxTechnique().getText());
        model.setContrastMethod(view.getContrastMethod().getText());
        model.setIlluminationType(view.getIlluminationType().getText());
        
        //int
        try {
            model.setExcitation(
                    Integer.parseInt(view.getExcitation().getText()));
        } catch (Exception e) {}// Do nothing
        try {
            model.setAuxLightWavelength(
                    Integer.parseInt(view.getAuxLightWavelength().getText()));
        } catch (Exception e) {}
        try {
            model.setLightWavelength(
                    Integer.parseInt(view.getLightWavelength().getText()));
        } catch (Exception e) {}
        try {
            model.setPinholeSize(
                    Integer.parseInt(view.getPinholeSize().getText()));
        } catch (Exception e) {}
        try {
            model.setSamplesPerPixel(
                    Integer.parseInt(view.getSamplesPerPixel().getText()));
        } catch (Exception e) {}
       
        
        
        //float
        try {
            model.setLightAttenuation(
                    Float.parseFloat(view.getLightAttenuation().getText()));
        } catch (Exception e) {}
        try {
            model.setNDFilter(
                    Float.parseFloat(view.getNDFilter().getText()));
        } catch (Exception e) {}
        try {
            model.setAuxLightAttenuation(
                    Float.parseFloat(view.getAuxLightAttenuation().getText()));
        } catch (Exception e) {}
        try {
            model.setDetectorGain(
                    Float.parseFloat(view.getDetectorGain().getText()));
        } catch (Exception e) {}
        try {
            model.setDetectorOffset(
                    Float.parseFloat(view.getDetectorOffset().getText()));
        } catch (Exception e) {}
        eventManager.updateChannelData(model);
        view.dispose();
    }
    
}
