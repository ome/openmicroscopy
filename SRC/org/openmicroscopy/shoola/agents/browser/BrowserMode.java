/*
 * org.openmicroscopy.shoola.agents.browser.BrowserMode
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser;

/**
 * A typedef enum that represents the current viewing of the browser.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserMode
{
    // dummy differentiation variable
    private int value;

    // private (singleton) constructor
    private BrowserMode(int value)
    {
        this.value = value;
    }

    /**
     * Indicates that the browser is in some sort of default mode (universal
     * for all ranges/classes)
     */
    public static final BrowserMode DEFAULT_MODE = new BrowserMode(0);
    
    /**
     * Indicates that the images should be placed in a structured format,
     * either by plate/well number (if that's the type of dataset) or by
     * ascending ID (if it's not)
     */
    public static final BrowserMode ORDERED_MODE = DEFAULT_MODE;
    
    /**
     * Indicates that the user is not using a drag to move the viewport
     * around.  The hand comes from the traditional Apple/Photoshop
     * notion of a hand moving the image around the viewport.
     */
    public static final BrowserMode NO_HAND_MODE = DEFAULT_MODE;

    /**
     * Indicates that the browser is in annotation mode (this might not be
     * a good example of a mode, but it illustrates the usage pattern.)
     */
    public static final BrowserMode ANNOTATE_MODE = new BrowserMode(1);
    
    /**
     * Indicates that the browser is in scalar classification ("heat map")
     * mode.
     */
    public static final BrowserMode HEAT_MAP_MODE = new BrowserMode(2);
    
    /**
     * Indicates that the browser is in categorical classification (by
     * phenotype, etc.) mode.
     */
    public static final BrowserMode CLASSIFY_MODE = new BrowserMode(3);
    
    /**
     * Indicates that the browser is in graphical classification (by
     * shared scalar value) mode.
     */
    public static final BrowserMode GRAPH_MODE = new BrowserMode(4);
    
    /**
     * Indicates that the user can move objects around in the browser as
     * he/she sees fit, no problems.
     */
    public static final BrowserMode PLACE_MODE = new BrowserMode(5);
    
    /**
     * Indicates that panning should be conducted by a mouse drag, which
     * essentially nullifies any other mouse down/drag inputs.  Its opposite
     * is NO_HAND_MODE.  The hand comes from the traditional Apple/Photoshop
     * notion of a hand moving the image around the viewport.
     */
    public static final BrowserMode HAND_MODE = new BrowserMode(6);
    
    /**
     * Indicates that semantic expansion of thumbnails should take place on
     * a mouse over.
     */
    public static final BrowserMode SEMANTIC_ZOOMING_MODE = new BrowserMode(7);
    
    /**
     * Indicates that the name of thumbnails should be displayed/painted.
     */
    public static final BrowserMode NAME_ON_MODE = new BrowserMode(8);
    
    /**
     * Indicates that hints about thumbnail annotations should be
     * displayed/painted.
     */
    public static final BrowserMode ANNOTATION_ON_MODE = new BrowserMode(9);
    
    /**
     * Indicates that nothing is currently selected.
     */
    public static final BrowserMode UNSELECTED_MODE = DEFAULT_MODE;
    
    /**
     * Indicates that a selection is in progress.
     */
    public static final BrowserMode SELECTING_MODE = new BrowserMode(10);
    
    /**
     * Indicates that multiple objects have been selected.
     */
    public static final BrowserMode SELECTED_MODE = new BrowserMode(11);
    
    /**
     * Indicates that the browser should zoom to fit the dataset.
     */
    public static final BrowserMode ZOOM_TO_FIT_MODE = DEFAULT_MODE;
    
    /**
     * Indicates that the browser should zoom to 100%.
     */
    public static final BrowserMode ZOOM_ACTUAL_MODE = new BrowserMode(12);
    
    /**
     * Indicates that the browser should zoom to 50%.
     */
    public static final BrowserMode ZOOM_50_MODE = new BrowserMode(13);
    
    /**
     * Indicates that the browser should zoom to 75%.
     */
    public static final BrowserMode ZOOM_75_MODE = new BrowserMode(14);
    
    /**
     * Indicates that the browser should zoom to 200%.
     */
    public static final BrowserMode ZOOM_200_MODE = new BrowserMode(15);
    
    /**
     * Indicates that mouse-over should trigger display of the image name.
     */
    public static final BrowserMode IMAGE_NAME_MODE = new BrowserMode(16);

    /**
     * Returns the numerical value of the Browser (so that this enum can be
     * used in switch statements)
     */
    public int getValue()
    {
        return value;
    }

    /**
     * Forces equality by reference and not by value.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
        return this == o;
    }

    /**
     * Maintains the equals/hashCode contract.
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return value;
    }
}
