/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.NoiseReductionAction
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

package org.openmicroscopy.shoola.agents.imviewer.actions;



//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JCheckBox;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.rnd.Renderer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Selects or not the <code>Noise reduction</code> rendering algorithm.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class NoiseReductionAction
    extends RndAction
{

    /** The name of the action. */
    public static final String  NAME = "Noise reduction";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Select or deselect the noise " +
                                                "reduction algorithm";
    
    /**
     * Creates a new instance.
     * 
     * @param model The {@link Renderer} model. Mustn't be <code>null</code>.
     */
    public NoiseReductionAction(Renderer model)
    {
        super(model);
        setEnabled(true);
        putValue(Action.NAME, NAME);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
    }
    
    /**
     * Selects/deselects the <code>Noise reduction</code> algorithm.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if (source instanceof JCheckBox) 
            model.setNoiseReduction(((JCheckBox) source).isSelected());
    }
    
}
