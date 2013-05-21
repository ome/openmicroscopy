/*
 * org.openmicroscopy.shoola.agents.fsimporter.actions.SubmitFilesAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Action to submit files to the development team.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class SubmitFilesAction 
	extends ImporterAction
{

	/** The name of the action. */
	private static final String NAME = "Submit All";
	
    /** The description of the action. */
    private static final String DESCRIPTION = "Submit the files that failed " +
    		"to import to the development team.";

    /**
     * Sets the <code>enabled</code> flag depending on the state.
     * @see ImporterAction#onStateChange()
     */
    protected void onStateChange()
    {
    	if (model.getState() == Importer.IMPORTING) {
    		setEnabled(false);
    	} else {
    		setEnabled(model.hasFailuresToSend());
    	}
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	public SubmitFilesAction(Importer model)
	{
		super(model);
		setEnabled(false);
		putValue(Action.NAME, NAME);
		putValue(Action.SHORT_DESCRIPTION,
				UIUtilities.formatToolTipText(DESCRIPTION));
	}
	
	/**
     * Submit the files to the QA system.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) { model.submitFiles(); }

}
