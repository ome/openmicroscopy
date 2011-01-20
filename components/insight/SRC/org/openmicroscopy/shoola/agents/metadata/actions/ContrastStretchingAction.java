/*
 * org.openmicroscopy.shoola.agents.metadata.actions.ContrastStretchingAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JCheckBox;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Adds or removes the <code>Contrast stretching</code> codomain 
 * transformations to the codomain chain.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ContrastStretchingAction    
	extends RndAction
{

    /** The name of the action. */
    private static final String NAME = "Contrast Stretching";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "";

    /**
     * Creates a new instance.
     * 
     * @param model The {@link Renderer} model. Mustn't be <code>null</code>.
     */
    public ContrastStretchingAction(Renderer model)
    {
        super(model);
        setEnabled(false);
        putValue(Action.NAME, NAME);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
    }
    
    /**
     * Adds or removes the <code>Contrast stretching</code> codomain 
     * transformations to the codomain chain.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if (source instanceof JCheckBox) {
            //boolean b = ((JCheckBox) source).isSelected();
            //if (b) model.addCodomainMap(ContrastStretchingContext.class);
            //else model.removeCodomainMap(ContrastStretchingContext.class);
        }    
    }
}
