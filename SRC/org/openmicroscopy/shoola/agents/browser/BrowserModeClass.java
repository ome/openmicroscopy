/*
 * org.openmicroscopy.shoola.agents.browser.BrowserModeClass
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
 * Specifies a range of potential browser modes, and which mode within the
 * class of nodes is the selected one.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserModeClass
{
    protected BrowserMode[] options;
    protected BrowserMode selected;
    
    /**
     * Create a browser mode with the specified potential options, and which
     * mode should be initially selected.  If defaultMode is null, or if the
     * mode specified is not within the list of valid options, the first
     * mode will be the default selected.  If options is null or empty, well,
     * you've got a useless class on your hands.
     * 
     * @param options The enumeration of possible modes for this class.
     * @param defaultMode Which node in the class should be selected.
     */
    public BrowserModeClass(BrowserMode[] options,
                            BrowserMode defaultMode)
    {
        if(options == null || options.length == 0)
        {
            this.options = new BrowserMode[0];
            return;
        }
        else // deep copy
        {
            this.options = new BrowserMode[options.length];
            for(int i=0;i<options.length;i++)
            {
                this.options[i] = options[i];
            }
        }
        
        // default check; if null, default is first index
        if(defaultMode == null || !optionCheck(defaultMode))
        {
            selected = this.options[0];
        }
        else
        {
            selected = defaultMode;
        }
    }
    
    /**
     * Makes sure the specified mode is in the list of options
     * @param mode The mode to check.
     * @return True if the mode is within the list of options, false otherwise.
     */
    protected boolean optionCheck(BrowserMode mode)
    {
        for(int i=0;i<options.length;i++)
        {
            if(options[i].equals(mode))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns an array of the potential mode options in this browser class.
     * @return Which modes are valid within this classification of modes.
     */
    public BrowserMode[] getValidModes()
    {
        // deep copy again
        BrowserMode[] copy = new BrowserMode[options.length];
        for(int i=0;i<options.length;i++)
        {
            copy[i] = options[i];
        }
        return copy;
    }
    
    /**
     * Returns the current selected mode in the class.
     * @return See above.
     */
    public BrowserMode getSelected()
    {
        return selected;
    }
    
    /**
     * Sets the current selected mode to the specified mode.  If the mode
     * is not within the valid range of options, this will have no effect.
     * @param mode The mode to specify.
     */
    public void setSelected(BrowserMode mode)
    {
        if(mode == null || !optionCheck(mode))
        {
            return;
        }
        selected = mode;
    }
}
