/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.UnitBarSizeAction
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * Increases or decreases the size of the unit bar.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class UnitBarSizeAction
    extends ViewerAction
{

    /** The description of the action. */
    private static final String DESCRIPTION_INCREASE = "Increase the size of " +
            "the Scale bar displayed on top of the image.";
    
    private static final String DESCRIPTION_DECREASE = "Decrease the size of " +
            "the Scale bar displayed on top of the image.";
    
    /** 
     * If <code>true</code> increase the size of the unit bar, 
     * decrease otherwise.
     */
    private boolean increase;
    
    /**
     * Creates a new instance.
     * 
     * @param model     Reference to the model. Mustn't be <code>null</code>.
     * @param increase  Pass <code>true</code> to increase the size of the 
     *                  unit bar, pass <code>false</code> to decrease the size.
     */
    public UnitBarSizeAction(ImViewer model, boolean increase)
    {
        super(model);
        this.increase = increase;
        IconManager icons = IconManager.getInstance();
        if (increase) {
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_INCREASE));
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PLUS));
        } else {
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_DECREASE));
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.MINUS));
        }
    }
    
    /**
     * Increases or decreases the size of the unit bar depending on the 
     * value of {@link #increase}.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        model.setUnitBarSize(increase);
    }
    
}
