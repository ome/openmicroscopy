/*
 * org.openmicroscopy.shoola.agents.metadata.actions.ColorModelAction 
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
package org.openmicroscopy.shoola.agents.metadata.actions;



//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Selects the color model i.e. <code>GreyScale</code> or <code>RGB</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ColorModelAction 
	extends RndAction
	implements PropertyChangeListener
{

	 /** The description of the action if the index is {@link #RGB_MODEL}. */
	public static final String 	DESCRIPTION_RGB = "Switch between color " +
    										"and monochrome.";
    
    /** 
     * The description of the action if the index is {@link #GREY_SCALE_MODEL}.
     */
    public static final String 	DESCRIPTION_GREY_SCALE = 
    						"Switch between color and monochrome.";
    
    /** Identifies the <code>Grey Scale</code>. */
    private static final int     GREY_SCALE_MODEL = 0;
    
    /** Identifies the <code>Grey Scale</code>. */
    private static final int     RGB_MODEL = 1;

    /** Helper reference to the icon manager. */
    private IconManager icons;
   
    /** 
     * Sets the details of the action i.e. name, icon and description.
     * 
     * @param index One of the constants defined by this class.
     */
    private void setActionDetails(int index)
    {
    	switch (index) {
	    	case GREY_SCALE_MODEL:
	    	default:
	    		putValue(Action.SHORT_DESCRIPTION, 
	    				UIUtilities.formatToolTipText(DESCRIPTION_GREY_SCALE));
	    		putValue(Action.SMALL_ICON, 
	    				icons.getIcon(IconManager.GRAYSCALE));
	    		break;
	    	case RGB_MODEL:
	    		putValue(Action.SHORT_DESCRIPTION, 
	    				UIUtilities.formatToolTipText(DESCRIPTION_RGB));
	    		putValue(Action.SMALL_ICON, icons.getIcon(IconManager.RGB));
    	}
    }
    
    /** Sets the details depending on the color model. */
    private void handleColorModel()
    {
    	String c = model.getColorModel();
    	if (Renderer.GREY_SCALE_MODEL.equals(c)) 
    		setActionDetails(GREY_SCALE_MODEL);
    	else if (Renderer.RGB_MODEL.equals(c)) 
    		setActionDetails(RGB_MODEL);
    }
    
	/**
     * Creates a new instance.
     * 
     * @param model	Reference to the model. Mustn't be <code>null</code>.
     */
    public ColorModelAction(Renderer model)
    {
        super(model);
        setEnabled(true);
        icons = IconManager.getInstance();
        handleColorModel();
        model.addPropertyChangeListener(this);
    }
    
    /** 
     * Sets the selected color model.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	String c = model.getColorModel();
    	if (Renderer.GREY_SCALE_MODEL.equals(c)) 
    		model.setColorModel(Renderer.RGB_MODEL, true);
    	else if (Renderer.RGB_MODEL.equals(c)) 
    		model.setColorModel(Renderer.GREY_SCALE_MODEL, true);
    }

    /**
     * Listens to property change to set the icon, name and description
     * of the action.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (Renderer.COLOR_MODEL_PROPERTY.equals(name)) handleColorModel();
	}
    
}
