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
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since 2.2
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
     * Indicates that the browser is in some sort of default mode.  (This will
     * probably stay.)
     */
    public static final BrowserMode DEFAULT_MODE = new BrowserMode(0);

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
