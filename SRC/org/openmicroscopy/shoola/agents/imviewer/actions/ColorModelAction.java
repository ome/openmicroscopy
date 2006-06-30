/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ColorModelAction
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
public class ColorModelAction
    extends ViewerAction
{

    /**
     * Bounds property indicating that a new color model is selected.
     */
    public static final String  COLOR_MODEL_PROPERTY = "colorModel";
    
    /** Identifies the <code>Grey Scale</code>. */
    public static final int     GREY_SCALE_MODEL = 0;
    
    /** Identifies the <code>Grey Scale</code>. */
    public static final int     RGB_MODEL = 1;
    
    /** Identifies the <code>Grey Scale</code>. */
    public static final int     HSB_MODEL = 2;
    
    /** The maximum number of supported model. */
    private static final int    MAX = 2;
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Select a color model.";

    /**
     * The name of the association corresponding to the constants defined
     * above.
     */
    private static String[] names;
    
    static {
        names = new String[MAX+1];
        names[GREY_SCALE_MODEL] = "Grey Scale";
        names[RGB_MODEL] = "RGB";
        names[HSB_MODEL] = "HSB/HSV"; 
    }
    
    /** The index of the model. One of the constants defined by this class. */
    private int modelIndex;
    
    /**
     * Checks if the passed index is supported.
     * 
     * @param index The index to check.
     */
    private void controlIndex(int index)
    {
        switch (index) {
            case GREY_SCALE_MODEL:
            case RGB_MODEL:
            case HSB_MODEL:
                return;
            default:
                throw new IllegalArgumentException("Model not supported.");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model         The model. Mustn't be <code>null</code>.
     * @param modelIndex    The index of the action.
     *                      One of the constants defined by this class.
     */
    public ColorModelAction(ImViewer model, int modelIndex)
    {
        super(model, NAME);
        controlIndex(modelIndex);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        this.modelIndex = modelIndex;
        putValue(Action.NAME, UIUtilities.formatToolTipText(names[modelIndex]));
        setName(names[modelIndex]);
    }
    
    /** 
     * TODO: command to execute the action.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        firePropertyChange(COLOR_MODEL_PROPERTY, null, this);
        model.setColorModel(modelIndex);
    }
    
}
