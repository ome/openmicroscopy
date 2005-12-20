/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.FilterAction
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

package org.openmicroscopy.shoola.agents.treeviewer.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class FilterAction
    extends BrowserAction
{

    /** Indicates that the container is of type <code>Dataset</code>.*/
    public static final int     DATASET_CONTAINER = 0;
    
    /** Indicates that the container is of type <code>Category</code>.*/
    public static final int     CATEGORY_CONTAINER = 1;
    
    /**
     * Name of the action if the {@link #type} is
     * {@link #DATASET_CONTAINER}.
     */
    private static final String NAME_DATASET = "Images in datasets";
    
    /**
     * Description of the action if the {@link #type} is
     * {@link #DATASET_CONTAINER}.
     */
    private static final String DESCRIPTION_DATASET = "Retrieve images " +
            "contained in the selected datasets.";
    
    /**
     * Name of the action if the {@link #type} is
     * {@link #CATEGORY_CONTAINER}.
     */
    private static final String NAME_CATEGORY = "Images in categories";
    
    /**
     * Description of the action if the {@link #type} is
     * {@link #CATEGORY_CONTAINER}.
     */
    private static final String DESCRIPTION_CATEGORY = "Retrieve images " +
            "contained in the selected categories.";
    
    /** One of the constants defined by this class. */
    private int type;
    
    /**
     * Checks that the specified type is supported.
     * 
     * @param type The type to check.
     */
    private void checkType(int type)
    {
        switch (type) {
            case DATASET_CONTAINER:
            case CATEGORY_CONTAINER:    
                break;
            default:
                throw new IllegalArgumentException("Type not supported");
        }
    }
    
    /** 
     * Sets the name and the description of this action according to 
     * the selected type.
     */
    private void setValues()
    {
        switch (type) {
            case DATASET_CONTAINER:
                putValue(Action.NAME, NAME_DATASET);
                putValue(Action.SHORT_DESCRIPTION, 
                        UIUtilities.formatToolTipText(DESCRIPTION_DATASET));
                break;
            case CATEGORY_CONTAINER:  
                putValue(Action.NAME, NAME_CATEGORY);
                putValue(Action.SHORT_DESCRIPTION, 
                        UIUtilities.formatToolTipText(DESCRIPTION_CATEGORY));
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param type  One of the constants defined by this class.
     */
    public FilterAction(Browser model, int type)
    {
        super(model);
        checkType(type);
        this.type = type;
        setValues();
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.FILTER));  
        setEnabled(model.getBrowserType() == Browser.IMAGES_EXPLORER);
    }
    
    /** Creates a  command to execute the action. */
    public void actionPerformed(ActionEvent e)
    { 
        model.loadFilterData(type);
    }
    
}
