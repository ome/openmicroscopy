/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.RateImageAction
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
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
public class RateImageAction
    extends ViewerAction
{
    
    /**
     * Bounds property indicating that a new level is selected.
     */
    public static final String  RATE_IMAGE_PROPERTY = "rateImage";
    
    /** Identifies the <code>first</code> rating level. */
    public static final int     RATE_ONE = 0;
    
    /** Identifies the <code>second</code> rating level. */
    public static final int     RATE_TWO = 1;
     
    /** Identifies the <code>third</code> rating level. */
    public static final int     RATE_THREE = 2;
    
    /** Identifies the <code>fourth</code> rating level. */
    public static final int     RATE_FOUR = 3;
    
    /** Identifies the <code>fifth</code> rating level. */
    public static final int     RATE_FIVE = 4;
    
    /** The number of supported ids. */
    private static final int    MAX = 4;
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Rate the image.";

    /** The array containing the actions' name. */
    private static String[]     names;
    
    static {
        names = new String[MAX+1];
        names[RATE_ONE] = "*";
        names[RATE_TWO] = "**";
        names[RATE_THREE] = "***";
        names[RATE_FOUR] = "****";
        names[RATE_FIVE] = "*****";
    }
    
    /** 
     * The index of the rating action. One of the contants defined by
     * this class.
     */
    private int ratingIndex;
    
    /**
     * Checks if the passed index is supported.
     * 
     * @param index The index to control.
     */
    private void controlsIndex(int index)
    {
        switch (index) {
            case RATE_ONE:
            case RATE_TWO:
            case RATE_THREE:
            case RATE_FOUR:
            case RATE_FIVE:
                return;
            default:
                throw new IllegalArgumentException("Rating index not " +
                        "supported.");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model         The model. Mustn't be <code>null</code>.
     * @param ratingIndex   The index of the rating action.
     *                      One of the constants defined by this class.
     */
    public RateImageAction(ImViewer model, int ratingIndex)
    {
        super(model);
        setEnabled(true);
        controlsIndex(ratingIndex);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        this.ratingIndex = ratingIndex;
        putValue(Action.NAME, 
                UIUtilities.formatToolTipText(names[ratingIndex]));
        setName(names[ratingIndex]);
    }
    
    /** 
     * TODO: command to execute the action.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        firePropertyChange(RATE_IMAGE_PROPERTY, null, this);
        model.setRateImage(ratingIndex);
    }

}
