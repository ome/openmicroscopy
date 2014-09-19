/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ColorModelAction
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.imviewer.actions;




//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Selects the color model i.e. <code>GreyScale</code>, <code>RGB</code>
 * or <code>HSB</code>.
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
     * The description of the action if the index is {@link #RGB_MODEL}
     * or  {@link #HSB_MODEL}.
     */
	public static final String 	DESCRIPTION_RGB = "Switch between color " +
    										"and monochrome.";
    
    /** 
     * The description of the action if the index is {@link #GREY_SCALE_MODEL}.
     */
    public static final String 	DESCRIPTION_GREY_SCALE = 
    						"Switch between color and monochrome.";
    
    /** Identifies the <code>Grey Scale</code>. */
    public static final int     GREY_SCALE_MODEL = 0;
    
    /** Identifies the <code>Grey Scale</code>. */
    public static final int     RGB_MODEL = 1;
    
    /** Identifies the <code>Grey Scale</code>. */
    public static final int     HSB_MODEL = 2;
    
    /** The maximum number of supported model. */
    private static final int    MAX = 2;

    /**
     * The name of the association corresponding to the constants defined
     * above.
     */
    private static String[] names;
    
    /** Defines the static fields. */
    static {
        names = new String[MAX+1];
        names[GREY_SCALE_MODEL] = "Grayscale";
        names[RGB_MODEL] = "Color";
        names[HSB_MODEL] = "HSB/HSV"; 
    }
    
    /** The index of the model. One of the constants defined by this class. */
    private int modelIndex;
    
    /**
     * Disposes and closes the movie player when the {@link ImViewer} is
     * discarded.
     * @see ViewerAction#onStateChange(ChangeEvent)
     */
    protected void onStateChange(ChangeEvent e)
    {
    	setEnabled(model.getState() == ImViewer.READY);
    }
    
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
     * Returns the icon corresponding to the specified index.
     * 
     * @param index The index. 
     * @return See above.
     */
    private Icon getColorModelIcon(int index)
    {
        IconManager icons = IconManager.getInstance();
        switch (index) {
            case GREY_SCALE_MODEL:
                default:
                return icons.getIcon(IconManager.GRAYSCALE);
            case RGB_MODEL:
                return icons.getIcon(IconManager.RGB);
            case HSB_MODEL:
                return icons.getIcon(IconManager.RGB);
        }
    }
    
    /**
     * Returns the description of the action depending on the specified
     * index.
     * 
     * @param index The index.
     * @return See above.
     */
    private String getDescription(int index)
    {
    	switch (index) {
	        case GREY_SCALE_MODEL:
	            default:
	            return DESCRIPTION_GREY_SCALE;
	        case RGB_MODEL:    
	        case HSB_MODEL:
	            return DESCRIPTION_RGB;
    	}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the model.
     *                      Mustn't be <code>null</code>.
     * @param modelIndex    The index of the action.
     *                      One of the constants defined by this class.
     */
    public ColorModelAction(ImViewer model, int modelIndex)
    {
        super(model, NAME);
        controlIndex(modelIndex);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(getDescription(modelIndex)));
        this.modelIndex = modelIndex;
        putValue(Action.NAME, names[modelIndex]);
        putValue(Action.SMALL_ICON, getColorModelIcon(modelIndex));
        name = names[modelIndex];
    }
    
    /**
     * Returns the index of the color model.
     * 
     * @return See above.
     */
    public int getIndex() { return modelIndex; }
    
    /** 
     * Sets the selected color model.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        model.setColorModel(modelIndex);
    }
    
}
