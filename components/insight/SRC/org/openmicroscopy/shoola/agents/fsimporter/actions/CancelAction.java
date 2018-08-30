/*
 * org.openmicroscopy.shoola.agents.fsimporter.actions.CancelAction 
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

import javax.swing.SwingUtilities;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Cancels the on-going import.
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
public class CancelAction 
	extends ImporterAction
{

	/** The description of the action. */
    private static final String NAME = "Cancel All";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Cancel the imports that" +
    		" have not yet started.";
    
	/** 
	 * Sets the enabled flag depending on the state.
	 * @see #onStateChange()
	 */
    protected void onStateChange()
    {
    	/*
    	if (model.getState() == Importer.IMPORTING) {
    		setEnabled(!model.isLastImport());
    	} else setEnabled(false);
    	setEnabled(true);
    	*/
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public CancelAction(Importer model)
    {
        super(model);
        setEnabled(true);
        putValue(Action.NAME, NAME);
        putValue(Action.SHORT_DESCRIPTION,
                UIUtilities.formatToolTipText(DESCRIPTION));
    }
    
    /**
     * Cancels the on-going import.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) { 
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // This might cause the UI to freeze for a while if there
                // are thousands of imports to cancel, hence wrapped into 
                // SwingUtilities.invokeLater
                model.cancelAllImports(); 
            }
        });
    }

}
